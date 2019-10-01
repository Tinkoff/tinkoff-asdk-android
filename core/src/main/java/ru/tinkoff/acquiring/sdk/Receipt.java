package ru.tinkoff.acquiring.sdk;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author Vitaliy Markus
 * Объект с данными чека
 */
public class Receipt implements Serializable {

    @SerializedName("ShopCode")
    private String shopCode;

    @SerializedName("Items")
    private Item[] items;

    @SerializedName("Email")
    private String email;

    @SerializedName("Taxation")
    private Taxation taxation;

    @SerializedName("Phone")
    private String phone;

    @SerializedName("AgentData")
    private AgentData agentData;

    @SerializedName("SupplierInfo")
    private SupplierInfo supplierInfo;

    @SerializedName("Customer")
    private String customer;

    @SerializedName("CustomerInn")
    private String customerInn;

    /**
     * @param items    Массив содержащий в себе информацию о товарах.
     * @param email    Емейл.
     * @param taxation Система налогообложения.
     */
    public Receipt(Item[] items, String email, Taxation taxation) {
        this(null, items, email, taxation);
    }

    public Receipt(String shopCode, Item[] items, String email, Taxation taxation) {
        this.shopCode = shopCode;
        this.items = items;
        this.email = email;
        this.taxation = taxation;
    }

    public Item[] getItems() {
        return items;
    }

    public String getEmail() {
        return email;
    }

    public Taxation getTaxation() {
        return taxation;
    }

    public String getPhone() {
        return phone;
    }

    /**
     * @param phone Телефон
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getShopCode() {
        return shopCode;
    }

    public void setShopCode(String shopCode) {
        this.shopCode = shopCode;
    }

    public AgentData getAgentData() {
        return agentData;
    }

    public void setAgentData(AgentData agentData) {
        this.agentData = agentData;
    }

    public SupplierInfo getSupplierInfo() {
        return supplierInfo;
    }

    public void setSupplierInfo(SupplierInfo supplierInfo) {
        this.supplierInfo = supplierInfo;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCustomerInn() {
        return customerInn;
    }

    public void setCustomerInn(String customerInn) {
        this.customerInn = customerInn;
    }
}
