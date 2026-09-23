package com.charles.footresults.dto;

/** Bilan de teamA contre teamB (du point de vue de teamA). */
public record HeadToHeadCellDto(Long teamAId, Long teamBId, int won, int drawn, int lost) {}
