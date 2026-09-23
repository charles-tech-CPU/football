package com.charles.footresults.repository;

import com.charles.footresults.domain.CountryUefaRanking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryUefaRankingRepository extends JpaRepository<CountryUefaRanking, Long> {
    List<CountryUefaRanking> findAllByOrderByUefaRankAsc();
}
