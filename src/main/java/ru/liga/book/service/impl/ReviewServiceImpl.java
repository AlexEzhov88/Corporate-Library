package ru.liga.book.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.liga.book.exception.ReviewNotFoundException;
import ru.liga.book.model.Review;
import ru.liga.book.repository.ReviewRepository;
import ru.liga.book.service.ReviewService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    public List<Review> findReviewsByBookId(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Override
    public Review updateReview(Review review) {
        Review existingReview = reviewRepository.findById(review.getId())
                .orElseThrow(() -> new ReviewNotFoundException("Review with ID " + review.getId() + " not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!existingReview.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("User not authorized to update this review");
        }

        if (review.getUser() == null) {
            review.setUser(existingReview.getUser());
        }

        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review with ID " + reviewId + " not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!existingReview.getUser().getUsername().equals(currentUsername) && !authentication
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new AccessDeniedException("User not authorized to delete this review");
        }

        reviewRepository.deleteById(reviewId);
    }

    @Override
    public Page<Review> findReviewsByBookIdAndSortedByRating(Long bookId, Pageable pageable) {
        return reviewRepository.findByBookIdOrderByRatingDesc(bookId, pageable);
    }

}