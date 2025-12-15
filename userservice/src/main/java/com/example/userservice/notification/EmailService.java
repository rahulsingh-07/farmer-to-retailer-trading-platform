package com.example.userservice.notification;

import com.example.userservice.entity.Users;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.serviceImp.UserServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
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

    public void sendReminderEmail(String to,String name) {
        String body =
                "Hi " + name + ",\n\n" +
                        "Your registration is approved.\n" +
                        "Please update your password using this link that had been sent to you\n\n" +
                        "Warm regards,\n" +
                        "FarmFresh Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Update Your Password");
        message.setText(body);
        mailSender.send(message);
    }

    public void sendDeletionEmail(String to,String name) {
        String body =
                "Hi " + name + ",\n\n" +
                        "We sent multiple reminders but your password was not updated.\n" +
                        "Your account has been removed.\n"+
                        "If this was a mistake, please register again.\n\n"+
                        "Warm regards,\n" +
                        "FarmFresh Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Account Removed – No Password Update");
        message.setText(body);
        mailSender.send(message);
    }

    public void sendDailyBidNotification(UUID farmerId, String cropName,long dayLeft, BigDecimal highestBid) {
        Users farmer = userRepository.findById(farmerId).orElseThrow();

        String body =
                "Hi " + farmer.getFullName() + ",\n\n" +
                        "🎉 Good news! Your crop "+ cropName +" received a new highest bid:\n" +
                        "💰 Current Highest Bid: ₹"+ highestBid.toPlainString() +"\n"+
                        "Days Remaining:"+ dayLeft +"\n\n"+
                        "If this was a mistake, please register again.\n\n"+
                        "Keep up the good work!\n" +
                        "FarmFresh Team";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(farmer.getEmail());
        message.setSubject("Daily Auction Update - New Highest Bid!");
        message.setText(body);
        mailSender.send(message);
    }
}
