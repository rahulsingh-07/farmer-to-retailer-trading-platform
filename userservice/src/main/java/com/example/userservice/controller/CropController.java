package com.example.userservice.controller;

import com.example.userservice.dto.BidRequest;
import com.example.userservice.enums.CropType;
import com.example.userservice.records.ApiResponse;
import com.example.userservice.records.BidDto;
import com.example.userservice.records.CropCardDto;
import com.example.userservice.records.RetailerCropDetailDto;
import com.example.userservice.serviceimp.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
public class CropController {
    private final CropServiceImp cropServiceImp;
    private final BidServiceImp bidServiceImp;
    private final OrderServiceImp orderServiceImp;

    // get all crops
    @GetMapping("/crops")
    public ResponseEntity<ApiResponse<Page<CropCardDto>>> getFilteredCrops(
            @RequestParam(required = false)CropType cropType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String variety,
            Pageable pageable) {

        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Crops fetches successfully",
                        cropServiceImp.findAvailableCrops(pageable,cropType, category, location, variety)
                ));
    }


    // place bid
    @PostMapping("/auctions/{auctionId}/bid")
    public ResponseEntity<ApiResponse<Void>> placeBid(@PathVariable UUID auctionId, @Valid @RequestBody BidRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        bidServiceImp.placeBid(auctionId,request,principal.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Bid placed successfully",
                        null
                ));
    }

    // get retailer active auction bid
    @GetMapping("/retailer/bids")
    public ResponseEntity<ApiResponse<List<BidDto>>> getMyBids(@AuthenticationPrincipal CustomUserDetails user) {
        UUID retailerId = user.getUserId();
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "All Current Active Bid",
                        bidServiceImp.getActiveBidsByRetailer(retailerId)
                ));
    }

    // get crop details
    @GetMapping("/crop/{id}")
    public ResponseEntity<ApiResponse<RetailerCropDetailDto>> getCropDetail(@PathVariable UUID id){
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "getting crop by id",
                        cropServiceImp.getCropDetail(id)
                ));
    }

    // instant buy
    @PostMapping("/crop/{id}/buy-now")
    public ResponseEntity<ApiResponse<UUID>> buyNow(@PathVariable UUID id,
                                    @AuthenticationPrincipal CustomUserDetails user) {
        UUID orderId= orderServiceImp.createOrder(id,user.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Order Placed",
                        orderId
                ));
    }

}
