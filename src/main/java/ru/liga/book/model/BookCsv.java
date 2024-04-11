package ru.liga.book.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCsv {
    private Integer id;
    private Long bookId;
    private Integer bestBookId;
    private Integer workId;
    private Integer booksCount;
    private String isbn;
    private Double isbn13;
    private String authors;
    private Double originalPublicationYear;
    private String originalTitle;
    private String title;
    private String languageCode;
    private Double averageRating;
    private Integer ratingsCount;
    private Integer workRatingsCount;
    private Integer workTextReviewsCount;
    private Integer ratings1;
    private Integer ratings2;
    private Integer ratings3;
    private Integer ratings4;
    private Integer ratings5;
    private String imageUrl;
    private String smallImageUrl;
}