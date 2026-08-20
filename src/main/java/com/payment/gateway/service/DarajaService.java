package com.payment.gateway.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.payment.gateway.repository.*;
import com.payment.gateway.model.*;
import org.springframework.http.HttpHeaders;



@Service
public class DarajaService {

    @Autowired
    private IncomingOrderRepo orderRepo;

    @Value("${consumer.key}")
    private String consumerKey;

    @Value("${consumer.secret}")
    private String consumerSecret;

    @Value("${bs.shortcode}")
    private Integer shortCode;

    @Value("${callback.url}")
    private String callbackUrl;

    @Value("${passkey}")
    private String passKey;

    public String getAccessToken(){
        String auth = consumerKey + ":" + consumerSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
    "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials",
    HttpMethod.GET,
    request,
    new ParameterizedTypeReference<Map<String, Object>>() {}
);

return (String) response.getBody().get("access_token");

}  

public String generateTimestamp(){
 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
 return LocalDateTime.now().format(formatter);
}

public String generatePassword(String timestamp){
    String rawPassword = shortCode + passKey + timestamp;
    return Base64.getEncoder().encodeToString(rawPassword.getBytes());
}

public Map<String,Object> intiateStkPush(IncomingOrder order){

    boolean LOAD_TEST_MODE = true; // flip to false to restore real M-Pesa calls

    Map<String,Object> responseBody = new HashMap<>();

    if (LOAD_TEST_MODE) {
        String fakeCheckoutRequestId = "ws_CO_LOADTEST_" + System.nanoTime();
        responseBody.put("CheckoutRequestID", fakeCheckoutRequestId);
        responseBody.put("MerchantRequestID", "LOADTEST-" + UUID.randomUUID());
        responseBody.put("ResponseCode", "0");
        responseBody.put("ResponseDescription", "Success. Request accepted for processing (STUBBED)");
        responseBody.put("CustomerMessage", "Success. Request accepted for processing (STUBBED)");
        System.out.println("STK push stubbed for load test, order: " + order.getOrderId());
    } else {
        String accessToken = getAccessToken();
        String timestamp = generateTimestamp();
        String password = generatePassword(timestamp);

        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("BusinessShortCode", shortCode);
        requestBody.put("Password", password);
        requestBody.put("Timestamp", timestamp);
        requestBody.put("TransactionType", "CustomerPayBillOnline");
        requestBody.put("Amount", order.getTotal().intValue());
        requestBody.put("PartyA", order.getPhoneNumber());
        requestBody.put("PartyB", shortCode);
        requestBody.put("PhoneNumber", order.getPhoneNumber());
        requestBody.put("CallBackURL", callbackUrl);
        requestBody.put("AccountReference", "Order" + order.getOrderId());
        requestBody.put("TransactionDesc", "Payment for order " + order.getOrderId());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String,Object>> request = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String,Object>> response = restTemplate.exchange(
            "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest",
            HttpMethod.POST, request,
            new ParameterizedTypeReference<Map<String,Object>>() {}
        );

        responseBody = response.getBody();
    }

    String checkoutRequestId = (String) responseBody.get("CheckoutRequestID");
    System.out.println(checkoutRequestId);
    order.setStatus("Initialized");
    orderRepo.save(order);

    return responseBody;
}
}