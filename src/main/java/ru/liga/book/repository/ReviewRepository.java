package ru.liga.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.liga.book.model.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = {"book", "user"})
    List<Review> findByBookId(Long bookId);

    Page<Review> findByBookIdOrderByRatingDesc(Long bookId, Pageable pageable);
}