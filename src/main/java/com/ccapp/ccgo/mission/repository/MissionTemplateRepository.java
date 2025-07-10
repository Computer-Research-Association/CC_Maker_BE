package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.mission.domain.MissionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface MissionTemplateRepository extends JpaRepository<MissionTemplate, Long> {

    @Query(value = "SELECT * FROM mission_template WHERE score = :score LIMIT 6", nativeQuery = true)
    List<MissionTemplate> findTop6ByScore(@Param("score") int score);

    // 새로고침용, 기존 6개 제외하고 랜덤 1개 조회
    @Query(value = "SELECT * FROM mission_template WHERE score = :score AND id NOT IN :excludedIds ORDER BY RAND() LIMIT 1", nativeQuery = true)
    MissionTemplate findRandomByScoreExcludingIds(@Param("score") int score, @Param("excludedIds") List<Long> excludedIds);

}
