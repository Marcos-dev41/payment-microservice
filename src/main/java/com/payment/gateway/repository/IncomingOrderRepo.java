package com.payment.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.payment.gateway.model.IncomingOrder;

@Repository

public interface IncomingOrderRepo  extends JpaRepository<IncomingOrder, Integer>{
    Optional<IncomingOrder> findByCheckoutRequestId (String checkoutRequestId);
}
