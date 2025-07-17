package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.mission.entity.MissionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MissionTemplateRepository extends JpaRepository<MissionTemplate, Long> {

    List<MissionTemplate> findByScore(Integer score);

}
