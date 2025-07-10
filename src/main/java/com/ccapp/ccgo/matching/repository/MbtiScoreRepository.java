package com.ccapp.ccgo.matching.repository;

import com.ccapp.ccgo.matching.domain.entity.MbtiScore;
import com.ccapp.ccgo.matching.domain.MbtiScoreId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MbtiScoreRepository extends JpaRepository<MbtiScore, MbtiScoreId> {
    List<MbtiScore> findAll();
}
