package com.example.userservice.repository;

import com.example.userservice.entity.Order;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    // OrderRepository

    @Query("SELECT COUNT(o) FROM Order o WHERE o.farmer.id=:farmerId AND o.orderStatus!='DELIVERED'")
    long countActiveOrdersByFarmer(@Param("farmerId")UUID farmerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.farmer.id = :farmerId AND o.orderStatus = :status")
    long countOrdersByFarmer(  @Param("farmerId")UUID farmerId,  @Param("status")OrderStatus status);

    @Query("""
    SELECT o FROM Order o
    WHERE o.farmer.id = :userId
      AND (:status IS NULL OR o.orderStatus = :status)
    ORDER BY o.createdAt DESC
""")
    Page<Order> findFarmerOrders(
            Pageable pageable,
            UUID userId,
            OrderStatus status
    );

    @Query("""
    SELECT o FROM Order o
    WHERE o.retailer.id = :userId
      AND (:status IS NULL OR o.orderStatus = :status)
    ORDER BY o.createdAt DESC
""")
    Page<Order> findRetailerOrders(
            Pageable pageable,
            UUID userId,
            OrderStatus status
    );

    @Query("""
    SELECT o FROM Order o
    WHERE (
            (:role = 'FARMER' AND o.farmer.id = :userId)
         OR (:role = 'RETAILER' AND o.retailer.id = :userId)
    )
    ORDER BY o.createdAt DESC
""")
    Page<Order> findAllByUserId(
            Pageable pageable,
            @Param("farmerId")UUID userId,
            @Param("role") UserRole role);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.retailer.id=:userId AND o.orderStatus='CONFIRMED'")
    long countConfirmedOrderByRetailerId(UUID userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.retailer.id=:userId AND o.orderStatus='PENDING'")
    long countNeedConfirmedOrderByRetailerId(UUID userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.retailer.id=:userId AND o.orderStatus='SHIPPED'")
    long countShippedOrderByRetailerId(UUID userId);

    @Query("""
    SELECT o
    FROM Order o
    WHERE o.crop.id = :cropId
      AND o.retailer.id = :retailerId
      AND o.orderStatus <> :status
""")
    Optional<Order> findActiveOrder(
            @Param("cropId") UUID cropId,
            @Param("retailerId") UUID retailerId,
            @Param("status") OrderStatus status
    );
}
