package com.example.userservice.repository;

import com.example.userservice.entity.Order;
import com.example.userservice.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    // OrderRepository
    @Query("SELECT o FROM Order o WHERE o.farmer.id = :farmerId AND o.orderStatus = :status")
    List<Order> findPendingOrdersByFarmer(@Param("farmerId") UUID farmerId, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.farmer.id=:farmerId AND o.orderStatus!='DELIVERED'")
    Long countActiveOrdersByFarmer(@Param("farmerId")UUID farmerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.farmer.id = :farmerId AND o.orderStatus = :status")
    Long countOrdersByFarmer(  @Param("farmerId")UUID farmerId,  @Param("status")OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.farmer.id = :farmerId AND o.orderStatus = :status")
    List<Order>  findAllByStatusAndFarmerId(@Param("farmerId")UUID farmerId,@Param("status")OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.farmer.id = :farmerId")
    List<Order>  findAllByFarmerId(@Param("farmerId")UUID farmerId);

    @Query("SELECT o FROM Order o WHERE o.retailer.id = :retailerId AND o.orderStatus = :status")
    List<Order> findAllByStatusAndRetailerId(@Param("retailerId")UUID farmerId,@Param("status")OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.retailer.id = :retailerId")
    List<Order>  findAllByRetailerId(@Param("retailerId")UUID retailerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.retailer.id=:userId AND o.orderStatus='CONFIRMED'")
    Long countConfirmedOrderByRetailerId(UUID userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.retailer.id=:userId AND o.orderStatus='PENDING'")
    Long countNeedConfirmedOrderByRetailerId(UUID userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.retailer.id=:userId AND o.orderStatus='SHIPPED'")
    Long countShippedOrderByRetailerId(UUID userId);

}
