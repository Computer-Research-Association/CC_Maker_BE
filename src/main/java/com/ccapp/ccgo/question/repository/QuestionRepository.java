package com.ccapp.ccgo.question.repository;

import com.ccapp.ccgo.matching.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTeam_TeamId(Long teamId);
}
