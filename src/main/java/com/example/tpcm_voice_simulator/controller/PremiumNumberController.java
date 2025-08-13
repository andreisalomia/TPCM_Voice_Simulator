package com.example.tpcm_voice_simulator.controller;

import com.example.tpcm_voice_simulator.model.PremiumNumber;
import com.example.tpcm_voice_simulator.service.PremiumNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/premium-numbers")
public class PremiumNumberController {

    @Autowired
    private PremiumNumberService premiumNumberService;

    @GetMapping
    public List<PremiumNumber> getAllPremiumNumbers() {
        return premiumNumberService.getAllPremiumNumbers();
    }

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<PremiumNumber> getPremiumNumber(@PathVariable @Parameter(description = "Phone number") String phoneNumber) {
        return premiumNumberService.getPremiumNumberByPhone(phoneNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public PremiumNumber createPremiumNumber(@RequestBody PremiumNumber premiumNumber) {
        return premiumNumberService.savePremiumNumber(premiumNumber);
    }

    @PutMapping("/{phoneNumber}")
    public ResponseEntity<PremiumNumber> updatePremiumNumber(@PathVariable String phoneNumber, @RequestBody PremiumNumber premiumNumber) {
        if (premiumNumberService.getPremiumNumberByPhone(phoneNumber).isPresent()) {
            premiumNumber.setPhoneNumber(phoneNumber);
            return ResponseEntity.ok(premiumNumberService.savePremiumNumber(premiumNumber));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{phoneNumber}")
    public ResponseEntity<?> deletePremiumNumber(@PathVariable String phoneNumber) {
        if (premiumNumberService.getPremiumNumberByPhone(phoneNumber).isPresent()) {
            premiumNumberService.deletePremiumNumber(phoneNumber);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}