package com.payment.gateway.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor


public class IncomingOrder{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer order_no;

    private Integer orderId;
    private BigDecimal total;
    private String phoneNumber;
    private String currency;
    private String provider;
    private String email;
    private String correlationId;
    private String checkoutRequestId;
    private String indempotecykey;
    private String status;
}
