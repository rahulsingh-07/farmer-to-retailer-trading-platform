package com.example.userservice.records;

public record AdminStatsDto(
        long totalUsers,
        long totalPendingUsers,
        long totalAdmin,
        long totalApproved
) {
}
