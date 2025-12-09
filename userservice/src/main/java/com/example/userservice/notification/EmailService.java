package com.example.userservice.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    public void sendPasswordSetupEmail(String to, String token, String username, String fullName) {
        String link = "http://localhost:5173/set-password?token=" + token;

        String body =
                "Hi " + fullName + ",\n\n" +
                        "Thank you for registering with us.\n" +
                        "Your username: " + username + "\n" +
                        "Please click the link below to set your password:\n" +
                        link + "\n\n" +
                        "If you did not request this, you can safely ignore this email.\n\n" +
                        "Warm regards,\n" +
                        "FarmFresh Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Set up your password");
        message.setText(body);
        mailSender.send(message);
    }
    public void sendRejectionNotice(String to,String name){

        String body =
                "Hi " + name + ",\n\n" +
                        "Thank you for your interest and for taking the time to register.\n" +
                        "Unfortunately, your documents could not be verified.\n" +
                        "Please log in again and upload the correct documents to continue.\n\n" +
                        "If you have any questions, you can reply to this email.\n\n" +
                        "Warm regards,\n" +
                        "FarmFresh Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Issue with your documents");
        message.setText(body);
        mailSender.send(message);

    }
}
