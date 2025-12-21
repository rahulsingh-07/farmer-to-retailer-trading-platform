package com.example.userservice.util;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuctionClosedEventTest {

    @Test
    void shouldCreateEventWithAuctionAndSource() {
        // Arrange
        Object source = new Object();
        Auction auction = new Auction();

        // Act
        AuctionClosedEvent event = new AuctionClosedEvent(source, auction);

        // Assert
        assertThat(event).isInstanceOf(ApplicationEvent.class);
        assertThat(event.getAuction()).isSameAs(auction);
        assertThat(event.getSource()).isSameAs(source);
    }

    @Test
    void getCrop_shouldReturnCropFromAuction() {
        // Arrange
        Crops crop = new Crops();
        Auction auction = new Auction();
        auction.setCrop(crop);

        AuctionClosedEvent event = new AuctionClosedEvent(this, auction);

        // Act & Assert
        assertThat(event.getCrop()).isSameAs(crop);
    }

    @Test
    void getFarmer_shouldReturnUserFromCrop() {
        // Arrange
        Users farmer = new Users();
        Crops crop = new Crops();
        crop.setUser(farmer);

        Auction auction = new Auction();
        auction.setCrop(crop);

        AuctionClosedEvent event = new AuctionClosedEvent(this, auction);

        // Act & Assert
        assertThat(event.getFarmer()).isSameAs(farmer);
    }

    @Test
    void getCrop_shouldThrowException_whenAuctionIsNull() {
        // Arrange
        AuctionClosedEvent event = new AuctionClosedEvent(this, null);

        // Act & Assert
        assertThatThrownBy(event::getCrop)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getFarmer_shouldThrowException_whenCropIsNull() {
        // Arrange
        Auction auction = new Auction(); // crop is null
        AuctionClosedEvent event = new AuctionClosedEvent(this, auction);

        // Act & Assert
        assertThatThrownBy(event::getFarmer)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_shouldThrowException_whenSourceIsNull() {
        Auction auction = new Auction();

        assertThatThrownBy(() -> new AuctionClosedEvent(null, auction))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null source");
    }


    @Test
    void getFarmer_shouldReturnNull_whenUserIsNull() {
        Crops crop = new Crops(); // user = null
        Auction auction = new Auction();
        auction.setCrop(crop);

        AuctionClosedEvent event = new AuctionClosedEvent(this, auction);

        assertThat(event.getFarmer()).isNull();
    }

}
