package com.charles.footresults.dto;

import java.util.List;

public record MatchPageDto(List<MatchDto> items, long totalCount) {}
