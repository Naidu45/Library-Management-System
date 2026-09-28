package com.vdn.service;

import com.vdn.domain.BookLoanStatus;
import com.vdn.payload.dto.BookLoanDTO;
import com.vdn.payload.request.BookLoanSearchRequest;
import com.vdn.payload.request.CheckinRequest;
import com.vdn.payload.request.CheckoutRequest;
import com.vdn.payload.request.RenewalRequest;
import com.vdn.payload.response.PageResponse;

public interface BookLoanService {

    BookLoanDTO checkoutBook(CheckoutRequest checkoutRequest) throws Exception;

    BookLoanDTO checkoutBookForUser(Long userId, CheckoutRequest checkoutRequest) throws Exception;

    BookLoanDTO checkinBook(CheckinRequest checkinRequest) throws Exception;

    BookLoanDTO renewCheckout(RenewalRequest renewalRequest) throws Exception;

    PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status, int page, int size) throws Exception;

    PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest request);

    int updateOverdueBookLoan();
}
