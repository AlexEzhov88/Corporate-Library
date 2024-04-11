package ru.liga.book.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.liga.book.model.Review;

import java.util.List;

public interface ReviewService {
    Review addReview(Review review);

    List<Review> findAllReviews();

    List<Review> findReviewsByBookId(Long bookId);

    Review updateReview(Review review);

    void deleteReview(Long reviewId);

    Page<Review> findReviewsByBookIdAndSortedByRating(Long bookId, Pageable pageable);

}