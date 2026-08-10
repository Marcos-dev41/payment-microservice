package com.payment.gateway.model;


import java.io.Serializable;

public class PaymentStatusUpdate implements Serializable {

    private String correlationId;
    private String status;
    private String checkoutRequestId;
    private String mpesaReceiptNumber;

    public PaymentStatusUpdate() {}

    public PaymentStatusUpdate(String correlationId, String status, String checkoutRequestId, String mpesaReceiptNumber) {
        this.correlationId = correlationId;
        this.status = status;
        this.checkoutRequestId = checkoutRequestId;
        this.mpesaReceiptNumber = mpesaReceiptNumber;
    }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCheckoutRequestId() { return checkoutRequestId; }
    public void setCheckoutRequestId(String checkoutRequestId) { this.checkoutRequestId = checkoutRequestId; }

    public String getMpesaReceiptNumber() { return mpesaReceiptNumber; }
    public void setMpesaReceiptNumber(String mpesaReceiptNumber) { this.mpesaReceiptNumber = mpesaReceiptNumber; }
}