package com.example.userservice.util;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Order;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.serviceimp.EmailServiceImp;
import com.example.userservice.serviceimp.NotificationServiceImp;
import com.example.userservice.repository.OrderRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionOrderListener {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository; // For highestBidderId
    private final EmailServiceImp emailServiceImp;
    private final NotificationServiceImp notificationServiceImp;

    @EventListener
    @Transactional
    public void autoCreateOrder(AuctionClosedEvent event) {
        Auction auction = event.getAuction();

        if (auction.getHighestBidderId() == null) {
            throw new IllegalStateException("Auction closed without a winner");
        }

        if (auction.getCrop() == null || auction.getCrop().getUser() == null) {
            throw new IllegalStateException("Auction has invalid crop or farmer");
        }

        Users retailer = getRetailerById(auction.getHighestBidderId());

        Order order = Order.builder()
                .crop(auction.getCrop())
                .farmer(auction.getCrop().getUser())
                .retailer(retailer)
                .finalPrice(auction.getCurrentHighestBid())
                .orderStatus(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        notifyUsers(auction, order, retailer);
    }


    private Users getRetailerById(UUID retailerId) {
        return userRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Winner not found"));
    }

    private void notifyUsers(Auction auction, Order order, Users retailer) {
        BigDecimal price = order.getFinalPrice();

        notificationServiceImp.notifyFarmerAuctionWon(
                auction, order.getCrop(), order.getFarmer(), retailer, price);

        notificationServiceImp.notifyWinner(
                auction, order.getCrop(), order.getFarmer(), retailer, price);

        emailServiceImp.notifyFarmerAuctionWon(
                order.getFarmer().getEmail(),
                order.getFarmer().getFullName(),
                order.getCrop().getCropName(),
                price,
                retailer.getFullName());

        emailServiceImp.notifyWinner(
                retailer.getEmail(),
                retailer.getFullName(),
                order.getCrop().getCropName(),
                price,
                order.getFarmer().getFullName());
    }

}

