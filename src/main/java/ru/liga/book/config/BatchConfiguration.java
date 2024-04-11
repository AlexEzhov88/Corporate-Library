package ru.liga.book.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;
import ru.liga.book.model.Book;
import ru.liga.book.model.BookCsv;
import ru.liga.book.model.Review;
import ru.liga.book.model.ReviewCsv;
import ru.liga.book.model.Role;
import ru.liga.book.model.User;
import ru.liga.book.repository.BookRepository;
import ru.liga.book.repository.RoleRepository;
import ru.liga.book.repository.UserRepository;

import java.util.Collections;
import java.util.Optional;

/**
 * Чтобы загрузить все файлы как надо, нужно отключить генерацию Id поля у Book и User
 * Это нужно, чтобы проинициализировать поля нужными Id из файла
 */
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    private final RoleRepository roleRepository;

    @Value("${spring.file.book-input}")
    private String bookInput;

    @Value("${spring.file.review-input}")
    private String reviewInput;

    private final PasswordEncoder passwordEncoder;

    private final BookRepository bookRepository;

    private final UserRepository userRepository;

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public FlatFileItemReader<BookCsv> bookReader() {
        return new FlatFileItemReaderBuilder<BookCsv>()
                .name("BookItemReader")
                .resource(new FileSystemResource(bookInput))
                .delimited()
                .names("id", "book_id", "best_book_id", "work_id", "books_count",
                        "isbn", "isbn13", "authors", "original_publication_year",
                        "original_title", "title", "language_code", "average_rating",
                        "ratings_count", "work_ratings_count", "work_text_reviews_count",
                        "ratings_1", "ratings_2", "ratings_3", "ratings_4", "ratings_5",
                        "image_url", "small_image_url")
                .linesToSkip(1)
                .fieldSetMapper(getFieldSetMapper())
                .build();
    }

    private BeanWrapperFieldSetMapper<BookCsv> getFieldSetMapper() {
        BeanWrapperFieldSetMapper<BookCsv> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(BookCsv.class);
        fieldSetMapper.setCustomEditors(Collections.singletonMap(Double.class,
                new CustomNumberEditor(Double.class, true) {
                    @Override
                    public void setAsText(@NonNull String text) throws IllegalArgumentException {
                        if ("null".equalsIgnoreCase(text)) {
                            setValue(null);
                        } else {
                            super.setAsText(text);
                        }
                    }
                }));
        return fieldSetMapper;
    }

    @Bean
    public JpaItemWriter<Book> writer() {
        JpaItemWriter<Book> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Step bookStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                         JpaItemWriter<Book> writer) {
        return new StepBuilder("bookStep", jobRepository)
                .<BookCsv, Book>chunk(1, transactionManager)
                .reader(bookReader())
                .processor(processor())
                .writer(writer)
                .build();
    }

    @Bean
    public ItemProcessor<BookCsv, Book> processor() {
        return bookCsv -> {
            long bookId = bookCsv.getBookId();

            Optional<String> isbn = Optional.ofNullable(bookCsv.getIsbn()).filter(StringUtils::hasText);
            Optional<Double> isbn13 = parseDoubleOrZero(String.valueOf(bookCsv.getIsbn13()));
            Optional<String> name = Optional.ofNullable(bookCsv.getAuthors()).filter(StringUtils::hasText);
            Optional<Double> originalPublicationYear = parseDoubleOrZero(String.valueOf(bookCsv
                    .getOriginalPublicationYear()));
            Optional<String> originalTitle = Optional.ofNullable(bookCsv.getOriginalTitle())
                    .filter(StringUtils::hasText);
            Optional<String> title = Optional.ofNullable(bookCsv.getTitle()).filter(StringUtils::hasText);
            Optional<String> langCode = Optional.ofNullable(bookCsv.getLanguageCode()).filter(StringUtils::hasText);
            Optional<String> imageUrl = Optional.ofNullable(bookCsv.getImageUrl()).filter(StringUtils::hasText);
            Optional<String> smallImageUrl = Optional.ofNullable(bookCsv.getSmallImageUrl())
                    .filter(StringUtils::hasText);
            Optional<Integer> ratingCount = Optional.ofNullable(bookCsv.getRatingsCount());
            Optional<Double> ratingAvg = parseDoubleOrZero(String.valueOf(bookCsv.getAverageRating()));

            return Book.builder()
                    .id(bookId)
                    .isbn(isbn.orElse(null))
                    .isbn13(isbn13.orElse(0.0))
                    .name(name.orElse(null))
                    .originalPublicationYear(originalPublicationYear.orElse(0.0))
                    .originalTitle(originalTitle.orElse(null))
                    .title(title.orElse(null))
                    .langCode(langCode.orElse(null))
                    .imageUrl(imageUrl.orElse(null))
                    .smallImageUrl(smallImageUrl.orElse(null))
                    .ratingCount(ratingCount.orElse(0))
                    .ratingAvg(ratingAvg.orElse(0.0))
                    .build();
        };
    }

    private Optional<Double> parseDoubleOrZero(String stringValue) {
        try {
            return Optional.ofNullable(stringValue)
                    .filter(str -> !str.trim().equalsIgnoreCase("null") && StringUtils.hasText(str))
                    .map(Double::parseDouble);
        } catch (NumberFormatException e) {
            log.error("Cannot parse to number: {}", stringValue, e);
            return Optional.of(0.0);
        }
    }

    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(@NonNull JobExecution jobExecution) {
                log.info("Before job execution: {}", jobExecution.getJobInstance().getJobName());
            }

            @Override
            public void afterJob(@NonNull JobExecution jobExecution) {
                log.info("After job execution: {}", jobExecution.getStatus());
            }
        };
    }

    @Bean
    public CommandLineRunner importBooksRunner(JobLauncher jobLauncher, Job importBookJob) {
        return args -> {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(importBookJob, jobParameters);
        };
    }

    @Bean
    public FlatFileItemReader<ReviewCsv> reviewReader() {
        BeanWrapperFieldSetMapper<ReviewCsv> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(ReviewCsv.class);

        return new FlatFileItemReaderBuilder<ReviewCsv>()
                .name("ReviewItemReader")
                .resource(new FileSystemResource(reviewInput))
                .delimited()
                .names("book_id", "user_id", "rating")
                .fieldSetMapper(fieldSetMapper)
                .linesToSkip(1)
                .build();
    }

    @Bean
    public JpaItemWriter<Review> reviewWriter() {
        JpaItemWriter<Review> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Step reviewStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           JpaItemWriter<Review> reviewWriter) {
        return new StepBuilder("reviewStep", jobRepository)
                .<ReviewCsv, Review>chunk(1, transactionManager)
                .reader(reviewReader())
                .processor(reviewProcessor())
                .writer(reviewWriter)
                .build();
    }

    @Bean
    public Job importJob(JobRepository jobRepository, JobExecutionListener listener, Step bookStep,
                         Step reviewStep) {
        return new JobBuilder("importJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(bookStep)
                .next(reviewStep)
                .build();
    }

    @Bean
    public ItemProcessor<ReviewCsv, Review> reviewProcessor() {
        return reviewCsv -> {
            Optional<Book> bookOptional = bookRepository.findById(reviewCsv.getBookId());
            if (bookOptional.isEmpty()) {

                return null;
            }

            User user = userRepository.findById(reviewCsv.getUserId())
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .id(reviewCsv.getUserId())
                                .username("User" + reviewCsv.getUserId())
                                .password(passwordEncoder.encode(String.valueOf(reviewCsv.getUserId())))
                                .build();
                        Role userRole = roleRepository.findByName("USER")
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        newUser.setRoles(Collections.singleton(userRole));
                        return userRepository.save(newUser);
                    });

            Review review = new Review();
            review.setBook(bookOptional.get());
            review.setUser(user);
            review.setRating(reviewCsv.getRating());
            return review;
        };
    }

}