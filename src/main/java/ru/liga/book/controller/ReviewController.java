package ru.liga.book.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.liga.book.model.Review;
import ru.liga.book.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Add a new review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review successfully added",
                    content = @Content(schema = @Schema(implementation = Review.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.addReview(review));
    }

    @Operation(summary = "Find all reviews")
    @ApiResponse(responseCode = "200", description = "Successfully found reviews",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Review.class))))
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<List<Review>> findAllReviews() {
        return ResponseEntity.ok(reviewService.findAllReviews());
    }

    @Operation(summary = "Find reviews by book ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully found reviews for book",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Review.class)))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<List<Review>> findReviewsByBookId(@PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.findReviewsByBookId(bookId));
    }

    @Operation(summary = "Update a review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review successfully updated",
                    content = @Content(schema = @Schema(implementation = Review.class))),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Review> updateReview(@PathVariable Long reviewId, @RequestBody Review review) {
        review.setId(reviewId);
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @Operation(summary = "Delete a review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Find reviews by book ID sorted by rating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully found reviews for book sorted by rating",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Review.class)))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/sorted-by-rating")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Page<Review>> findReviewsByBookIdAndSortedByRating(
            @RequestParam Long bookId,
            @PageableDefault(sort = "rating",
                    direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reviewService.findReviewsByBookIdAndSortedByRating(bookId, pageable));
    }
}