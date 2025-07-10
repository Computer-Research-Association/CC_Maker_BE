package com.ccapp.ccgo.mission.repository;
import com.ccapp.ccgo.mission.entity.Partner;
import com.ccapp.ccgo.mission.entity.PartnerMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerMissionRepository extends JpaRepository<PartnerMission, Long> {

    List<PartnerMission> findByPartnerId(Long partnerId);

    List<PartnerMission> findByPartnerAndIsActiveTrue(Partner partner);

    Optional<PartnerMission> findTopByPartnerOrderByIdDesc(Partner partner);
}