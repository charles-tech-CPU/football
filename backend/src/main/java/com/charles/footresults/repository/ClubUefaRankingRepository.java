package com.charles.footresults.repository;

import com.charles.footresults.domain.ClubUefaRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubUefaRankingRepository extends JpaRepository<ClubUefaRanking, Long> {
    List<ClubUefaRanking> findAllByOrderByUefaRankAsc();
}
