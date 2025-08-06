package com.example.tpcm_voice_simulator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PREMIUM_NUMBERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumNumber {

    @Id
    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "COST_1")
    private Double cost1;

    @Column(name = "DURATION_1")
    private Integer duration1;

    @Column(name = "COST_2")
    private Double cost2;

    @Column(name = "DURATION_2")
    private Integer duration2;

    @Column(name = "COST_3")
    private Double cost3;

    @Column(name = "DURATION_3")
    private Integer duration3;
}