package com.ccapp.ccgo.matching.repository;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubGroupRepository extends JpaRepository<SubGroup, Long> {
    List<SubGroup> findByTeam_TeamId(Long teamId);

}
