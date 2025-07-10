package com.ccapp.ccgo.matching.repository;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubGroupRepository extends JpaRepository<SubGroup, Long> {
    List<SubGroup> findByTeam_TeamId(Long teamId);
}
