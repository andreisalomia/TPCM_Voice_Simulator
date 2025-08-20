package com.example.tpcm_voice_simulator.service;

import com.example.tpcm_voice_simulator.client.TpcmClient;
import com.example.tpcm_voice_simulator.dto.CallRequest;
import com.example.tpcm_voice_simulator.dto.CallResponse;
import com.example.tpcm_voice_simulator.repository.PremiumNumberRepository;
import com.example.tpcm_voice_simulator.repository.VoiceCallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.tpcm_voice_simulator.model.PremiumNumber;
import com.example.tpcm_voice_simulator.model.VoiceCall;
import java.time.LocalDateTime;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceSimulatorService {
    private final TpcmClient tpcmClient;
    private final PremiumNumberRepository premiumNumberRepository;
    private final VoiceCallRepository voiceCallRepository;
    private final ExecutorService callExecutor = Executors.newCachedThreadPool();
    private final AtomicLong callIdGenerator = new AtomicLong(1);

    public CallResponse startCall(CallRequest request) {
        if(request.getCallerNumber() == null || request.getPremiumNumber() == null
                || request.getDurationSeconds() == null || request.getDurationSeconds() <= 0) {
            CallResponse response = new CallResponse();
            response.setStatus("FAILED");
            response.setMessage("Invalid request parameters");
            return response;
        }

        Long callId = callIdGenerator.getAndIncrement();

        CompletableFuture.runAsync(() -> simulateCall(request, callId), callExecutor);

        CallResponse response = new CallResponse();
        response.setStatus("ACCEPTED");
        response.setMessage("Call simulation started successfully");
        response.setCallId(callId);
        log.info("CallId {}: Call simulation request accepted", callId);
        return response;
    }

    private void simulateCall(CallRequest request, Long callId) {
        log.info("Starting call simulation for callId: {}", callId);

        PremiumNumber premiumConfig = premiumNumberRepository.findByPhoneNumber(request.getPremiumNumber()).orElse(null);

        if (premiumConfig == null) {
            log.error("No premium number configuration found for: {}", request.getPremiumNumber());
            return;
        }

        int elapsedSeconds = 0;
        double totalCost = 0.0;

        try {
            log.info("CallId {}: Premium config - duration1: {}s (cost1: {}), duration2: {}s (cost2: {}), cost3: {}, duration3: {}s",
                    callId, premiumConfig.getDuration1(), premiumConfig.getCost1(),
                    premiumConfig.getDuration2(), premiumConfig.getCost2(),
                    premiumConfig.getCost3(), premiumConfig.getDuration3());

            if (premiumConfig.getDuration1() != null && premiumConfig.getDuration1() > 0) {
                log.info("CallId {}: Free phase - {} seconds", callId, premiumConfig.getDuration1());

                if (premiumConfig.getCost1() != null && premiumConfig.getCost1() != 0) {
                    log.warn("CallId {}: Free phase has non-zero cost: {}", callId, premiumConfig.getCost1());
                }

                for (int i = 0; i < premiumConfig.getDuration1(); i++) {
                    Thread.sleep(1000L);
                    elapsedSeconds++;
//                    daca apelul este mai scurt decat durata free atunci salvez direct
                    if (elapsedSeconds >= request.getDurationSeconds()) {
                        saveVoiceCall(request, callId, request.getDurationSeconds(), totalCost);
                        log.info("CallId {}: Call completed during free phase", callId);
                        return;
                    }
                }
                log.info("CallId {}: Free phase completed", callId);
            }

            if (premiumConfig.getDuration2() != null && premiumConfig.getDuration2() > 0) {
                log.info("CallId {}: Indivisible phase - {} seconds, cost: {}", callId, premiumConfig.getDuration2(), premiumConfig.getCost2());

                String transactionId2 = tpcmClient.requestTransaction(request.getCallerNumber(), premiumConfig.getCost2(), false);

                if (transactionId2 == null) {
                    log.warn("CallId {}: TPCM rejected indivisible request. Disconnecting.", callId);
                    saveVoiceCall(request, callId, request.getDurationSeconds(), totalCost);
                    return;
                }

                log.info("CallId {}: TPCM approved indivisible, transactionId: {}", callId, transactionId2);

                for (int i = 0; i < premiumConfig.getDuration2(); i++) {
                    Thread.sleep(1000L);
                    elapsedSeconds++;
//                    daca apelul este mai scurt decat durata indivizibila atunci salvez direct
                    if (elapsedSeconds >= request.getDurationSeconds()) {
                        tpcmClient.commitTransaction(transactionId2, premiumConfig.getCost2());
                        totalCost += premiumConfig.getCost2();
                        saveVoiceCall(request, callId, request.getDurationSeconds(), totalCost);
                        log.info("CallId {}: Call completed during indivisible phase", callId);
                        return;
                    }
                }

//                daca am terminat perioada 2 atunci facem commit pe
                tpcmClient.commitTransaction(transactionId2, premiumConfig.getCost2());
                totalCost += premiumConfig.getCost2();
                log.info("CallId {}: Committed indivisible amount: {}", callId, premiumConfig.getCost2());
            }

            if (elapsedSeconds < request.getDurationSeconds()) {
                if (premiumConfig.getCost3() != null && premiumConfig.getCost3() > 0) {
//                    daca exista durata 3 si costul 3 atunci trecem la faza 3
                    totalCost += simulateDivisiblePhase(request, callId, premiumConfig, elapsedSeconds);
                } else if (premiumConfig.getDuration2() != null && premiumConfig.getDuration2() > 0) {
//                    daca nu exista durata 3 dar exista 2 incepem sa repetam faza 2
                    totalCost += simulateRepeatedIndivisiblePhase(request, callId, premiumConfig, elapsedSeconds);
                } else {
                    log.info("CallId {}: No phase 3 configuration, ending call", callId);
                }
            }

            saveVoiceCall(request, callId, request.getDurationSeconds(), totalCost);

        } catch (InterruptedException e) {
            log.info("CallId {}: Call simulation interrupted", callId);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("CallId {}: Error during call simulation", callId, e);
        }
    }

    private double simulateDivisiblePhase(CallRequest request, Long callId, PremiumNumber premiumConfig, int startElapsed)
            throws InterruptedException {

        int elapsedSeconds = startElapsed;
        double phaseCost = 0.0;

        int segmentDuration = 0;
        if (premiumConfig.getDuration3() != null && premiumConfig.getDuration3() > 0) {
            segmentDuration = premiumConfig.getDuration3();
        }

        double costPerSecond = premiumConfig.getCost3() / segmentDuration;

        log.info("CallId {}: Starting divisible phase - cost total: {} for {}s, cost per second: {}",
                callId, premiumConfig.getCost3(), premiumConfig.getDuration3(), costPerSecond);

        while (elapsedSeconds < request.getDurationSeconds()) {
//            cat timp nu am terminat durata totala a apelului, incepem un ciclu nou de durata3 secunde
            int remainingDuration = request.getDurationSeconds() - elapsedSeconds;

            int currentSegmentDuration = Math.min(segmentDuration, remainingDuration);

            double segmentCost = costPerSecond * currentSegmentDuration;

            String transactionId = tpcmClient.requestTransaction(
                    request.getCallerNumber(),
                    segmentCost,
                    true
            );

            if (transactionId == null) {
                log.warn("CallId {}: TPCM rejected divisible request. Disconnecting.", callId);
                break;
            }

            log.info("CallId {}: TPCM approved divisible segment, transactionId: {}, requested duration: {}s, cost: {}",
                    callId, transactionId, currentSegmentDuration, segmentCost);

            int actualSegmentDuration = 0;

            for (int i = 0; i < currentSegmentDuration; i++) {
                Thread.sleep(1000L);
                elapsedSeconds++;
                actualSegmentDuration++;

                if (elapsedSeconds >= request.getDurationSeconds()) {
                    break;
                }
            }

            double actualCost = costPerSecond * actualSegmentDuration;
            tpcmClient.commitTransaction(transactionId, actualCost);
            phaseCost += actualCost;

            log.info("CallId {}: Committed divisible amount: {} for {}s (requested {}s)",
                    callId, actualCost, actualSegmentDuration, currentSegmentDuration);
        }

        log.info("CallId {}: Divisible phase completed, total phase cost: {}", callId, phaseCost);
        return phaseCost;
    }

    private double simulateRepeatedIndivisiblePhase(CallRequest request, Long callId, PremiumNumber premiumConfig, int startElapsed)
            throws InterruptedException {

        int elapsedSeconds = startElapsed;
        double phaseCost = 0.0;

        log.info("CallId {}: Starting repeated indivisible phase (cost3 is null)", callId);

        while (elapsedSeconds < request.getDurationSeconds()) {
            String transactionId = tpcmClient.requestTransaction(
                    request.getCallerNumber(),
                    premiumConfig.getCost2(),
                    false
            );

            if (transactionId == null) {
                log.warn("CallId {}: TPCM rejected repeated indivisible request. Disconnecting.", callId);
                break;
            }

            log.info("CallId {}: TPCM approved repeated indivisible segment, transactionId: {}", callId, transactionId);

            for (int i = 0; i < premiumConfig.getDuration2(); i++) {
                Thread.sleep(1000L);
                elapsedSeconds++;
                if (elapsedSeconds >= request.getDurationSeconds()) {
                    break;
                }
            }

            tpcmClient.commitTransaction(transactionId, premiumConfig.getCost2());
            phaseCost += premiumConfig.getCost2();
            log.info("CallId {}: Committed repeated indivisible amount: {}", callId, premiumConfig.getCost2());
        }

        log.info("CallId {}: Repeated indivisible phase completed, total phase cost: {}", callId, phaseCost);
        return phaseCost;
    }

    private void saveVoiceCall(CallRequest request, Long callId, int actualDuration, double totalCost) {
        try {
            VoiceCall voiceCall = new VoiceCall();
            voiceCall.setCallerNumber(request.getCallerNumber());
            voiceCall.setDurationSeconds(actualDuration);
            voiceCall.setChargedAmount(totalCost);
            voiceCall.setCallTimestamp(LocalDateTime.now());

            PremiumNumber premiumNumber = premiumNumberRepository.findByPhoneNumber(request.getPremiumNumber()).orElse(null);
            voiceCall.setPremiumNumber(premiumNumber);

            voiceCallRepository.save(voiceCall);
            log.info("CallId {}: Saved voice call record - duration: {}s, cost: {}", callId, actualDuration, totalCost);
        } catch (Exception e) {
            log.error("CallId {}: Failed to save voice call record", callId, e);
        }
    }
}