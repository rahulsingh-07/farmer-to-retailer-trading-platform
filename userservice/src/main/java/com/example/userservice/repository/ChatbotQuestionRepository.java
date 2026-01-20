package com.example.userservice.repository;

import com.example.userservice.entity.ChatbotQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ChatbotQuestionRepository extends JpaRepository<ChatbotQuestion,Long> {
    List<ChatbotQuestion> findByRoleAndLanguageAndIsActiveOrderByOrderIndex(String upperCase, String lang, boolean b);
}
