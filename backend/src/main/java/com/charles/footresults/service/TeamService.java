package com.charles.footresults.service;

import com.charles.footresults.domain.ClubUefaRanking;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.repository.ClubUefaRankingRepository;
import com.charles.footresults.repository.HonourRepository;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Le fichier Excel source utilise parfois des libelles legerement differents
 * pour le meme club (ex: "VLLAZNIA" / "VLLAZINA"), ce qui cree des equipes
 * doublons apres import (limitation documentee dans le README). Cet endpoint
 * permet de fusionner deux fiches equipe en une seule : tous les matchs et
 * statuts de "source" sont reaffectes a "target" (ainsi que sa ligne du classement UEFA
 * des clubs et son logo si "target" n'en a pas), puis "source" est supprimee.
 *
 * Une equipe creee par erreur peut aussi etre supprimee, mais seulement si rien ne l'utilise
 * (matchs, statuts, classement UEFA, palmares) : sinon c'est une fusion qu'il faut.
 */
@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TeamCompetitionStatusRepository statusRepository;
    private final ClubUefaRankingRepository clubRankingRepository;
    private final HonourRepository honourRepository;

    public TeamService(
            TeamRepository teamRepository,
            MatchRepository matchRepository,
            TeamCompetitionStatusRepository statusRepository,
            ClubUefaRankingRepository clubRankingRepository,
            HonourRepository honourRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.statusRepository = statusRepository;
        this.clubRankingRepository = clubRankingRepository;
        this.honourRepository = honourRepository;
    }

    public TeamDto merge(Long sourceId, Long targetId) {
        if (sourceId.equals(targetId)) {
            throw new IllegalArgumentException("Impossible de fusionner une equipe avec elle-meme");
        }
        Team source = teamRepository
                .findById(sourceId)
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + sourceId));
        Team target = teamRepository
                .findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + targetId));

        List<Match> matches = matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(sourceId, sourceId);
        boolean alreadyPlayedEachOther = matches.stream()
                .anyMatch(m -> (m.getTeam1().getId().equals(sourceId)
                                && m.getTeam2().getId().equals(targetId))
                        || (m.getTeam1().getId().equals(targetId)
                                && m.getTeam2().getId().equals(sourceId)));
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
                    .findByCompetitionIdAndTeamId(status.getCompetition().getId(), targetId)
                    .isPresent();
            if (targetAlreadyHasStatus) {
                statusRepository.delete(status);
            } else {
                status.setTeam(target);
                statusRepository.save(status);
            }
        }

        List<ClubUefaRanking> rankings = clubRankingRepository.findByTeamId(sourceId);
        for (ClubUefaRanking ranking : rankings) {
            ranking.setTeam(target);
        }
        clubRankingRepository.saveAll(rankings);

        honourRepository.reassignWinner(source, target);
        honourRepository.reassignRunnerUp(source, target);
        honourRepository.reassignThird(source, target);

        if (target.getLogoPath() == null && source.getLogoPath() != null) {
            target.setLogoPath(source.getLogoPath());
            teamRepository.save(target);
        }

        teamRepository.delete(source);
        return TeamDto.from(target);
    }

    public void delete(Long id) {
        Team team = teamRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + id));
        List<String> usages = new ArrayList<>();
        addUsage(usages, matchRepository.countByTeam1_IdOrTeam2_Id(id, id), "match", "matchs");
        addUsage(usages, statusRepository.findByTeamId(id).size(), "statut de competition", "statuts de competition");
        addUsage(
                usages,
                clubRankingRepository.findByTeamId(id).size(),
                "ligne du classement UEFA",
                "lignes du classement UEFA");
        addUsage(usages, honourRepository.countByTeam(id), "ligne de palmares", "lignes de palmares");
        if (!usages.isEmpty()) {
            throw new IllegalArgumentException("Impossible de supprimer " + team.getName() + ", encore utilisee : "
                    + String.join(", ", usages) + ". Corrige ou fusionne-la plutot.");
        }
        teamRepository.delete(team);
    }

    private static void addUsage(List<String> usages, long count, String singular, String plural) {
        if (count > 0) {
            usages.add(count + " " + (count > 1 ? plural : singular));
        }
    }
}
