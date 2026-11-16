package com.charles.footresults.repository;

import com.charles.footresults.domain.Rivalry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RivalryRepository extends JpaRepository<Rivalry, Long> {

    @Override
    @EntityGraph(attributePaths = {"team1", "team2"})
    List<Rivalry> findAll();

    Optional<Rivalry> findByTeam1_IdAndTeam2_Id(Long team1Id, Long team2Id);

    /** Rivalites d'un club (fusion de clubs, cf. TeamService.merge). */
    List<Rivalry> findByTeam1_IdOrTeam2_Id(Long team1Id, Long team2Id);
}
