package com.payment.gateway.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration 
public class PaymentStatusRabbitConfig { 
    public static final String STATUS_EXCHANGE = "payment.status.exchange"; 
    public static final String STATUS_QUEUE = "payment.status.queue"; 
    public static final String STATUS_ROUTING_KEY = "payment.status.updated";


 @Bean
  public TopicExchange paymentStatusExchange() { 
    return new TopicExchange(STATUS_EXCHANGE); } 

@Bean
     public Queue paymentStatusQueue() { return new Queue(STATUS_QUEUE, true);  }
 @Bean 
 public Binding paymentStatusBinding(Queue paymentStatusQueue, TopicExchange paymentStatusExchange) {
     return BindingBuilder.bind(paymentStatusQueue).to(paymentStatusExchange).with(STATUS_ROUTING_KEY); } 
    }