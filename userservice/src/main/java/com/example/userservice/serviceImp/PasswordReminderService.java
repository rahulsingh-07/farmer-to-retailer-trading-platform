package com.example.userservice.serviceImp;

import com.example.userservice.entity.Users;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordReminderService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 10 * * *") // daily at 10 AM
    public void runReminderTask(){
        List<Users> users = userRepository.findByUpdatePasswordRequired(true);

        for (Users user : users) {

            int attempts = user.getPasswordResetAttempts();

            if (attempts >= 3) {
                emailService.sendDeletionEmail(user.getEmail(),user.getFullName());
                userRepository.delete(user);
                continue;
            }

            emailService.sendReminderEmail(user.getEmail(),user.getFullName());

            user.setPasswordResetAttempts(attempts + 1);
            userRepository.save(user);
        }
    }
}
