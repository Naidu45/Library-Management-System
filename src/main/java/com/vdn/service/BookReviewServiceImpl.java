package com.vdn.service;

import com.vdn.domain.BookLoanStatus;
import com.vdn.mapper.BookReviewMapper;
import com.vdn.model.Book;
import com.vdn.model.BookLoan;
import com.vdn.model.BookReview;
import com.vdn.model.User;
import com.vdn.payload.dto.BookReviewDTO;
import com.vdn.payload.request.CreateReviewRequest;
import com.vdn.payload.request.UpdateReviewRequest;
import com.vdn.payload.response.PageResponse;
import com.vdn.repository.BookLoanRepository;
import com.vdn.repository.BookRepository;
import com.vdn.repository.BookReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookReviewServiceImpl implements BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final BookReviewMapper bookReviewMapper;
    private final BookLoanRepository bookLoanRepository;

    @Override
    public BookReviewDTO createReview(CreateReviewRequest request) throws Exception {

        //fetch the logged user
        User user = userService.getCurrentUser();

        //validate book exist
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new Exception("Book not found"));

        //check if user has already reviewed the book
        if(bookReviewRepository.existsByUserIdAndBookId(user.getId(),book.getId())) {
            throw new Exception("you have already reviewed this book");
        }

        //check if user has read the book
        boolean hasReadBook=hasUserReadBook(user.getId(),book.getId());
        if(!hasReadBook) {
            throw new Exception("you have not read this book");
        }

        //create review
        BookReview bookReview = new BookReview();
        bookReview.setUser(user);
        bookReview.setBook(book);
        bookReview.setRating(request.getRating());
        bookReview.setReviewText(request.getReviewText());
        bookReview.setTitle(request.getTitle());
        BookReview savedBookReview = bookReviewRepository.save(bookReview);

        return bookReviewMapper.toDTO(savedBookReview);
    }

    @Override
    public BookReviewDTO updateReview(Long reviewId, UpdateReviewRequest request) throws Exception {

        //fetch the logged user
        User user = userService.getCurrentUser();

        //find the review
        BookReview bookReview=bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Review not found"));

        if(!bookReview.getUser().getId().equals(user.getId())) {
            throw new Exception("you have not reviewed this book");
        }

        //update review
        bookReview.setReviewText(request.getReviewText());
        bookReview.setTitle(request.getTitle());
        bookReview.setRating(request.getRating());

        BookReview savedBookReview = bookReviewRepository.save(bookReview);
        return bookReviewMapper.toDTO(savedBookReview);
    }

    @Override
    public void deleteReview(Long reviewId) throws Exception {
        //fetch the logged user
        User currentUser = userService.getCurrentUser();

        //find the review
        BookReview bookReview=bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Review not found with id: " + reviewId));

        //check if current user is owner of the review
        if(!bookReview.getUser().getId().equals(currentUser.getId())) {
            throw new Exception("you can only delete your own review");
        }

        bookReviewRepository.delete(bookReview);
    }

    @Override
    public PageResponse<BookReviewDTO> getReviewsByBookId(Long id, int page, int size) throws Exception {

        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new Exception("book not found by id"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookReview> reviewPage = bookReviewRepository.findByBook(book, pageable);
        return convertToPageResponse(reviewPage);
    }

    private PageResponse<BookReviewDTO> convertToPageResponse(Page<BookReview> reviewPage) {

        List<BookReviewDTO> reviewDTOs = reviewPage.getContent()
                .stream()
                .map(bookReviewMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                reviewDTOs,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isLast(),
                reviewPage.isFirst(),
                reviewPage.isEmpty()
        );
    }

    private boolean hasUserReadBook(Long userId,Long bookId) {

        List<BookLoan> bookLoans=bookLoanRepository.findByBookId(bookId);

        return bookLoans.stream()
                .anyMatch(loan -> loan.getUser().getId().equals(userId) &&
                        loan.getStatus() == BookLoanStatus.RETURNED);
    }
}
