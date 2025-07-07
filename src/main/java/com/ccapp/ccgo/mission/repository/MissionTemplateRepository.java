package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.mission.MissionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MissionTemplateRepository extends JpaRepository<MissionTemplate, Long> {

    List<MissionTemplate> findByScoreOrderById(int score);


    Long findMaxId();
}
