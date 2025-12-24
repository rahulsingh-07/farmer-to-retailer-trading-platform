package com.example.userservice.mapper;

import com.example.userservice.dto.CropImageResponse;
import com.example.userservice.dto.OrderFarmerResponse;
import com.example.userservice.dto.OrderResponse;
import com.example.userservice.dto.OrderRetailerResponse;
import com.example.userservice.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderFarmerResponse toFarmerDto(Order order){
        List<CropImageResponse> imageResponses = order.getCrop().getImages()
                .stream()
                .map(img -> {
                    CropImageResponse imgRes = new CropImageResponse();
                    imgRes.setImageUrl(img.getImageUrl());
                    return imgRes;
                })
                .toList();
        return OrderFarmerResponse.builder()
                .orderId(order.getId())
                .status(order.getOrderStatus())
                .cropName(order.getCrop().getCropName())
                .category(order.getCrop().getCategory())
                .quantity(order.getCrop().getQuantity())
                .variety(order.getCrop().getVariety())
                .imageUrl(imageResponses)
                .retailerName(order.getRetailer().getFullName())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .build();
    }

    public OrderRetailerResponse toRetailerDto(Order order){
        List<CropImageResponse> imageResponses = order.getCrop().getImages()
                .stream()
                .map(img -> {
                    CropImageResponse imgRes = new CropImageResponse();
                    imgRes.setImageUrl(img.getImageUrl());
                    return imgRes;
                })
                .toList();
        return OrderRetailerResponse.builder()
                .orderId(order.getId())
                .status(order.getOrderStatus())
                .cropName(order.getCrop().getCropName())
                .category(order.getCrop().getCategory())
                .quantity(order.getCrop().getQuantity())
                .variety(order.getCrop().getVariety())
                .imageUrl(imageResponses)
                .farmerName(order.getFarmer().getFullName())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .build();
    }

    public OrderResponse toDto(Order order){
        List<CropImageResponse> imageResponses = order.getCrop().getImages()
                .stream()
                .map(img -> {
                    CropImageResponse imgRes = new CropImageResponse();
                    imgRes.setImageUrl(img.getImageUrl());
                    return imgRes;
                })
                .toList();
        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .paymentStatus(order.getPaymentStatus())

                //farmer details
                .farmerName(order.getFarmer().getFullName())
                .farmerEmail(order.getFarmer().getEmail())
                .farmerPhoneNumber(order.getFarmer().getPhoneNumber())
                .farmerAddress(order.getFarmer().getFarmerDetails().getAddress())

                //retailer details
                .retailerName(order.getRetailer().getFullName())
                .retailerEmail(order.getRetailer().getEmail())
                .retailerPhoneNumber(order.getRetailer().getPhoneNumber())
                .retailerAddress(order.getRetailer().getRetailerDetails().getBusinessAddress())

                //crop details
                .cropName(order.getCrop().getCropName())
                .category(order.getCrop().getCategory())
                .variety(order.getCrop().getVariety())
                .harvestDate(order.getCrop().getHarvestDate())
                .quantity(order.getCrop().getQuantity())
                .unit(order.getCrop().getUnit())
                .pricePerUnit(order.getCrop().getPricePerUnit())
                .location(order.getCrop().getLocation())
                .description(order.getCrop().getDescription())
                .finalPrice(order.getFinalPrice())
                .imageUrl(imageResponses)
                .auctionId(order.getAuction().getId())
                .build();




    }

}
