package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.matching.SubGroup;
import com.ccapp.ccgo.mission.domain.Partner;

import java.util.List;
import java.util.Optional;

public interface PartnerService {

    // 매칭된 SubGroup 정보를 받아 Partner(짝) 생성
    List<Partner> createPartnersFromSubGroups(List<SubGroup> subGroups);


}