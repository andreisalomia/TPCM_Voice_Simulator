package com.example.tpcm_voice_simulator.repository;

import com.example.tpcm_voice_simulator.model.PremiumNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PremiumNumberRepository extends JpaRepository<PremiumNumber, Long> {
    Optional<PremiumNumber> findByPhoneNumber(String phoneNumber);

    void deleteByPhoneNumber(String phoneNumber);
}