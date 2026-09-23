package com.charles.footresults.repository;

import com.charles.footresults.domain.ClubUefaRanking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubUefaRankingRepository extends JpaRepository<ClubUefaRanking, Long> {
    List<ClubUefaRanking> findAllByOrderByUefaRankAsc();
}
