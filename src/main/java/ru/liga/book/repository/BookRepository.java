package ru.liga.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import ru.liga.book.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("SELECT b FROM Book b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    @EntityGraph(attributePaths = {"reviews"})
    Page<Book> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isbn = :isbn")
    @EntityGraph(attributePaths = {"reviews"})
    Page<Book> findByIsbn(String isbn, Pageable pageable);

    @EntityGraph(attributePaths = {"reviews"})
    Page<Book> findAllByOrderByTitleAsc(Pageable pageable);

    @EntityGraph(attributePaths = {"reviews"})
    Page<Book> findAllByOrderByOriginalPublicationYearDesc(Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"reviews"})
    Page<Book> findAll(@NonNull Pageable pageable);
}