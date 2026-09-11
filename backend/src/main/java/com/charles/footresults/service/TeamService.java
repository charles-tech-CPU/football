package com.charles.footresults.service;

import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Le fichier Excel source utilise parfois des libelles legerement differents
 * pour le meme club (ex: "VLLAZNIA" / "VLLAZINA"), ce qui cree des equipes
 * doublons apres import (limitation documentee dans le README). Cet endpoint
 * permet de fusionner deux fiches equipe en une seule : tous les matchs et
 * statuts de "source" sont reaffectes a "target", puis "source" est supprimee.
 */
@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TeamCompetitionStatusRepository statusRepository;

    public TeamService(TeamRepository teamRepository, MatchRepository matchRepository,
                        TeamCompetitionStatusRepository statusRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.statusRepository = statusRepository;
    }

    public TeamDto merge(Long sourceId, Long targetId) {
        if (sourceId.equals(targetId)) {
            throw new IllegalArgumentException("Impossible de fusionner une equipe avec elle-meme");
        }
        Team source = teamRepository.findById(sourceId)
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + sourceId));
        Team target = teamRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + targetId));

        List<Match> matches = matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(sourceId, sourceId);
        boolean alreadyPlayedEachOther = matches.stream().anyMatch(m ->
                (m.getTeam1().getId().equals(sourceId) && m.getTeam2().getId().equals(targetId))
                        || (m.getTeam1().getId().equals(targetId) && m.getTeam2().getId().equals(sourceId)));
        if (alreadyPlayedEachOther) {
            throw new IllegalArgumentException(
                    "Ces deux equipes se sont deja affrontees dans un match : ce ne sont probablement pas des doublons");
        }

        for (Match m : matches) {
            if (m.getTeam1().getId().equals(sourceId)) m.setTeam1(target);
            if (m.getTeam2().getId().equals(sourceId)) m.setTeam2(target);
        }
        matchRepository.saveAll(matches);

        for (TeamCompetitionStatus status : statusRepository.findByTeamId(sourceId)) {
            boolean targetAlreadyHasStatus = statusRepository
                    .findByCompetitionIdAndTeamId(status.getCompetition().getId(), targetId).isPresent();
            if (targetAlreadyHasStatus) {
                statusRepository.delete(status);
            } else {
                status.setTeam(target);
                statusRepository.save(status);
            }
        }

        teamRepository.delete(source);
        return TeamDto.from(target);
    }
}
