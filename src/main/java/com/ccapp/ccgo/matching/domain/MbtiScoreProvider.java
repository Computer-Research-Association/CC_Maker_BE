package com.ccapp.ccgo.matching.domain;

import com.ccapp.ccgo.matching.domain.entity.MbtiScore;
import com.ccapp.ccgo.matching.repository.MbtiScoreRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Map 캐시 만들어주는 역할

@Component
@RequiredArgsConstructor
public class MbtiScoreProvider {

    private final MbtiScoreRepository mbtiScoreRepository;

    private final Map<String, Map<String, Integer>> scoreMap = new HashMap<>();

    @PostConstruct
    public void loadMbtiScores() {
        List<MbtiScore> scores = mbtiScoreRepository.findAll();
        for (MbtiScore score : scores) {
            scoreMap
                    .computeIfAbsent(score.getFromMbti(), k -> new HashMap<>())
                    .put(score.getToMbti(), score.getScore());
        }
    }

    public int getScore(String fromMbti, String toMbti) {
        return scoreMap
                .getOrDefault(fromMbti, new HashMap<>())
                .getOrDefault(toMbti, 0);
    }
}
