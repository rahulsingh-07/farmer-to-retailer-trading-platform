package com.example.userservice.records;

import com.example.userservice.entity.Review;

public record ReviewDto(
        int rating,
        String comment
) {
    public static ReviewDto from(Review review) {
        return new ReviewDto(
                review.getRating(),
                review.getComment()
        );
    }
}
