package com.example.tpcm_voice_simulator.exceptions;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorMessageTemplate {
    private int status;
    private String error;
    private String message;
}