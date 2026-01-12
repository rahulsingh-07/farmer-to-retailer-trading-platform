package com.example.userservice.serviceimp;

import com.example.userservice.entity.Users;
import com.example.userservice.exception.FileUploadException;
import com.example.userservice.repository.UserRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private UserRepository userRepository;
    @InjectMocks private EmailServiceImp emailService;

    private Users testFarmer;
    private Users testRetailer;
    private UUID farmerId;
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @BeforeEach
    void setUp() {
        farmerId = UUID.randomUUID();
        testFarmer = createTestUser(farmerId, "farmer@example.com", "John Farmer");
        testRetailer = createTestUser(UUID.randomUUID(), "retailer@example.com", "Jane Retailer");
        messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    }

    // === AUCTION NOTIFICATION TESTS ===

    @Test
    void notifyFarmerAuctionWon_shouldSendCorrectEmail() {
        // Arrange
        String cropName = "Wheat";
        BigDecimal price = BigDecimal.valueOf(1500);
        String retailerName = "Jane Retailer";

        // Act
        emailService.notifyFarmerAuctionWon(
                testFarmer.getEmail(), testFarmer.getFullName(), cropName, price, retailerName);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage msg = messageCaptor.getValue();

        assertEmailContent(msg, testFarmer.getEmail(), "Auction Won – Your Crop Has Been Sold",
                testFarmer.getFullName(), cropName, price, retailerName);
    }

    @Test
    void notifyWinner_shouldSendCorrectEmail() {
        // Arrange
        String cropName = "Rice";
        BigDecimal price = BigDecimal.valueOf(2000);
        String farmerName = "John Farmer";

        // Act
        emailService.notifyWinner(testRetailer.getEmail(), testRetailer.getFullName(),
                cropName, price, farmerName);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage msg = messageCaptor.getValue();

        assertEmailContent(msg, testRetailer.getEmail(), "Congratulations – You Won the Auction",
                testRetailer.getFullName(), cropName, price, farmerName);
    }

    @Test
    void notifyFarmerAuctionWon_shouldHandleNullEmailGracefully() {
        // Arrange
        emailService.notifyFarmerAuctionWon(null, "John Farmer", "Wheat",
                BigDecimal.valueOf(1500), "Jane Retailer");

        // FIXED: Cast to SimpleMailMessage
        verify(mailSender).send(argThat((SimpleMailMessage msg) -> {
            Assertions.assertNotNull(msg.getTo());
            return msg.getTo()[0] == null;
        }));
    }

    // === DAILY BID NOTIFICATION TESTS ===

    @Test
    void sendDailyBidNotification_shouldSendCorrectEmail_whenFarmerFound() {
        // Arrange
        String cropName = "Wheat";
        long daysLeft = 2L;
        BigDecimal highestBid = BigDecimal.valueOf(1500.50);

        when(userRepository.findById(farmerId)).thenReturn(Optional.of(testFarmer));

        // Act
        emailService.sendDailyBidNotification(farmerId, cropName, daysLeft, highestBid);

        // Assert
        verify(userRepository).findById(farmerId);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage msg = messageCaptor.getValue();
        Assertions.assertNotNull(msg.getTo());
        assertThat(msg.getTo()[0]).isEqualTo(testFarmer.getEmail());
        assertThat(msg.getSubject()).isEqualTo("Daily Auction Update - New Highest Bid!");
        assertThat(msg.getText()).contains(testFarmer.getFullName())
                .contains(cropName)
                .contains(highestBid.toPlainString())
                .contains(String.valueOf(daysLeft));
    }

    @Test
    void sendDailyBidNotification_shouldThrowException_whenFarmerNotFound() {
        // Arrange
        when(userRepository.findById(farmerId)).thenReturn(Optional.empty());

        // FIXED: Correct exception type
        assertThatThrownBy(() -> emailService.sendDailyBidNotification(farmerId, "Wheat", 2L, BigDecimal.TEN))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No value present");

        verify(userRepository).findById(farmerId);
        verifyNoInteractions(mailSender);
    }


    // === PASSWORD & USER MANAGEMENT TESTS ===

    @Test
    void sendPasswordSetupEmail_shouldBuildCorrectMessage() {
        emailService.sendPasswordSetupEmail("user@example.com", "abc123", "testuser", "Test User");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage msg = messageCaptor.getValue();

        Assertions.assertNotNull(msg.getTo());
        assertThat(msg.getTo()[0]).isEqualTo("user@example.com");
        assertThat(msg.getSubject()).isEqualTo("Set up your password");
        assertThat(msg.getText()).contains("Test User").contains("testuser")
                .contains("http://localhost:5173/set-password?token=abc123");
    }

    @Test
    void sendRejectionNotice_shouldBuildCorrectMessage() {
        emailService.sendRejectionNotice("user@example.com", "Test User");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage msg = messageCaptor.getValue();

        assertThat(msg.getSubject()).isEqualTo("Issue with your documents");
        assertThat(msg.getText()).contains("Test User").contains("could not be verified");
    }

    @Test
    void sendReminderEmail_shouldBuildCorrectMessage() {
        emailService.sendReminderEmail("user@example.com", "Test User");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage msg = messageCaptor.getValue();

        assertThat(msg.getSubject()).isEqualTo("Update Your Password");
        assertThat(msg.getText()).contains("Test User").contains("registration is approved");
    }

    @Test
    void sendDeletionEmail_shouldBuildCorrectMessage() {
        emailService.sendDeletionEmail("user@example.com", "Test User");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage msg = messageCaptor.getValue();

        assertThat(msg.getSubject()).isEqualTo("Account Removed – No Password Update");
        assertThat(msg.getText()).contains("Test User").contains("account has been removed");
    }

    // === EDGE CASES ===

    @Test
    void sendDailyBidNotification_shouldHandleZeroDaysLeft() {
        when(userRepository.findById(farmerId)).thenReturn(Optional.of(testFarmer));

        emailService.sendDailyBidNotification(farmerId, "Wheat", 0L, BigDecimal.ZERO);

        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getText()).contains("0");
    }

    @Test
    void sendDailyBidNotification_shouldHandleNegativeDaysLeft() {
        when(userRepository.findById(farmerId)).thenReturn(Optional.of(testFarmer));

        emailService.sendDailyBidNotification(farmerId, "Rice", -1L, BigDecimal.TEN);

        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getText()).contains("-1");
    }

    @Test
    void allEmailMethods_shouldHandleNullNamesGracefully() {
        // Test null names don't crash any method
        emailService.sendPasswordSetupEmail("test@example.com", "token", "user", null);
        emailService.sendRejectionNotice("test@example.com", null);
        emailService.sendReminderEmail("test@example.com", null);
        emailService.sendDeletionEmail("test@example.com", null);

        verify(mailSender, times(4)).send(any(SimpleMailMessage.class));
    }

    // === OTP & INVOICE ===
    @Test
    void sendOtp_ShouldSendCorrectDeliveryOtpEmail() {
        // Given
        UUID orderId = UUID.randomUUID();
        String otp = "123456";

        // When
        emailService.sendOtp(testRetailer.getEmail(), orderId, otp);

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage msg = messageCaptor.getValue();

        Assertions.assertNotNull(msg.getTo());
        assertEquals(testRetailer.getEmail(), msg.getTo()[0]);
        assertEquals("Delivery OTP for Order " + orderId, msg.getSubject());
        Assertions.assertNotNull(msg.getText());
        assertTrue(msg.getText().contains(orderId.toString()));
        assertTrue(msg.getText().contains("Delivery OTP : " + otp));
        assertTrue(msg.getText().contains("Do not share this OTP"));
    }

    @Test
    void sendInvoiceEmail_ShouldSendInvoiceWithAttachment() {
        String to = "test@example.com";
        String retailerName = "Retailer A";
        byte[] pdfBytes = "dummy pdf".getBytes();
        String invoiceNumber = "INV-001";
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendInvoiceEmail(to,retailerName,pdfBytes,invoiceNumber);
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
    @Test
    void sendInvoiceEmail_throwsCustomExceptionWhenMailFails() {
        String to = "test@example.com";
        String retailerName = "Retailer A";
        byte[] pdfBytes = "dummy pdf".getBytes();
        String invoiceNumber = "INV-001";
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP down"))
                .when(mailSender)
                .send(any(MimeMessage.class));
        assertThrows(
                FileUploadException.class,
                () -> emailService.sendInvoiceEmail(
                        to,
                        retailerName,
                        pdfBytes,
                        invoiceNumber
                )
        );

    }
        // === HELPER METHODS ===

    private Users createTestUser(UUID id, String email, String fullName) {
        Users user = new Users();
        user.setId(id);
        user.setEmail(email);
        user.setFullName(fullName);
        return user;
    }

    private void assertEmailContent(SimpleMailMessage msg, String expectedTo, String expectedSubject,
                                    String expectedName, String expectedCrop, BigDecimal expectedPrice, String expectedOtherName) {
        Assertions.assertNotNull(msg.getTo());
        assertThat(msg.getTo()[0]).isEqualTo(expectedTo);
        assertThat(msg.getSubject()).isEqualTo(expectedSubject);
        assertThat(msg.getText()).contains(expectedName)
                .contains(expectedCrop)
                .contains(expectedPrice.toPlainString())
                .contains(expectedOtherName);
    }

}
