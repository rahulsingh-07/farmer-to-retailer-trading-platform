package com.example.userservice.serviceimp;

import com.example.userservice.entity.Users;
import com.example.userservice.exception.FileUploadException;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImp implements EmailService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void notifyFarmerAuctionWon(String farmerEmail,String farmerName,String cropName,BigDecimal price,String retailerName) {

        String body =
                "Hi " + farmerName + ",\n\n" +
                        "Good news! Your auction for crop \"" + cropName + "\" has ended successfully.\n" +
                        "Winner (Retailer): " + retailerName + "\n" +
                        "Final Selling Price: ₹" + price.toPlainString() + "\n\n" +
                        "You can now contact the retailer to finalize delivery and payment.\n\n" +
                        "Warm regards,\n" +
                        "FarmFresh Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(farmerEmail);
        message.setSubject("Auction Won – Your Crop Has Been Sold");
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void notifyWinner( String retailerEmail,String retailerName,String cropName,BigDecimal price,String farmerName) {

        String body =
                "Hi " + retailerName + ",\n\n" +
                        "Congratulations! You have won the auction for crop \"" + cropName + "\".\n" +
                        "Final Purchase Price: ₹" + price.toPlainString() + "\n" +
                        "Farmer: " + farmerName + "\n\n" +
                        "Please contact the farmer to arrange payment and delivery.\n\n" +
                        "Warm regards,\n" +
                        "FarmFresh Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(retailerEmail);
        message.setSubject("Congratulations – You Won the Auction");
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendOtp(String retailerEmail,UUID orderId,String otp){
        String body = """
        Dear Retailer,

        Your order has been successfully marked as shipped.

        Please share the following Delivery OTP with the farmer\s
        at the time of delivery to confirm completion of the order.

        Order ID : %s
        Delivery OTP : %s

        ⚠ Do not share this OTP with anyone other than the farmer.
        This OTP is required to confirm delivery.

        Thank you for using our platform.

        Regards,
        Farmer Trading Platform Team
       \s""".formatted(orderId, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(retailerEmail);
        message.setSubject("Delivery OTP for Order " + orderId);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendInvoiceEmail(String to,String retailerName,byte[] pdfBytes,String invoiceNumber) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Invoice " + invoiceNumber + " – FarmFresh");

            String body =
                    "Hi " + retailerName + ",<br><br>" +
                            "Your payment has been successfully received.<br>" +
                            "Please find your invoice attached.<br><br>" +
                            "<b>Invoice Number:</b> " + invoiceNumber + "<br><br>" +
                            "Warm regards,<br>" +
                            "FarmFresh Team";

            helper.setText(body, true);

            helper.addAttachment(
                    invoiceNumber + ".pdf",
                    new ByteArrayResource(pdfBytes)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new FileUploadException("Failed to send invoice email", e);
        }
    }

}
