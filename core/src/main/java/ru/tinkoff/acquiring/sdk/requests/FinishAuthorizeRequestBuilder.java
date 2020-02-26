/*
 * Copyright © 2016 Tinkoff Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.acquiring.sdk.requests;

import java.security.PublicKey;
import java.util.Map;

import ru.tinkoff.acquiring.sdk.AcquiringSdk;

/**
 * @author Mikhail Artemyev
 */
final public class FinishAuthorizeRequestBuilder extends AcquiringRequestBuilder<FinishAuthorizeRequest> {

    private FinishAuthorizeRequest request = new FinishAuthorizeRequest();

    /**
     * Билдер для запроса FinishAuthorize
     *
     * @param password    Пароль. Выдается банком на каждый магазин.
     * @param terminalKey Уникальный идентификатор терминала. Выдается банком на каждый магазин.
     */
    public FinishAuthorizeRequestBuilder(final String password, final String terminalKey) {
        super(password, terminalKey);
    }

    /**
     * @param value Уникальный идентификатор транзакции в системе Банка, полученный в ответе на вызов метода Init
     */
    public FinishAuthorizeRequestBuilder setPaymentId(final Long value) {
        request.setPaymentId(value);
        return this;
    }

    /**
     * @param value Отправлять Email-нотификацию об успешном платеже
     */
    public FinishAuthorizeRequestBuilder setSendEmail(final boolean value) {
        request.setSendEmail(value);
        return this;
    }

    /**
     * @param value Данные карты, преобразованные методом {@link ru.tinkoff.acquiring.sdk.CardData#encode(PublicKey)},
     *              где ключ получается {@link AcquiringSdk#getPublicKey()}
     */
    public FinishAuthorizeRequestBuilder setCardData(final String value) {
        request.setCardData(value);
        return this;
    }

    /**
     * @param token Токен для оплаты с помощью Google Pay
     */
    public FinishAuthorizeRequestBuilder setGooglePayToken(final String token) {
        request.setGooglePayToken(token);
        request.setSource("GooglePay");
        return this;
    }

    /**
     * @param email Email для отправки
     */
    public FinishAuthorizeRequestBuilder setEmail(final String email) {
        request.setEmail(email);
        return this;
    }

    public FinishAuthorizeRequestBuilder setIp(final String ip) {
        request.setIp(ip);
        return this;
    }

    public FinishAuthorizeRequestBuilder setData(final Map<String, String> data) {
        request.setData(data);
        return this;
    }

    @Override
    protected void validate() {
        validateZeroOrPositive(request.getPaymentId(), "Payment ID");
        if (request.getGooglePayToken() != null) {
            validateNonEmpty(request.getGooglePayToken(), AcquiringRequest.ANDROID_PAY_TOKEN);
            validateNonEmpty(request.getSource(), AcquiringRequest.SOURCE);
        } else if (request.getCardId() == null) {
            validateNonEmpty(request.getCardData(), "Card data");
        } else {
            validateNonEmpty(request.getCardId(), "CardId");
            validateNonEmpty(request.getCvv(), "CVV");
        }
    }

    @Override
    protected FinishAuthorizeRequest getRequest() {
        return request;
    }
}
