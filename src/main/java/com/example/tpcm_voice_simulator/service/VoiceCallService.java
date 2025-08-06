package com.example.tpcm_voice_simulator.service;

import com.example.tpcm_voice_simulator.model.VoiceCall;
import com.example.tpcm_voice_simulator.repository.VoiceCallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VoiceCallService {

    @Autowired
    private VoiceCallRepository voiceCallRepository;

    public List<VoiceCall> getAllCalls() {
        return voiceCallRepository.findAll();
    }

    public Optional<VoiceCall> getCallById(Long id) {
        return voiceCallRepository.findById(id);
    }

    public VoiceCall saveCall(VoiceCall call) {
        return voiceCallRepository.save(call);
    }

    public void deleteCall(Long id) {
        voiceCallRepository.deleteById(id);
    }
}