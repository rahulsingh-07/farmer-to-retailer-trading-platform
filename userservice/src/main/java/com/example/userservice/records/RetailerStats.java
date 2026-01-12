package com.example.userservice.records;

public record RetailerStats(
        long needConfirmation,
        long confirmed,
        long shipped,
        long notifications
) {}
