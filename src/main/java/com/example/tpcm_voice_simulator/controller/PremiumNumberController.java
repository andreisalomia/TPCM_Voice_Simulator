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
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true")
public class PremiumNumberController {

    @Autowired
    private PremiumNumberService premiumNumberService;

    @GetMapping
    @Operation(summary = "Get all premium numbers")
    public List<PremiumNumber> getAllPremiumNumbers() {
        return premiumNumberService.getAllPremiumNumbers();
    }

    @GetMapping("/{phoneNumber}")
    @Operation(summary = "Get premium number by phone number")
    public ResponseEntity<PremiumNumber> getPremiumNumber(@PathVariable @Parameter(description = "Phone number") String phoneNumber) {
        return premiumNumberService.getPremiumNumberByPhone(phoneNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new premium number")
    public PremiumNumber createPremiumNumber(@RequestBody PremiumNumber premiumNumber) {
        return premiumNumberService.savePremiumNumber(premiumNumber);
    }

    @PutMapping("/{phoneNumber}")
    @Operation(summary = "Update premium number")
    public ResponseEntity<PremiumNumber> updatePremiumNumber(@PathVariable String phoneNumber, @RequestBody PremiumNumber premiumNumber) {
        if (premiumNumberService.getPremiumNumberByPhone(phoneNumber).isPresent()) {
            premiumNumber.setPhoneNumber(phoneNumber);
            return ResponseEntity.ok(premiumNumberService.savePremiumNumber(premiumNumber));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{phoneNumber}")
    @Operation(summary = "Delete premium number")
    public ResponseEntity<?> deletePremiumNumber(@PathVariable String phoneNumber) {
        if (premiumNumberService.getPremiumNumberByPhone(phoneNumber).isPresent()) {
            premiumNumberService.deletePremiumNumber(phoneNumber);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}