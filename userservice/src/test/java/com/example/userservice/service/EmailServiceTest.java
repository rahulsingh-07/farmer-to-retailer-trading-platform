package com.example.userservice.notification;

import com.example.userservice.entity.Users;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendPasswordSetupEmail_shouldBuildMessageAndSend() {
        String to = "user@example.com";
        String token = "abc123";
        String username = "testuser";
        String fullName = "Test User";

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendPasswordSetupEmail(to, token, username, fullName);

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertArrayEquals(new String[]{to}, msg.getTo());
        assertEquals("Set up your password", msg.getSubject());
        assertNotNull(msg.getText());
        assertTrue(msg.getText().contains("Hi " + fullName));
        assertTrue(msg.getText().contains("Your username: " + username));
        assertTrue(msg.getText().contains("http://localhost:5173/set-password?token=" + token));
    }

    @Test
    void sendRejectionNotice_shouldBuildMessageAndSend() {
        String to = "user@example.com";
        String name = "Test User";

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendRejectionNotice(to, name);

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertArrayEquals(new String[]{to}, msg.getTo());
        assertEquals("Issue with your documents", msg.getSubject());
        assertNotNull(msg.getText());
        assertTrue(msg.getText().contains("Hi " + name));
        assertTrue(msg.getText().contains("could not be verified"));
    }

    @Test
    void sendReminderEmail_shouldBuildMessageAndSend() {
        String to = "user@example.com";
        String name = "Test User";

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendReminderEmail(to, name);

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertArrayEquals(new String[]{to}, msg.getTo());
        assertEquals("Update Your Password", msg.getSubject());
        assertNotNull(msg.getText());
        assertTrue(msg.getText().contains("Hi " + name));
        assertTrue(msg.getText().contains("Your registration is approved"));
    }

    @Test
    void sendDeletionEmail_shouldBuildMessageAndSend() {
        String to = "user@example.com";
        String name = "Test User";

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendDeletionEmail(to, name);

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertArrayEquals(new String[]{to}, msg.getTo());
        assertEquals("Account Removed – No Password Update", msg.getSubject());
        assertNotNull(msg.getText());
        assertTrue(msg.getText().contains("Hi " + name));
        assertTrue(msg.getText().contains("Your account has been removed"));
    }

    @Test
    void sendDailyBidNotification_shouldLoadFarmerAndSendMail() {
        UUID farmerId = UUID.randomUUID();
        String cropName = "Wheat";
        long daysLeft = 2L;
        BigDecimal highestBid = new BigDecimal("1500.50");

        Users farmer = new Users();
        farmer.setId(farmerId);
        farmer.setFullName("Farmer Name");
        farmer.setEmail("farmer@example.com");

        when(userRepository.findById(farmerId)).thenReturn(Optional.of(farmer));

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendDailyBidNotification(farmerId, cropName, daysLeft, highestBid);

        verify(userRepository, times(1)).findById(farmerId);
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertArrayEquals(new String[]{farmer.getEmail()}, msg.getTo());
        assertEquals("Daily Auction Update - New Highest Bid!", msg.getSubject());
        assertNotNull(msg.getText());
        assertTrue(msg.getText().contains("Hi " + farmer.getFullName()));
        assertTrue(msg.getText().contains(cropName));
        assertTrue(msg.getText().contains(highestBid.toPlainString()));
        assertTrue(msg.getText().contains(String.valueOf(daysLeft)));
    }
}
