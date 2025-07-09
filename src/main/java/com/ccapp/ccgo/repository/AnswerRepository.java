package com.ccapp.ccgo.repository;

import com.ccapp.ccgo.matching.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByUser_Id(Long userId);

    List<Answer> findByQuestion_Id(Long questionId);

}
