package com.example.tpcm_voice_simulator.controller;

import com.example.tpcm_voice_simulator.model.VoiceCall;
import com.example.tpcm_voice_simulator.service.VoiceCallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voice-calls")
public class VoiceCallController {

    @Autowired
    private VoiceCallService voiceCallService;

    @GetMapping
    public List<VoiceCall> getAllCalls() {
        return voiceCallService.getAllCalls();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoiceCall> getCallById(@PathVariable @Parameter(description = "Call ID") Long id) {
        return voiceCallService.getCallById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public VoiceCall createCall(@RequestBody VoiceCall call) {
        return voiceCallService.saveCall(call);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoiceCall> updateCall(@PathVariable Long id, @RequestBody VoiceCall call) {
        if (voiceCallService.getCallById(id).isPresent()) {
            call.setId(id);
            return ResponseEntity.ok(voiceCallService.saveCall(call));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCall(@PathVariable Long id) {
        if (voiceCallService.getCallById(id).isPresent()) {
            voiceCallService.deleteCall(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}