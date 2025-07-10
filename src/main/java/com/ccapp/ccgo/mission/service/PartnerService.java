package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.mission.entity.Partner;

import java.util.List;

public interface PartnerService {

    // 매칭된 SubGroup 정보를 받아 Partner(짝) 생성
    List<Partner> createPartnersFromSubGroups(List<SubGroup> subGroups);


}