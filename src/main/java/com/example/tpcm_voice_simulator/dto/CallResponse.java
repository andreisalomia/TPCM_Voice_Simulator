package com.example.tpcm_voice_simulator.dto;

import lombok.Data;

@Data
public class CallResponse {
    private String status;
    private String message;
    private Long callId;
}
