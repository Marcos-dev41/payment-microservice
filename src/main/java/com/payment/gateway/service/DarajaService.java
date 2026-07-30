package com.payment.gateway.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
    String accessToken = getAccessToken();
    String timestamp = generateTimestamp();
    String password = generatePassword(timestamp);

    Map<String,Object> requestBody = new HashMap<>();
    requestBody.put("BusinessShortCode", shortCode);
    requestBody.put("Password",password);
    requestBody.put("Timestamp",timestamp);
    requestBody.put("TransactionType","CustomerPayBillOnline");
    requestBody.put("Amount", order.getTotal().intValue());
    requestBody.put("PartyA",order.getPhoneNumber());
    requestBody.put("PartyB",shortCode);
    requestBody.put("PhoneNumber",order.getPhoneNumber());
    requestBody.put("CallBackURL",callbackUrl);
    requestBody.put("AccountReference","Order"+ order.getOrder_id());
    requestBody.put("TransactionDesc","Payment for order "+ order.getOrder_id());


    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization","Bearer "+accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String,Object>> request = new HttpEntity<>(requestBody,headers);

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<Map<String,Object>> response = restTemplate.exchange("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest", HttpMethod.POST,request,new ParameterizedTypeReference <Map<String,Object>>() {});

    Map<String,Object> responseBody = response.getBody();

    String checkoutRequestId =(String) responseBody.get("CheckoutRequestID");
    System.out.println(checkoutRequestId);
    orderRepo.save(order);
     return responseBody;
}
}
