package com.charles.footresults.repository;

import com.charles.footresults.domain.EuropeanSlot;
import com.charles.footresults.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EuropeanSlotRepository extends JpaRepository<EuropeanSlot, Long> {

    List<EuropeanSlot> findBySeason(Integer season);

    List<EuropeanSlot> findBySeasonAndCountry(Integer season, String country);

    boolean existsBySeason(Integer season);

    @Query("select max(s.season) from EuropeanSlot s")
    Integer findLatestSeason();

    /** Fusion de clubs (TeamService.merge) : la place europeenne suit le club conserve. */
    @Modifying
    @Query("update EuropeanSlot s set s.team = :target where s.team = :source")
    void reassignTeam(@Param("source") Team source, @Param("target") Team target);
}
