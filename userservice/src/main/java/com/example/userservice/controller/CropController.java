package com.example.userservice.controller;

import com.example.userservice.dto.BidRequest;
import com.example.userservice.dto.CropResponse;
import com.example.userservice.entity.Crops;
import com.example.userservice.farmer.AuctionService;
import com.example.userservice.farmer.BidService;
import com.example.userservice.farmer.CropService;
import com.example.userservice.mapper.CropMapper;
import com.example.userservice.serviceimp.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
public class CropController {
    private final AuctionService auctionService;
    private final CropService cropService;
    private final CropMapper cropMapper;
    private final BidService bidService;

    @GetMapping("/crops")
    public ResponseEntity<Page<CropResponse>> getFilteredCrops(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String variety,
            @PageableDefault(size = 12)
            Pageable pageable) {

        Page<Crops> crops = cropService.findAvailable(pageable, category, location, variety);
        Page<CropResponse> response = cropMapper.toResponsePage(crops);

        return ResponseEntity.ok(response);
    }



    @PostMapping("/auctions/{auctionId}/bid")
    public ResponseEntity<Map<String,String>> placeBid(@PathVariable UUID auctionId, @Valid @RequestBody BidRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        String res=bidService.placeBid(auctionId,request,principal.getUserId());
        return ResponseEntity.ok(Map.of("message",res));
    }

    @GetMapping("/crop/{id}")
    public ResponseEntity<CropResponse> getCropDetail(@PathVariable UUID id){

        Crops crop=cropService.getCropDetail(id);
        CropResponse response = cropMapper.toResponse(crop);
        return ResponseEntity.ok(response);
    }

}
