package com.example.userservice.util;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Order;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.enums.PaymentStatus;
import com.example.userservice.farmer.NotificationService;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.OrderRepository;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionOrderListenerTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private NotificationService notificationService;
    @InjectMocks private AuctionOrderListener auctionOrderListener;

    private UUID retailerId;
    private Users farmer;
    private Users retailer;
    private Crops crop;
    private Auction auction;
    private AuctionClosedEvent event;

    @BeforeEach
    void setUp() {
        retailerId = UUID.randomUUID();
        farmer = createUser(null, "farmer@example.com", "John Farmer");
        retailer = createUser(retailerId, "retailer@example.com", "Jane Retailer");
        crop = createCrop(farmer, "Wheat");
        auction = createAuction(crop, retailerId, BigDecimal.valueOf(1500));
        event = new AuctionClosedEvent(this, auction);
    }

    @Test
    void autoCreateOrder_shouldCreateOrderAndSendNotifications_happyPath() {
        // Arrange
        when(userRepository.findById(retailerId)).thenReturn(Optional.of(retailer));

        // Act
        auctionOrderListener.autoCreateOrder(event);

        // Assert order creation
        verifyOrderCreation(auction, crop, farmer, retailer, BigDecimal.valueOf(1500));

        // Assert notifications - verify each call separately due to same args issue
        verify(notificationService).notifyFarmerAuctionWon(auction, crop, farmer, retailer, BigDecimal.valueOf(1500));
        verify(notificationService).notifyWinner(auction, crop, farmer, retailer, BigDecimal.valueOf(1500));

        // Assert emails
        verify(emailService).notifyFarmerAuctionWon("farmer@example.com", "John Farmer", "Wheat", BigDecimal.valueOf(1500), "Jane Retailer");
        verify(emailService).notifyWinner("retailer@example.com", "Jane Retailer", "Wheat", BigDecimal.valueOf(1500), "John Farmer");
    }

    @Test
    void autoCreateOrder_shouldThrowException_whenRetailerNotFound() {
        // Arrange
        when(userRepository.findById(retailerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> auctionOrderListener.autoCreateOrder(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Winner not found");

        verifyNoInteractions(orderRepository, notificationService, emailService);
    }

    @Test
    void autoCreateOrder_shouldThrowException_whenAuctionCropNull() {
        // Arrange
        auction.setCrop(null);
        event = new AuctionClosedEvent(this, auction);

        assertThatThrownBy(() -> auctionOrderListener.autoCreateOrder(event))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(orderRepository, notificationService, emailService);
    }

    @Test
    void autoCreateOrder_shouldThrowException_whenCropUserNull() {

        crop.setUser(null);
        auction.setCrop(crop);
        event = new AuctionClosedEvent(this, auction);
        when(userRepository.findById(retailerId)).thenReturn(Optional.of(retailer));

        assertThatThrownBy(() -> auctionOrderListener.autoCreateOrder(event))
                .isInstanceOf(NullPointerException.class);

        verify(userRepository).findById(retailerId);
        verifyNoInteractions(notificationService, emailService);
    }

    @Test
    void autoCreateOrder_shouldHandleNullFarmerEmailGracefully() {
        // Arrange
        farmer.setEmail(null);  // null email
        crop.setUser(farmer);
        auction.setCrop(crop);
        event = new AuctionClosedEvent(this, auction);
        when(userRepository.findById(retailerId)).thenReturn(Optional.of(retailer));

        // Act - Should NOT throw exception
        auctionOrderListener.autoCreateOrder(event);

        // Assert - Order created successfully
        verifyOrderCreation(auction, crop, farmer, retailer, BigDecimal.valueOf(1500));

        // Assert - Notifications still sent (null-safe)
        verify(notificationService).notifyFarmerAuctionWon(auction, crop, farmer, retailer, BigDecimal.valueOf(1500));
        verify(notificationService).notifyWinner(auction, crop, farmer, retailer, BigDecimal.valueOf(1500));

        // Assert - Emails sent with null email (your EmailService handles it)
        verify(emailService).notifyFarmerAuctionWon(null, "John Farmer", "Wheat", BigDecimal.valueOf(1500), "Jane Retailer");
        verify(emailService).notifyWinner("retailer@example.com", "Jane Retailer", "Wheat", BigDecimal.valueOf(1500), "John Farmer");
    }


    @Test
    void autoCreateOrder_shouldHandleZeroBidGracefully() {
        // Arrange
        auction.setCurrentHighestBid(BigDecimal.ZERO);
        event = new AuctionClosedEvent(this, auction);
        when(userRepository.findById(retailerId)).thenReturn(Optional.of(retailer));

        // Act
        auctionOrderListener.autoCreateOrder(event);

        // Assert
        verifyOrderCreation(auction, crop, farmer, retailer, BigDecimal.ZERO);
        verify(notificationService).notifyFarmerAuctionWon(auction, crop, farmer, retailer, BigDecimal.ZERO);
        verify(notificationService).notifyWinner(auction, crop, farmer, retailer, BigDecimal.ZERO);
    }

    @Test
    void autoCreateOrder_shouldHandleNegativeBid() {
        // Arrange
        auction.setCurrentHighestBid(BigDecimal.valueOf(-100));
        event = new AuctionClosedEvent(this, auction);
        when(userRepository.findById(retailerId)).thenReturn(Optional.of(retailer));

        // Act
        auctionOrderListener.autoCreateOrder(event);

        // Assert
        verifyOrderCreation(auction, crop, farmer, retailer, BigDecimal.valueOf(-100));
    }

    @Test
    void autoCreateOrder_shouldHandleNullRetailerFullName() {
        // Arrange
        retailer.setFullName(null);
        when(userRepository.findById(retailerId)).thenReturn(Optional.of(retailer));

        // Act
        auctionOrderListener.autoCreateOrder(event);

        // Assert - Should not crash on null retailer name
        verifyOrderCreation(auction, crop, farmer, retailer, BigDecimal.valueOf(1500));
        verify(emailService).notifyFarmerAuctionWon(anyString(), anyString(), anyString(), any(BigDecimal.class), isNull());
    }

    private Users createUser(UUID id, String email, String fullName) {
        Users user = new Users();
        user.setId(id);
        user.setEmail(email);
        user.setFullName(fullName);
        return user;
    }

    private Crops createCrop(Users farmer, String cropName) {
        Crops cropTest = new Crops();
        cropTest.setUser(farmer);
        cropTest.setCropName(cropName);
        return cropTest;
    }

    private Auction createAuction(Crops crop, UUID retailerId, BigDecimal bid) {
        Auction auctionTest = new Auction();
        auctionTest.setCrop(crop);
        auctionTest.setHighestBidderId(retailerId);
        auctionTest.setCurrentHighestBid(bid);
        return auctionTest;
    }

    private void verifyOrderCreation(Auction auction, Crops crop, Users farmer, Users retailer, BigDecimal price) {
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getAuction()).isSameAs(auction);
        assertThat(savedOrder.getCrop()).isSameAs(crop);
        assertThat(savedOrder.getFarmer()).isSameAs(farmer);
        assertThat(savedOrder.getRetailer()).isSameAs(retailer);
        assertThat(savedOrder.getFinalPrice()).isEqualByComparingTo(price);
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(savedOrder.getCreatedAt()).isNotNull();
    }
}
