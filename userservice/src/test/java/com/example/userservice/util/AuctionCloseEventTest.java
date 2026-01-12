package com.example.userservice.util;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class AuctionCloseEventTest {

    @Test
    void constructor_shouldSetSourceAndAuction() {
        Object source = new Object();

        Users farmer = new Users();
        Crops crop = new Crops();
        crop.setUser(farmer);

        Auction auction = new Auction();
        auction.setCrop(crop);

        AuctionClosedEvent event = new AuctionClosedEvent(source, auction);

        assertSame(source, event.getSource());
        assertSame(auction, event.getAuction());
    }

    @Test
    void getCrop_shouldReturnCropFromAuction() {
        Crops crop = new Crops();
        Auction auction = new Auction();
        auction.setCrop(crop);

        AuctionClosedEvent event = new AuctionClosedEvent(this, auction);

        assertSame(crop, event.getCrop());
    }

    @Test
    void getFarmer_shouldReturnFarmerFromCrop() {
        Users farmer = new Users();
        Crops crop = new Crops();
        crop.setUser(farmer);

        Auction auction = new Auction();
        auction.setCrop(crop);

        AuctionClosedEvent event = new AuctionClosedEvent(this, auction);

        assertSame(farmer, event.getFarmer());
    }
}
