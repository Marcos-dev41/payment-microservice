package com.payment.gateway.rabbitmq;
    


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "payment.exchange";
    public static final String REQUEST_ROUTING_KEY = "payment.request.mpesa";
    public static final String REQUEST_QUEUE= "payment.request.mpesa";

    @Bean
    public TopicExchange paymExchange(){
        return new TopicExchange(EXCHANGE);
    }
    @Bean
    public Queue requestQueue(){
        return new Queue(REQUEST_QUEUE);
    }
    @Bean
    public Binding requstBinding(){
        return BindingBuilder.bind(requestQueue()).to(paymExchange()).with(REQUEST_ROUTING_KEY);
    }

     @Bean
    public MessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
