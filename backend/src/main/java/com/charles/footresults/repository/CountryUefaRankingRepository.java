package com.charles.footresults.repository;

import com.charles.footresults.domain.CountryUefaRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryUefaRankingRepository extends JpaRepository<CountryUefaRanking, Long> {
    List<CountryUefaRanking> findAllByOrderByUefaRankAsc();
}
