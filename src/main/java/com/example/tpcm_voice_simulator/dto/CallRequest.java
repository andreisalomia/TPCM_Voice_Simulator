package com.example.tpcm_voice_simulator.dto;

import lombok.Data;

@Data
public class CallRequest {
    private String callerNumber;
    private String premiumNumber;
    private Integer durationSeconds;
}
