package com.example.userservice.serviceimp;

import com.example.userservice.dto.BidRequest;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Bid;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.exception.AuctionNotFoundException;
import com.example.userservice.exception.InvalidBidException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.records.BidDto;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.BidRepository;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceImpTest {

    @Mock private BidRepository bidRepository;
    @Mock private AuctionRepository auctionRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks
    private BidServiceImp bidServiceImp;

    private Auction auction;
    private Users user;
    private UUID userId;
    private UUID auctionId;
    private BidRequest bidRequest;
    private Crops crop;
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        auctionId = UUID.randomUUID();

        // User
        user = new Users();
        user.setId(userId);
        // Crop
        crop = new Crops();
        crop.setId(UUID.randomUUID());
        crop.setPricePerUnit(BigDecimal.valueOf(100));
        crop.setCropName("Wheat");
        crop.setVariety("A");
        crop.setQuantity(10.0);
        crop.setUnit("KG");

        // Auction
        auction = new Auction();
        auction.setId(auctionId);
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setCurrentHighestBid(BigDecimal.valueOf(100));
        auction.setCrop(crop);

        // Bid Request
        bidRequest = new BidRequest();
        bidRequest.setAmount(BigDecimal.valueOf(150));
    }


    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> bidServiceImp.placeBid(auctionId, bidRequest, userId));

        verifyNoInteractions(auctionRepository, bidRepository);
    }

    @Test
    void shouldThrowExceptionWhenAuctionNotFound() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(auctionRepository.findById(auctionId))
                .thenReturn(Optional.empty());

        assertThrows(AuctionNotFoundException.class,
                () -> bidServiceImp.placeBid(auctionId, bidRequest, userId));

        verify(bidRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenAuctionNotActive() {
        auction.setStatus(AuctionStatus.CLOSED);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(auctionRepository.findById(auctionId))
                .thenReturn(Optional.of(auction));

        assertThrows(InvalidBidException.class,
                () -> bidServiceImp.placeBid(auctionId, bidRequest, userId));
    }

    @Test
    void shouldThrowExceptionWhenBidIsLowerThanHighest() {
        // Arrange
        bidRequest.setAmount(BigDecimal.valueOf(90)); // Lower than current highest
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(auctionRepository.findHighestBid(auctionId)).thenReturn(BigDecimal.valueOf(100));

        // Act & Assert
        assertThrows(InvalidBidException.class,
                () -> bidServiceImp.placeBid(auctionId, bidRequest, userId));

        // Verify no bid was saved
        verify(bidRepository, never()).save(any());
        verify(auctionRepository, never()).save(any());
    }



    @Test
    void shouldPlaceBidSuccessfully() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(auctionRepository.findById(auctionId))
                .thenReturn(Optional.of(auction));
        when(auctionRepository.findHighestBid(auctionId))
                .thenReturn(BigDecimal.valueOf(100));

        bidServiceImp.placeBid(auctionId, bidRequest, userId);

        verify(bidRepository).save(any(Bid.class));
        verify(auctionRepository).save(auction);

        assertEquals(BigDecimal.valueOf(150), auction.getCurrentHighestBid());
        assertEquals(userId, auction.getHighestBidderId());
    }



    @Test
    void validateBid_shouldReturnTrue_whenAmountIsGreaterThanMinIncrement() {
        when(auctionRepository.findHighestBid(auction.getId()))
                .thenReturn(BigDecimal.valueOf(100));

        boolean result = bidServiceImp.validateBid(auction, BigDecimal.valueOf(106));

        assertTrue(result);
    }

    @Test
    void validateBid_shouldReturnFalse_whenAmountEqualsMinIncrement() {
        when(auctionRepository.findHighestBid(auction.getId()))
                .thenReturn(BigDecimal.valueOf(100));

        boolean result = bidServiceImp.validateBid(auction, BigDecimal.valueOf(105));

        assertFalse(result);
    }

    @Test
    void validateBid_shouldReturnFalse_whenAmountIsLowerThanMinIncrement() {
        when(auctionRepository.findHighestBid(auction.getId()))
                .thenReturn(BigDecimal.valueOf(100));

        boolean result = bidServiceImp.validateBid(auction, BigDecimal.valueOf(104));

        assertFalse(result);
    }

    @Test
    void getActiveBidsByRetailer_shouldReturnMappedBidDtos() {
        UUID retailerId = UUID.randomUUID();
        // Auctions
        Auction auction1 = new Auction();
        auction1.setId(UUID.randomUUID());
        auction1.setCrop(crop);
        auction1.setCurrentHighestBid(BigDecimal.valueOf(100));
        auction1.setStatus(AuctionStatus.ACTIVE);
        auction1.setEndTime(LocalDateTime.now().plusDays(1));

        Auction auction2 = new Auction();
        auction2.setId(UUID.randomUUID());
        auction2.setCrop(crop);
        auction2.setCurrentHighestBid(BigDecimal.valueOf(200));
        auction2.setStatus(AuctionStatus.ACTIVE);
        auction2.setEndTime(LocalDateTime.now().plusDays(2));

        // Bids
        Bid bid1 = new Bid();
        bid1.setId(UUID.randomUUID());
        bid1.setAmount(BigDecimal.valueOf(100));
        bid1.setAuction(auction1);

        Bid bid2 = new Bid();
        bid2.setId(UUID.randomUUID());
        bid2.setAmount(BigDecimal.valueOf(200));
        bid2.setAuction(auction2);

        when(bidRepository.findActiveAllByUserId(retailerId))
                .thenReturn(List.of(bid1, bid2));

        // Act
        List<BidDto> result = bidServiceImp.getActiveBidsByRetailer(retailerId);

        // Assert
        assertEquals(2, result.size());

        assertEquals(bid1.getAmount(), result.get(0).yourBid());
        assertEquals(bid1.getAuction().getCrop().getId(), result.get(0).cropId());
        assertEquals(bid1.getAuction().getCrop().getCropName(), result.get(0).cropName());

        assertEquals(bid2.getAmount(), result.get(1).yourBid());
        assertEquals(bid2.getAuction().getCrop().getId(), result.get(1).cropId());
        assertEquals(bid2.getAuction().getCrop().getCropName(), result.get(1).cropName());
    }


}
