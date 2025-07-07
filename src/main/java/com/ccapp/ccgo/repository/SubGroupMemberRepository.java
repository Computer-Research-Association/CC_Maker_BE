package com.ccapp.ccgo.repository;

import com.ccapp.ccgo.matching.SubGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubGroupMemberRepository extends JpaRepository<SubGroupMember, Long> {
    List<SubGroupMember> findBySubGroup_Id(Long subGroupId);
}
