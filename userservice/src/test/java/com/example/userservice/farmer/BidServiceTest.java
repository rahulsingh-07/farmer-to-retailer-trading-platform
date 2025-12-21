package com.example.userservice.farmer;

import com.example.userservice.dto.BidRequest;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Users;
import com.example.userservice.exception.AuctionNotFoundException;
import com.example.userservice.exception.InvalidBidException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.BidRepository;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    void placeBid_successfulBidPlacement() {
        // Arrange
        UUID auctionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Users user = new Users();
        user.setId(userId);
        Auction auction = new Auction();
        auction.setId(auctionId);
        BidRequest request = new BidRequest();
        request.setAmount(new BigDecimal("1100")); // Valid bid

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(auctionRepository.findHighestBid(auctionId)).thenReturn(new BigDecimal("1000")); // Current highest

        // Act
        String result = bidService.placeBid(auctionId, request, userId);

        // Assert
        assertEquals("Bid Place Successfully", result);
        verify(bidRepository, times(1)).save(argThat(bid ->
                bid.getAmount().equals(new BigDecimal("1100")) &&
                        bid.getUser().getId().equals(userId) &&
                        bid.getAuction().getId().equals(auctionId)));
        verify(auctionRepository, times(1)).save(argThat(auc ->
                auc.getCurrentHighestBid().equals(new BigDecimal("1100")) &&
                        auc.getHighestBidderId().equals(userId)));
    }

    @Test
    void placeBid_userNotFound_throwsUserNotFoundException() {
        // Arrange
        UUID auctionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BidRequest request = new BidRequest();
        request.setAmount(new BigDecimal("1000"));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bidService.placeBid(auctionId, request, userId));
        assertEquals("User Not Found", exception.getMessage());
        verifyNoInteractions(bidRepository, auctionRepository);
    }

    @Test
    void placeBid_auctionNotFound_throwsAuctionNotFoundException() {
        // Arrange
        UUID auctionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Users user = new Users();
        user.setId(userId);
        BidRequest request = new BidRequest();
        request.setAmount(new BigDecimal("1000"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.empty());

        // Act & Assert
        AuctionNotFoundException exception = assertThrows(AuctionNotFoundException.class,
                () -> bidService.placeBid(auctionId, request, userId));
        assertEquals("Place Higher bid", exception.getMessage());
        verifyNoInteractions(bidRepository);
    }

    @Test
    void placeBid_invalidBid_throwsInvalidBidException() {
        // Arrange
        UUID auctionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Users user = new Users();
        user.setId(userId);
        Auction auction = new Auction();
        auction.setId(auctionId);
        BidRequest request = new BidRequest();
        request.setAmount(new BigDecimal("1004"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(auctionRepository.findHighestBid(auctionId)).thenReturn(new BigDecimal("1000"));

        // Act & Assert
        InvalidBidException exception = assertThrows(InvalidBidException.class,
                () -> bidService.placeBid(auctionId, request, userId));
        assertEquals("Bid must be higher than current highest bid", exception.getMessage());
        verify(bidRepository, never()).save(any());
        verify(auctionRepository, never()).save(any());
    }

    @Test
    void validateBid_validBid_returnsTrue() {
        // Arrange
        Auction auction = new Auction();
        auction.setId(UUID.randomUUID());
        when(auctionRepository.findHighestBid(auction.getId())).thenReturn(new BigDecimal("1000"));

        boolean result = bidService.validateBid(auction, new BigDecimal("1100"));

        // Assert
        assertTrue(result);
    }

    @Test
    void validateBid_invalidBid_returnsFalse() {
        // Arrange
        Auction auction = new Auction();
        auction.setId(UUID.randomUUID());
        when(auctionRepository.findHighestBid(auction.getId())).thenReturn(new BigDecimal("1000"));

        // 1040 < 1050 (1000 * 1.05)
        boolean result = bidService.validateBid(auction, new BigDecimal("1004"));

        // Assert
        assertFalse(result);
    }

    @Test
    void validateBid_noPreviousBids_minimumBidAllowed() {
        // Arrange
        Auction auction = new Auction();
        auction.setId(UUID.randomUUID());
        when(auctionRepository.findHighestBid(auction.getId())).thenReturn(BigDecimal.ZERO);

        boolean result = bidService.validateBid(auction, new BigDecimal("100"));

        // Assert
        assertTrue(result);
    }
}
