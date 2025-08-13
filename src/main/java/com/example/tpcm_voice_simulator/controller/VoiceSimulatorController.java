package com.example.tpcm_voice_simulator.controller;

import com.example.tpcm_voice_simulator.dto.CallRequest;
import com.example.tpcm_voice_simulator.dto.CallResponse;
import com.example.tpcm_voice_simulator.service.VoiceSimulatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voice-simulator")
@RequiredArgsConstructor
@Slf4j
public class VoiceSimulatorController {

    private final VoiceSimulatorService voiceSimulatorService;

    @PostMapping("/call/start")
    public ResponseEntity<CallResponse> startCall(@RequestBody CallRequest request) {
        CallResponse response = voiceSimulatorService.startCall(request);
        return "ACCEPTED".equals(response.getStatus()) ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}