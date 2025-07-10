package com.ccapp.ccgo.mission.service;
import com.ccapp.ccgo.mission.domain.MissionTemplate;
import java.util.List;


public interface MissionTemplateService {

    List<MissionTemplate> getSixMissionsByScore(int score);

    MissionTemplate refreshMission(int score, List<Long> excludedIds);
}
