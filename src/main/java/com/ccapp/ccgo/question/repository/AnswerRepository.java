package com.ccapp.ccgo.question.repository;

import com.ccapp.ccgo.matching.domain.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByUser_Id(Long userId);

    List<Answer> findByQuestionId(Long questionId);

    Optional<Answer> findByUser_IdAndQuestionId(Long id, Long id1);
}
