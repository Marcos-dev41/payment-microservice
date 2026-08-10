package com.payment.gateway.controller;


import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payment.gateway.model.*;
import com.payment.gateway.rabbitmq.PaymentStatusRabbitConfig;
import com.payment.gateway.repository.IncomingOrderRepo;
import com.payment.gateway.service.*;

@RestController
@RequestMapping("/payments/webhook/mpesa")
public class MpesaPaymentController {

    @Autowired
    private DarajaService darajaService;

    @Autowired
    private IncomingOrderRepo orderRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/test-token")
    public String testToken() {
        return darajaService.getAccessToken();
    } 

    @PostMapping("/stkpush")
    public Map<String,Object> triggerStkPush(@RequestBody IncomingOrder order){
        return darajaService.intiateStkPush(order);

    }

    @PostMapping("/callback")
public Map<String, Object> handleCallback(@RequestBody Map<String, Object> callbackData) {
    System.out.println("Daraja callback received: " + callbackData);
    
    Map<String,Object> body = (Map<String,Object>)callbackData.get("Body");
    Map<String,Object> stkCallback = (Map<String,Object>)body.get("stkCallback");

    String checkoutRequestId = (String) stkCallback.get("CheckoutRequestID");
    int resultCode = (int) stkCallback.get("ResultCode");

    IncomingOrder order = orderRepo.findByCheckoutRequestId(checkoutRequestId).orElse(null);
   

    if(order == null){
        System.out.println("no matching order for checkoutRequestId: " +  checkoutRequestId);
        return Map.of("ResultCode",0,"ResultDesc","Accepted");
    }
     String email = order.getEmail();
      String mpesaReceiptNumber= null;

    if(resultCode == 0){
    List<Map<String, Object>> items = (List<Map<String, Object>>)
    ((Map<String, Object>) stkCallback.get("CallbackMetadata")).get("Item");
       
        Long phoneNumber = null;

        for(Map<String,Object>item :items){
            String name = (String) item.get("Name");
            if (name.equals("MpesaReceiptNumber")){
                mpesaReceiptNumber = (String)item.get("Value");
            }else if(name.equals("PhoneNumber"))
            {
                phoneNumber = ((Number) item.get("Value")).longValue();
            }
    }
             order.setStatus("PAID");
             orderRepo.save(order);
            emailService.sendPaymentConfirmation(email, order);     
    }else{
            order.setStatus("FAILED");
            orderRepo.save(order);

            emailService.sendPaymentFailure(email, order);
    }

     PaymentStatusUpdate update = new PaymentStatusUpdate(
            order.getCorrelationId(),
            order.getStatus(),
            order.getCheckoutRequestId(),
            mpesaReceiptNumber
        );

        rabbitTemplate.convertAndSend(
            PaymentStatusRabbitConfig.STATUS_EXCHANGE,
            PaymentStatusRabbitConfig.STATUS_ROUTING_KEY,
            update
        );
 
    return Map.of("ResultCode", 0, "ResultDesc", "Accepted");

}}