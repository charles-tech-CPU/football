package com.charles.footresults.repository;

import com.charles.footresults.domain.TeamCompetitionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamCompetitionStatusRepository extends JpaRepository<TeamCompetitionStatus, Long> {
    List<TeamCompetitionStatus> findByCompetitionId(Long competitionId);

    List<TeamCompetitionStatus> findByTeamId(Long teamId);

    Optional<TeamCompetitionStatus> findByCompetitionIdAndTeamId(Long competitionId, Long teamId);
}
