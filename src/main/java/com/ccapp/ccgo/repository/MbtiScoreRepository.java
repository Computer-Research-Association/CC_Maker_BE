package com.ccapp.ccgo.repository;

import com.ccapp.ccgo.matching.MbtiScore;
import com.ccapp.ccgo.matching.MbtiScoreId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MbtiScoreRepository extends JpaRepository<MbtiScore, MbtiScoreId> {
    List<MbtiScore> findAll();
}
