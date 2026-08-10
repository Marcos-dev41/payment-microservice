package com.payment.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.payment.gateway.model.*;

@Service
public class EmailService{

@Value("${spring.mail.username}")
    private String senderEmail;


@Autowired
private JavaMailSender mailSender;


@Async
public void sendPaymentConfirmation(String toEmail ,IncomingOrder order){
 
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(senderEmail);
    message.setTo(toEmail);
    message.setSubject("Payment Confrimation -Order #" + order.getOrderId());
    message.setText("Your payment of ksh " + order.getTotal() + " has been recieved succesfully. Thankyou!");
    mailSender.send(message);
}
@Async
public void sendPaymentFailure(String toEmail ,IncomingOrder order){
 
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(senderEmail);
    message.setTo(toEmail);
    message.setSubject("Payment Failed -Order #" + order.getOrderId());
    message.setText("Your payment of ksh " + order.getTotal() + " has been FAILED. please try again");
    mailSender.send(message);
}
}