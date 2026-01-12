package com.example.userservice.util;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Order;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.repository.OrderRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.serviceimp.EmailServiceImp;
import com.example.userservice.serviceimp.NotificationServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionOrderListenerTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailServiceImp emailServiceImp;
    @Mock private NotificationServiceImp notificationServiceImp;

    @InjectMocks
    private AuctionOrderListener listener;

    @Test
    void autoCreateOrder_shouldCreateOrderAndNotifyUsers() {
        // -------- GIVEN --------
        UUID retailerId = UUID.randomUUID();

        Users farmer = new Users();
        farmer.setId(UUID.randomUUID());
        farmer.setEmail("farmer@test.com");
        farmer.setFullName("Farmer One");

        Users retailer = new Users();
        retailer.setId(retailerId);
        retailer.setEmail("retailer@test.com");
        retailer.setFullName("Retailer One");

        Crops crop = new Crops();
        crop.setCropName("Wheat");
        crop.setUser(farmer);

        Auction auction = new Auction();
        auction.setCrop(crop);
        auction.setHighestBidderId(retailerId);
        auction.setCurrentHighestBid(BigDecimal.valueOf(1500));

        AuctionClosedEvent event = new AuctionClosedEvent(this, auction);

        when(userRepository.findById(retailerId))
                .thenReturn(Optional.of(retailer));

        ArgumentCaptor<
                Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        // -------- WHEN --------
        listener.autoCreateOrder(event);

        // -------- THEN --------
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(crop, savedOrder.getCrop());
        assertEquals(farmer, savedOrder.getFarmer());
        assertEquals(retailer, savedOrder.getRetailer());
        assertEquals(BigDecimal.valueOf(1500), savedOrder.getFinalPrice());
        assertEquals(OrderStatus.PENDING, savedOrder.getOrderStatus());
        assertNotNull(savedOrder.getCreatedAt());

        // Notifications
        verify(notificationServiceImp).notifyFarmerAuctionWon(
                auction, crop, farmer, retailer, BigDecimal.valueOf(1500)
        );

        verify(notificationServiceImp).notifyWinner(
                auction, crop, farmer, retailer, BigDecimal.valueOf(1500)
        );

        // Emails
        verify(emailServiceImp).notifyFarmerAuctionWon(
                "farmer@test.com", "Farmer One", "Wheat",
                BigDecimal.valueOf(1500), "Retailer One"
        );

        verify(emailServiceImp).notifyWinner(
                "retailer@test.com", "Retailer One", "Wheat",
                BigDecimal.valueOf(1500), "Farmer One"
        );
    }

    @Test
    void autoCreateOrder_shouldThrowExceptionWhenRetailerNotFound() {
        UUID retailerId = UUID.randomUUID();

        Users farmer = new Users();
        farmer.setId(UUID.randomUUID());

        Crops crop = new Crops();
        crop.setUser(farmer);

        Auction auction = new Auction();
        auction.setCrop(crop);
        auction.setHighestBidderId(retailerId);
        auction.setCurrentHighestBid(BigDecimal.TEN);

        AuctionClosedEvent event = new AuctionClosedEvent(this, auction);

        when(userRepository.findById(retailerId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> listener.autoCreateOrder(event));

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(emailServiceImp);
        verifyNoInteractions(notificationServiceImp);
    }

}
