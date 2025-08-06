package com.example.tpcm_voice_simulator.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}