package com.example.tpcm_voice_simulator.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.util.Map;

@Component
@Slf4j
public class TpcmClient {

    @Value("${tpcm.api.url:http://localhost:8080}")
    private String tpcmApiUrl;

    @Value("${tpcm.api.username:admin}")
    private String tpcmUsername;

    @Value("${tpcm.api.password:admin}")
    private String tpcmPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    public String requestTransaction(String msisdn, Double amount, boolean partialReservation) {
        try {
            String url = tpcmApiUrl + "/api/app/transactions/flow/request";

            log.info("TPCM Request params - MSISDN: {}, Amount: {}, PartialReservation: {}",
                    msisdn, amount, partialReservation);

            HttpHeaders headers = createAuthHeaders();

            Map<String, Object> request = Map.of(
                    "msisdn", msisdn,
                    "amount", amount,
                    "partialReservation", partialReservation ? "Y" : "N",
                    "channel", "CALL"
            );

            log.info("TPCM Request body: {}", request);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("TPCM Response status: {}", response.getStatusCode());
            log.info("TPCM Response body: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String status = (String) body.get("status");
                Object transactionId = body.get("transactionId");

                if ("PENDING".equals(status)) {
                    log.info("TPCM Transaction approved - ID: {}, Status: {}", transactionId, status);
                    return transactionId != null ? transactionId.toString() : null;
                } else {
                    log.warn("TPCM Transaction rejected - ID: {}, Status: {}", transactionId, status);
                    return null;
                }
            }

            log.warn("TPCM Request failed - Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
            return null;
        } catch (Exception e) {
            log.error("TPCM Unexpected error: ", e);
            return null;
        }
    }

    public void commitTransaction(String transactionId, Double actualAmount) {
        try {
            String url = tpcmApiUrl + "/api/app/transactions/flow/commit";

            log.info("TPCM Commit params - TransactionId: {}, Amount: {}", transactionId, actualAmount);

            HttpHeaders headers = createAuthHeaders();

            Map<String, Object> request = Map.of(
                    "transactionId", transactionId,
                    "amount", actualAmount
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("TPCM Commit response status: {}", response.getStatusCode());
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("TPCM Commit result: {}", success ? "SUCCESS" : "FAILED");

        } catch (Exception e) {
            log.error("TPCM Commit unexpected error: ", e);
        }
    }


    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = tpcmUsername + ":" + tpcmPassword;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        log.debug("Added Basic Auth header for user: {}", tpcmUsername);
        return headers;
    }
}