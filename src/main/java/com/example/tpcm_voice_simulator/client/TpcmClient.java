package com.example.tpcm_voice_simulator.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Component
public class TpcmClient {

    @Value("${tpcm.api.url:http://localhost:8080}")
    private String tpcmApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String requestTransaction(String msisdn, Double amount, boolean partialReservation) {
        try {
            String url = tpcmApiUrl + "/api/app/transactions/flow/request";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = Map.of(
                    "msisdn", msisdn,
                    "amount", amount,
                    "partialReservation", partialReservation ? "Y" : "N",
                    "channel", "CALL"
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().get("transactionId").toString();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean commitTransaction(String transactionId, Double actualAmount) {
        try {
            String url = tpcmApiUrl + "/api/app/transactions/flow/commit";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = Map.of(
                    "transactionId", transactionId,
                    "amount", actualAmount
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean cancelTransaction(String transactionId) {
        try {
            String url = tpcmApiUrl + "/api/app/transactions/flow/cancel/" + transactionId;
            restTemplate.put(url, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Double getBalance(String msisdn) {
        try {
            String url = tpcmApiUrl + "/api/app/transactions/flow/balance/" + msisdn;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Double.valueOf(response.getBody().get("balance").toString());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}