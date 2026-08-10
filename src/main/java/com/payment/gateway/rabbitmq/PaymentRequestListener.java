package com.payment.gateway.rabbitmq;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.payment.gateway.service.*;
import com.payment.gateway.model.*;
import com.payment.gateway.repository.*;

@Component
public class PaymentRequestListener {

    @Autowired
    private DarajaService darajaService;

    @Autowired
    private IncomingOrderRepo orderRepo;

    @RabbitListener(queues = "payment.request.mpesa")
    public void handlePaymentRequest(PaymentRequestDto dto) {

        IncomingOrder order = new IncomingOrder();
        order.setOrderId(dto.getOrderId());
        order.setTotal(dto.getAmount());
        order.setPhoneNumber(dto.getPhoneNumber());
        order.setCurrency(dto.getCurrency());
        order.setProvider(dto.getProvider());
        order.setEmail(dto.getEmail());
        order.setCorrelationId(dto.getCorrelationId());

        order = orderRepo.save(order);

        Map<String, Object> result = darajaService.intiateStkPush(order);

        // Safaricom's synchronous STK push response usually includes a CheckoutRequestID
        // even though the actual payment result comes later via callback
        if (result != null && result.get("CheckoutRequestID") != null) {
            order.setCheckoutRequestId(result.get("CheckoutRequestID").toString());
            orderRepo.save(order);
        }
    }
}