package com.example.tpcm_voice_simulator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "VOICE_CALLS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CALLER_NUMBER", nullable = false, length = 20)
    private String callerNumber;

    @Column(name = "DURATION_SECONDS")
    private Integer durationSeconds;

    @Column(name = "CHARGED_AMOUNT")
    private Double chargedAmount;

    @Column(name = "CALL_TIMESTAMP")
    private LocalDateTime callTimestamp;

    @ManyToOne
    @JoinColumn(name = "CALLED_NUMBER", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PremiumNumber premiumNumber;
}