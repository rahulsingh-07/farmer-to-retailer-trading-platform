package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chatbot_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(length = 50)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "order_index")
    private Integer orderIndex;
}
