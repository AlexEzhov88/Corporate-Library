package ru.liga.book.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.liga.book.exception.BookNotFoundException;
import ru.liga.book.model.Book;
import ru.liga.book.repository.BookRepository;
import ru.liga.book.service.BookService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final String BOOK_NOT_FOUND = "Book with ID %d not found";
    private static final String BOOKS_NOT_FOUND_BY_TITLE = "No books found with title containing '%s'";
    private static final String BOOKS_NOT_FOUND_BY_ISBN = "No books found with ISBN '%s'";
    private static final String BOOK_ALREADY_EXISTS = "Book with ISBN %s already exists";

    private final BookRepository bookRepository;

    @Override
    public Page<Book> findAllBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findAll(pageable);
        if (books.isEmpty()) {
            throw new BookNotFoundException("No books found");
        }
        return books;
    }

    @Override
    public Optional<Book> findBookById(Long id) {
        return Optional.ofNullable(bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(String.format(BOOK_NOT_FOUND, id))));
    }

    @Override
    public Book saveBook(Book book) {
        bookRepository.findByIsbn(book.getIsbn(), Pageable.unpaged()).stream()
                .findFirst()
                .ifPresent(b -> {
                    throw new IllegalArgumentException(String.format(BOOK_ALREADY_EXISTS, book.getIsbn()));
                });
        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(Long id, Book bookDetails) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(String.format(BOOK_NOT_FOUND, id)));
        updateBookFields(existingBook, bookDetails);
        return bookRepository.save(existingBook);
    }

    private void updateBookFields(Book existingBook, Book bookDetails) {
        existingBook.setIsbn(bookDetails.getIsbn());
        existingBook.setIsbn13(bookDetails.getIsbn13());
        existingBook.setName(bookDetails.getName());
        existingBook.setOriginalPublicationYear(bookDetails.getOriginalPublicationYear());
        existingBook.setOriginalTitle(bookDetails.getOriginalTitle());
        existingBook.setTitle(bookDetails.getTitle());
        existingBook.setLangCode(bookDetails.getLangCode());
        existingBook.setImageUrl(bookDetails.getImageUrl());
        existingBook.setSmallImageUrl(bookDetails.getSmallImageUrl());
        existingBook.setRatingCount(bookDetails.getRatingCount());
        existingBook.setRatingAvg(bookDetails.getRatingAvg());
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(String.format(BOOK_NOT_FOUND, id));
        }
        bookRepository.deleteById(id);
    }

    @Override
    public Page<Book> findBooksByTitle(String title, Pageable pageable) {
        Page<Book> books = bookRepository.findByNameContainingIgnoreCase(title, pageable);
        if (books.isEmpty()) {
            throw new BookNotFoundException(String.format(BOOKS_NOT_FOUND_BY_TITLE, title));
        }
        return books;
    }

    @Override
    public Page<Book> findBooksByIsbn(String isbn, Pageable pageable) {
        Page<Book> books = bookRepository.findByIsbn(isbn, pageable);
        if (books.isEmpty()) {
            throw new BookNotFoundException(String.format(BOOKS_NOT_FOUND_BY_ISBN, isbn));
        }
        return books;
    }

    @Override
    public Page<Book> findAllBooksSortedByTitle(Pageable pageable) {
        Page<Book> books = bookRepository.findAllByOrderByTitleAsc(pageable);
        if (books.isEmpty()) {
            throw new BookNotFoundException("No books found when sorting by title");
        }
        return books;
    }

    @Override
    public Page<Book> findAllBooksSortedByPublicationYear(Pageable pageable) {
        Page<Book> books = bookRepository.findAllByOrderByOriginalPublicationYearDesc(pageable);
        if (books.isEmpty()) {
            throw new BookNotFoundException("No books found when sorting by original publication year");
        }
        return books;
    }
}