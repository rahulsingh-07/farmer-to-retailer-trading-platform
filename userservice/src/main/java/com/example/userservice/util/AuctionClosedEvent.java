package com.example.userservice.util;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
public class AuctionClosedEvent extends ApplicationEvent {
    private final Auction auction;

    public AuctionClosedEvent(Object source,Auction auction) {
        super(source);
        this.auction=auction;
    }

    // Easy accessors for Order creation
    public Crops getCrop() { return auction.getCrop(); }
    public Users getFarmer() { return auction.getCrop().getUser(); }
}
