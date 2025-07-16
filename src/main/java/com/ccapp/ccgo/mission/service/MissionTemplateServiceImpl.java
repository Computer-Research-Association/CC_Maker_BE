package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.mission.entity.MissionTemplate;
import com.ccapp.ccgo.mission.repository.MissionTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionTemplateServiceImpl implements MissionTemplateService {

    private final MissionTemplateRepository missionTemplateRepository;

    @Override
    public List<MissionTemplate> getSixMissionsByScore(Integer score) {
        return missionTemplateRepository.findTop6ByScore(score);
    }

    @Override
    public MissionTemplate refreshMission(Integer score, List<Long> excludedIds) {
        return missionTemplateRepository.findRandomByScoreExcludingIds(score, excludedIds);
    }
}
