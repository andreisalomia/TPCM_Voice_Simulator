package com.example.tpcm_voice_simulator.repository;

import com.example.tpcm_voice_simulator.model.VoiceCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoiceCallRepository extends JpaRepository<VoiceCall, Long> {
}