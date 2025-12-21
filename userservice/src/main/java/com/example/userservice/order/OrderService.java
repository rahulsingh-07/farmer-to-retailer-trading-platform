package com.example.userservice.order;

import com.example.userservice.dto.OrderFarmerResponse;
import com.example.userservice.dto.OrderResponse;
import com.example.userservice.dto.OrderRetailerResponse;
import com.example.userservice.entity.Order;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.mapper.OrderMapper;
import com.example.userservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;


    public List<OrderFarmerResponse> getAllFarmerOrder(UUID farmerId, OrderStatus status){
        log.info("getFarmerOrder called: farmerId={}, status={}", farmerId, status);
        if(status!=null && !status.toString().trim().isEmpty()){
            log.info("Using filtered query: findAllByStatusAndFarmerId({}, {})", farmerId, status);
            return orderRepository.findAllByStatusAndFarmerId(farmerId,status)
                    .stream()
                    .map(orderMapper::toFarmerDto)
                    .toList();
        }
        log.info("Using unfiltered query: findAllByFarmerId({})", farmerId);
        return orderRepository.findAllByFarmerId(farmerId)
                .stream()
                .map(orderMapper::toFarmerDto)
                .toList();
    }

    public List<OrderRetailerResponse> getAllRetailerOrder(UUID retailerId, OrderStatus status){
        if(status!=null && !status.toString().trim().isEmpty()){
            return orderRepository.findAllByStatusAndRetailerId(retailerId,status)
                    .stream()
                    .map(orderMapper::toRetailerDto)
                    .toList();
        }
        return orderRepository.findAllByRetailerId(retailerId).stream()
                .map(orderMapper::toRetailerDto)
                .toList();
    }

    public OrderResponse getFarmerOrder(UUID orderId){
        Order order=orderRepository.findById(orderId)
                .orElseThrow(()->new RuntimeException("Order Not Found"));
        return orderMapper.toDto(order);


    }

    public OrderResponse getRetailerOrder(UUID orderId){
        Order order=orderRepository.findById(orderId)
                .orElseThrow(()->new RuntimeException("Order Not Found"));
        return orderMapper.toDto(order);
    }

    public String getOrderConfirmed(UUID orderId) {
        Order order=orderRepository.findById(orderId)
                .orElseThrow(()->new RuntimeException("Order Not Found"));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        orderRepository.save(order);

        return "Order Confirmed Successfully";
    }
}
