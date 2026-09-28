package com.vdn.service;

import com.vdn.payload.dto.BookReviewDTO;
import com.vdn.payload.request.CreateReviewRequest;
import com.vdn.payload.request.UpdateReviewRequest;
import com.vdn.payload.response.PageResponse;

public interface BookReviewService {

    BookReviewDTO createReview(CreateReviewRequest request) throws Exception;

    BookReviewDTO updateReview(Long reviewId, UpdateReviewRequest request) throws Exception;

    void deleteReview(Long reviewId) throws Exception;

    PageResponse<BookReviewDTO> getReviewsByBookId(Long id,int page,int size) throws Exception;
}
