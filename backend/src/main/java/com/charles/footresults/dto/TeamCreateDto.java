package com.charles.footresults.dto;

import jakarta.validation.constraints.NotBlank;

public record TeamCreateDto(@NotBlank String name, String country) {}
