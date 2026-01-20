package com.example.userservice.mapper;

import com.example.userservice.dto.OrderResponse;
import com.example.userservice.entity.CropImage;
import com.example.userservice.entity.Order;
import com.example.userservice.entity.Review;
import com.example.userservice.records.OrderCardDto;
import com.example.userservice.records.ReviewDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {


    public OrderCardDto toResponseDto(Order order){

        return new OrderCardDto(
                order.getId(),
                order.getFarmer().getFullName(),
                order.getRetailer().getFullName(),
                order.getOrderStatus(),
                order.getCrop().getCropName(),
                order.getCreatedAt(),
                order.getCrop().getVariety(),
                order.getCrop().getQuantity(),
                order.getCrop().getImages().get(0).getImageUrl()
        );
    }

    public OrderResponse toDto(Order order, Review review){
        List<String> imageUrls = order.getCrop()
                .getImages()
                .stream()
                .map(CropImage::getImageUrl)
                .toList();
        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getOrderStatus())
                .paymentAt(order.getPaymentAt())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .review(review != null ? ReviewDto.from(review) : null)

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
                .cropType(order.getCrop().getCropType())
                .variety(order.getCrop().getVariety())
                .harvestDate(order.getCrop().getHarvestDate())
                .quantity(order.getCrop().getQuantity())
                .unit(order.getCrop().getUnit())
                .pricePerUnit(order.getCrop().getPricePerUnit())
                .location(order.getCrop().getLocation())
                .description(order.getCrop().getDescription())
                .finalPrice(order.getFinalPrice())
                .imageUrl(imageUrls)
                .build();




    }

}
