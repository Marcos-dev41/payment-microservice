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

    private Integer order_id;
    private BigDecimal total;
    private long phoneNumber;
    private String email;
    private Integer indempotency_key;
    private Integer correlationId;
    private String checkoutRequestId;
}
