package com.demo.game.objects.dto;

import jakarta.validation.constraints.Min;

public record PlayerConfig(@Min(0) int archers,
                           @Min(0) int transports,
                           @Min(0) int cannons) { }