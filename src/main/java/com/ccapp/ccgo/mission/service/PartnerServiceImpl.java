package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.matching.domain.entity.SubGroupMember;
import com.ccapp.ccgo.mission.entity.Partner;
import com.ccapp.ccgo.mission.repository.PartnerRepository;
import com.ccapp.ccgo.matching.repository.SubGroupMemberRepository;
import com.ccapp.ccgo.team.repository.TeamRepository;
import com.ccapp.ccgo.user.repository.UserRepository;
import com.ccapp.ccgo.team.entity.Team;
import com.ccapp.ccgo.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final SubGroupMemberRepository subGroupMemberRepository;  // 추가

    @Override
    public List<Partner> createPartnersFromSubGroups(List<SubGroup> subGroups) {
        List<Partner> partners = new ArrayList<>();

        for (SubGroup subGroup : subGroups) {
            Team team = subGroup.getTeam();

            // 해당 SubGroup의 멤버 2명 조회
            List<SubGroupMember> members = subGroupMemberRepository.findBySubGroup(subGroup);

            if (members.size() != 2) {
                throw new IllegalStateException("SubGroup에 멤버가 2명이 아닙니다: " + subGroup.getId());
            }

            User user1 = members.get(0).getUser();
            User user2 = members.get(1).getUser();

            Partner partner = Partner.builder()
                    .team(team)
                    .user1(user1)
                    .user2(user2)
                    .build();

            partnerRepository.save(partner);
            partners.add(partner);
        }

        return partners;
    }

}
