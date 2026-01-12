package com.example.userservice.records;

public record FarmerStats(
        long totalCrops,
        long totalActiveAuction,
        long totalPendingOrders,
        long totalWaitingPayment,
        long totalShippedOrders,
        long totalCompletedDelivery,
        long totalActiveOrders
) {
}
