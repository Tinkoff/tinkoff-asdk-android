package ru.tinkoff.acquiring.payment

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import ru.tinkoff.acquiring.sdk.*
import ru.tinkoff.acquiring.sdk.requests.InitRequestBuilder
import ru.tinkoff.acquiring.sdk.responses.Check3dsVersionResponse

/**
 * @author Stanislav Mukhametshin
 */
class PaymentProcess internal constructor() {

    private val handler = PaymentHandler()
    private lateinit var thread: Thread
    private lateinit var requestBuilder: InitRequestBuilder
    private var check3dsVersionResponse: Check3dsVersionResponse? = null
    private var paymentListeners: Set<PaymentListener> = HashSet()
    private val paymentDataUi = PaymentDataUi()
    private var lastException: Exception? = null
    private var lastKnownAction: Int? = null
    private var paymentId: Long? = null
    private var state: Int = CREATED

    private companion object {
        private const val SUCCESS = 1
        private const val START_3DS = 2
        private const val CHARGE_REQUEST_REJECTED = 3
        private const val EXCEPTION = 4
        private const val COLLECT_3DS_DATA = 5

        private const val CREATED = 0
        private const val EXECUTING = 1
        private const val FINISHED = 2
    }

    @JvmSynthetic
    internal fun initPaymentRequest(
            initRequestBuilder: InitRequestBuilder,
            paymentData: PaymentData,
            modifyRequest: InitRequestBuilder.() -> Unit
    ): PaymentProcess {
        requestBuilder = paymentData.run {
            initRequestBuilder.setOrderId(orderId)
                    .setAmount(coins)
                    .setChargeFlag(chargeMode)
                    .setCustomerKey(paymentData.customerKey)
                    .setRecurrent(recurrentPayment)
                    .apply {
                        paymentData.marketPlaceData?.apply {
                            setShops(shops, receipts)
                        }
                    }
                    .also(modifyRequest)
        }
        paymentDataUi.recurrentPayment = paymentData.recurrentPayment
        return this
    }

    @JvmSynthetic
    internal fun initPaymentThread(sdk: AcquiringSdk,
                                   paySource: PaySource,
                                   email: String?,
                                   chargeMode: Boolean): PaymentProcess {
        paymentDataUi.card = (paySource as? CardDataPaySource)?.cardData?.map()

        thread = Thread(Runnable {
            try {
                val paymentId = sdk.init(requestBuilder)

                if (Thread.interrupted()) throw InterruptedException()
                var threeDsData: ThreeDsData? = null

                if (paySource is GPayTokenPaySource) {
                    threeDsData = sdk.finishAuthorize(paymentId, paySource.token, email)
                } else {
                    val cardData = (paySource as? CardDataPaySource)?.cardData
                            ?: throw IllegalArgumentException()

                    if (chargeMode) {
                        val paymentInfo = sdk.charge(paymentId, cardData.rebillId)
                        if (paymentInfo.isSuccess) {
                            handler.obtainMessage(SUCCESS, paymentId).sendToTarget()
                        } else {
                            handler.obtainMessage(CHARGE_REQUEST_REJECTED, paymentInfo).sendToTarget()
                        }
                    } else {
                        val versionResponse = sdk.check3DsVersion(paymentId, cardData)
                        if (versionResponse.version == ThreeDsVersion.TWO) {
                            val threeDsUrl = versionResponse.threeDsMethodUrl

                            if (threeDsUrl != null && threeDsUrl.isNotEmpty()) {
                                handler.obtainMessage(COLLECT_3DS_DATA, versionResponse).sendToTarget()
                            } else {
                                handler.obtainMessage(COLLECT_3DS_DATA, null).sendToTarget()
                            }

                            synchronized(paymentDataUi.collectedDeviceData) {
                                if (paymentDataUi.collectedDeviceData.isEmpty()) {
                                    (paymentDataUi.collectedDeviceData as Object).wait()
                                }
                            }

                            threeDsData = sdk.finishAuthorize(paymentId, paySource.cardData, email, paymentDataUi.collectedDeviceData)

                        } else {
                            threeDsData = sdk.finishAuthorize(paymentId, paySource.cardData, email, null)
                        }
                    }
                }

                if (threeDsData != null && threeDsData.isThreeDsNeed) {
                    handler.obtainMessage(START_3DS, threeDsData).sendToTarget()
                } else {
                    handler.obtainMessage(SUCCESS, paymentId).sendToTarget()
                }

            } catch (e: Exception) {
                handler.obtainMessage(EXCEPTION, e).sendToTarget()
            }
        })
        return this
    }

    private fun CardData.map(): Card {
        val card = Card()
        card.pan = pan
        card.cardId = cardId
        card.rebillId = rebillId
        card.status = CardStatus.ACTIVE
        return card
    }

    fun subscribe(paymentListener: PaymentListener) {
        this.paymentListeners += paymentListener
        val action = lastKnownAction
        if (action != null) {
            sendToListener(action, paymentListener)
        }
    }

    fun unsubscribe(paymentListener: PaymentListener) {
        this.paymentListeners -= paymentListener
    }

    fun start(): PaymentProcess {
        if (state != CREATED) {
            throw IllegalStateException("Already in use create another PaymentProcess")
        }
        state = EXECUTING
        thread.start()
        return this
    }

    fun stop() {
        thread.interrupt()
        state = FINISHED
    }

    private fun sendToListeners(action: Int) {
        lastKnownAction = action
        paymentListeners.forEach { sendToListener(action, it) }
    }

    private fun sendToListener(action: Int, listener: PaymentListener) {
        listener.apply {
            when (action) {
                SUCCESS -> {
                    val paymentId = paymentId ?: return
                    onSuccess(paymentId)
                }
                COLLECT_3DS_DATA -> {
                    synchronized(paymentDataUi.collectedDeviceData) {
                        onCollectDeviceData(check3dsVersionResponse)?.let { paymentDataUi.collectedDeviceData.putAll(it) }
                        (paymentDataUi.collectedDeviceData as Object).notify()
                    }
                }
                CHARGE_REQUEST_REJECTED, START_3DS -> onUiNeeded(paymentDataUi)
                EXCEPTION -> {
                    val lastException = lastException ?: return
                    onError(lastException)
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    inner class PaymentHandler : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                SUCCESS -> paymentId = msg.obj as Long
                EXCEPTION -> lastException = (msg.obj as Exception)
                CHARGE_REQUEST_REJECTED -> {
                    paymentDataUi.paymentInfo = msg.obj as PaymentInfo
                    paymentDataUi.status = PaymentDataUi.Status.REJECTED
                }
                COLLECT_3DS_DATA -> {
                    check3dsVersionResponse = if (msg.obj != null) {
                        msg.obj as Check3dsVersionResponse
                    } else null
                }
                START_3DS -> {
                    paymentDataUi.threeDsData = msg.obj as ThreeDsData
                    paymentDataUi.status = PaymentDataUi.Status.THREE_DS_NEEDED
                }
            }
            sendToListeners(msg.what)
            if (msg.what != COLLECT_3DS_DATA) {
                state = FINISHED
            }
        }
    }
}
