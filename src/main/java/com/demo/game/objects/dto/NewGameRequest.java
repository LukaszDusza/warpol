package com.demo.game.objects.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record NewGameRequest(@Min(4) @Max(20) int boardWidth,
                             @Min(4) @Max(20) int boardHeight,
                             @NotNull PlayerConfig whiteConfig,
                             @NotNull PlayerConfig blackConfig) { }
