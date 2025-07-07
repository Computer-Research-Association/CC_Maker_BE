package com.ccapp.ccgo.mission;

import com.ccapp.ccgo.matching.SubGroup;
import com.ccapp.ccgo.matching.SubGroupMember;
import com.ccapp.ccgo.mission.repository.PartnerRepository;
import com.ccapp.ccgo.mission.service.PartnerService;
import com.ccapp.ccgo.repository.SubGroupMemberRepository;
import com.ccapp.ccgo.team.Team;
import com.ccapp.ccgo.user.User;
import jakarta.transaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final SubGroupMemberRepository subGroupMemberRepository;

    @Override
    public List<Partner> createPartnersFromSubGroups(List<SubGroup> subGroups) {
        List<Partner> partners = new ArrayList<>();

        for (SubGroup sg : subGroups) {
            List<SubGroupMember> members = subGroupMemberRepository.findBySubGroup_Id(sg.getId());

            User user1 = members.get(0).getUser();
            User user2 = members.get(1).getUser();
            Team team = sg.getTeam();

            // 중복 Partner 체크 (팀, user1, user2 기준)
            boolean exists = partnerRepository.existsByTeamAndUser1AndUser2(team, user1, user2);
            if (exists) {
                continue;
            }

            Partner partner = Partner.builder()
                    .team(team)
                    .user1(user1)
                    .user2(user2)
                    .build();

            partners.add(partnerRepository.save(partner));
        }

        return partners;
    }

    @Override
    public List<Partner> findPartnersByTeamId(Long teamId) {
        return partnerRepository.findByTeam_TeamId(teamId);
    }

    @Override
    public Optional<Partner> findById(Long partnerId) {
        return partnerRepository.findById(partnerId);
    }

    @Override
    public void deletePartner(Long partnerId) {
        partnerRepository.deleteById(partnerId);
    }
}