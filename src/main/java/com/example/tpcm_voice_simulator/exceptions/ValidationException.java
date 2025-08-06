package com.example.tpcm_voice_simulator.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}