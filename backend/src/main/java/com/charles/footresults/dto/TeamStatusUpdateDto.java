package com.charles.footresults.dto;

public record TeamStatusUpdateDto(
        boolean defendingChampion,
        boolean promoted,
        boolean previousCupWinner
) {
}
