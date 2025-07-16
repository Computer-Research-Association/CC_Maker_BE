package com.ccapp.ccgo.mission.service;
import com.ccapp.ccgo.mission.entity.MissionTemplate;
import java.util.List;


public interface MissionTemplateService {

    List<MissionTemplate> getSixMissionsByScore(Integer score);

    MissionTemplate refreshMission(Integer score, List<Long> excludedIds);
}
