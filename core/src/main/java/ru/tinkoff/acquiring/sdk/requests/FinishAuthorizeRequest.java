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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mikhail Artemyev
 */
final public class FinishAuthorizeRequest extends AcquiringRequest {

    private Long paymentId;
    private String sendEmail;
    private String cardData;
    private String email;
    private String cardId;
    private String cvv;
    private String googlePayToken;
    private String source;
    private String ip;
    private Map<String, String> data;

    public FinishAuthorizeRequest() {
        super("FinishAuthorize");
    }

    @Override
    public Map<String, Object> asMap() {
        final Map<String, Object> map = super.asMap();

        putIfNotNull(PAYMENT_ID, paymentId.toString(), map);
        putIfNotNull(SEND_EMAIL, sendEmail, map);
        putIfNotNull(CARD_DATA, cardData, map);
        putIfNotNull(CARD_ID, cardId, map);
        putIfNotNull(CVV, cvv, map);
        putIfNotNull(EMAIL, email, map);
        putIfNotNull(SOURCE, source, map);
        putIfNotNull(ANDROID_PAY_TOKEN, googlePayToken, map);
        putIfNotNull(IP, ip, map);
        if (data != null) {
            putDataIfNonNull(map);
        }

        return map;
    }

    @Override
    public Set<String> getTokenIgnoreFields() {
        Set<String> result = new HashSet<>();
        result.add(CARD_ID);
        result.add(CVV);
        result.add(DATA);
        return result;
    }

    public String getCvv() {
        return cvv;
    }

    void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCardId() {
        return cardId;
    }

    void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public boolean getSendEmail() {
        return "true".equals(sendEmail);
    }

    void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail ? "true" : "false";
    }

    public String getCardData() {
        return cardData;
    }

    void setCardData(String cardData) {
        this.cardData = cardData;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGooglePayToken() {
        return googlePayToken;
    }

    public void setGooglePayToken(String token) {
        this.googlePayToken = token;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public Boolean is3DsVersionV2() {
        return data != null && ip != null;
    }

    private void putDataIfNonNull(Map<String, Object> map) {
        HashMap<String, String> dataMap = new HashMap<>();
        if (data != null) {
            dataMap.putAll(data);
        }
        map.put(DATA, dataMap);
    }
}

