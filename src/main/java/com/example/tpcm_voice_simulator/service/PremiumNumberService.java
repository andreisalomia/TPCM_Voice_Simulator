package com.example.tpcm_voice_simulator.service;

import com.example.tpcm_voice_simulator.model.PremiumNumber;
import com.example.tpcm_voice_simulator.repository.PremiumNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PremiumNumberService {

    @Autowired
    private PremiumNumberRepository premiumNumberRepository;

    public List<PremiumNumber> getAllPremiumNumbers() {
        return premiumNumberRepository.findAll();
    }

    public Optional<PremiumNumber> getPremiumNumberByPhone(String phoneNumber) {
        return premiumNumberRepository.findByPhoneNumber(phoneNumber);
    }

    public PremiumNumber savePremiumNumber(PremiumNumber premiumNumber) {
        return premiumNumberRepository.save(premiumNumber);
    }

    public void deletePremiumNumber(String phoneNumber) {
        premiumNumberRepository.deleteByPhoneNumber(phoneNumber);
    }
}