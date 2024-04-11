package ru.liga.book.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.liga.book.model.Book;

import java.util.Optional;

public interface BookService {
    Page<Book> findAllBooks(Pageable pageable);

    Optional<Book> findBookById(Long id);

    Book saveBook(Book book);

    Book updateBook(Long id, Book bookDetails);

    void deleteBook(Long id);

    Page<Book> findBooksByTitle(String title, Pageable pageable);

    Page<Book> findBooksByIsbn(String isbn, Pageable pageable);

    Page<Book> findAllBooksSortedByTitle(Pageable pageable);

    Page<Book> findAllBooksSortedByPublicationYear(Pageable pageable);
}