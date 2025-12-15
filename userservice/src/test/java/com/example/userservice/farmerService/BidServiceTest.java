package com.example.userservice.farmerService;

import com.example.userservice.dto.BidRequest;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Bid;
import com.example.userservice.exception.AuctionNotFoundException;
import com.example.userservice.exception.InvalidBidException;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.BidRepository;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BidService bidService;

    @Test
    void placeBid_shouldSaveBidAndUpdateAuction_whenValidBid() {
        UUID auctionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Auction auction = new Auction();
        auction.setId(auctionId);

        BidRequest request = new BidRequest();
        request.setAmount(new BigDecimal("105.00"));

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        // currentHighest = 100 → minIncrement = 105 → amount 105 > 105? No, so choose 106
        when(auctionRepository.findHighestBid(auctionId)).thenReturn(new BigDecimal("100.00"));

        // adjust to valid amount > minIncrement
        request.setAmount(new BigDecimal("106.00"));

        String result = bidService.placeBid(auctionId, request, userId);

        assertEquals("Bid Place Successfully", result);

        ArgumentCaptor<Bid> bidCaptor = ArgumentCaptor.forClass(Bid.class);
        verify(bidRepository, times(1)).save(bidCaptor.capture());
        Bid savedBid = bidCaptor.getValue();

        assertEquals(request.getAmount(), savedBid.getAmount());
        assertEquals(userId, savedBid.getBidderId());
        assertEquals(auction, savedBid.getAuction());
        assertNotNull(savedBid.getCreatedAt());

        assertEquals(request.getAmount(), auction.getCurrentHighestBid());
        assertEquals(userId, auction.getHighestBidderId());
        verify(auctionRepository, times(1)).save(auction);
    }

    @Test
    void placeBid_shouldThrowAuctionNotFoundException_whenAuctionMissing() {
        UUID auctionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        BidRequest request = new BidRequest();
        request.setAmount(new BigDecimal("100.00"));

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.empty());

        AuctionNotFoundException ex = assertThrows(
                AuctionNotFoundException.class,
                () -> bidService.placeBid(auctionId, request, userId)
        );
        assertEquals("Place Higher bid", ex.getMessage());

        verify(bidRepository, never()).save(any());
        verify(auctionRepository, never()).save(any());
    }

    @Test
    void placeBid_shouldThrowInvalidBidException_whenBidNotHighEnough() {
        UUID auctionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Auction auction = new Auction();
        auction.setId(auctionId);

        BidRequest request = new BidRequest();
        request.setAmount(new BigDecimal("102.00")); // will be <= minIncrement

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(auctionRepository.findHighestBid(auctionId)).thenReturn(new BigDecimal("100.00")); // minIncrement = 105

        InvalidBidException ex = assertThrows(
                InvalidBidException.class,
                () -> bidService.placeBid(auctionId, request, userId)
        );
        assertEquals("Bid must be higher than current highest bid", ex.getMessage());

        verify(bidRepository, never()).save(any());
        verify(auctionRepository, never()).save(auction);
    }

    @Test
    void validateBid_shouldReturnTrue_whenAmountGreaterThanMinIncrement() {
        Auction auction = new Auction();
        UUID auctionId = UUID.randomUUID();
        auction.setId(auctionId);

        when(auctionRepository.findHighestBid(auctionId)).thenReturn(new BigDecimal("100.00"));

        boolean result = bidService.validateBid(auction, new BigDecimal("106.00"));

        assertTrue(result);
    }

    @Test
    void validateBid_shouldReturnFalse_whenAmountNotGreaterThanMinIncrement() {
        Auction auction = new Auction();
        UUID auctionId = UUID.randomUUID();
        auction.setId(auctionId);

        when(auctionRepository.findHighestBid(auctionId)).thenReturn(new BigDecimal("100.00")); // minIncrement = 105

        boolean equalToMin = bidService.validateBid(auction, new BigDecimal("105.00"));
        boolean lower = bidService.validateBid(auction, new BigDecimal("104.00"));

        assertFalse(equalToMin);
        assertFalse(lower);
    }
}
