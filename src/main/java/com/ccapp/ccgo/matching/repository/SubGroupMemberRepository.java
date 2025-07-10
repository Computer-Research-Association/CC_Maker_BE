package com.ccapp.ccgo.matching.repository;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.matching.domain.entity.SubGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubGroupMemberRepository extends JpaRepository<SubGroupMember, Long> {
    List<SubGroupMember> findBySubGroup_Id(Long subGroupId);

    // SubGroup 기준으로 멤버들 조회
    List<SubGroupMember> findBySubGroup(SubGroup subGroup);
}
