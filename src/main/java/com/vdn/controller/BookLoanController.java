package com.vdn.controller;

import com.vdn.domain.BookLoanStatus;
import com.vdn.payload.dto.BookLoanDTO;
import com.vdn.payload.request.BookLoanSearchRequest;
import com.vdn.payload.request.CheckinRequest;
import com.vdn.payload.request.CheckoutRequest;
import com.vdn.payload.request.RenewalRequest;
import com.vdn.payload.response.ApiResponse;
import com.vdn.payload.response.PageResponse;
import com.vdn.service.BookLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-loans")
public class BookLoanController {

    private final BookLoanService bookLoanService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutBook(@Valid @RequestBody CheckoutRequest checkoutRequest) throws Exception {
        BookLoanDTO bookLoan = bookLoanService.checkoutBook(checkoutRequest);
        return new ResponseEntity<>(bookLoan, HttpStatus.CREATED);
    }

    @PostMapping("/checkout/user/{userId}")
    public ResponseEntity<?> checkoutBookForUser(@PathVariable Long userId,
                                                 @Valid @RequestBody CheckoutRequest checkoutRequest) throws Exception {

        BookLoanDTO bookLoan = bookLoanService.checkoutBookForUser(userId, checkoutRequest);
        return new ResponseEntity<>(bookLoan, HttpStatus.CREATED);
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> checkin(@Valid @RequestBody CheckinRequest checkinRequest) throws Exception {

        BookLoanDTO bookLoan = bookLoanService.checkinBook(checkinRequest);
        return new ResponseEntity<>(bookLoan, HttpStatus.OK);
    }

    @PostMapping("/renew")
    public ResponseEntity<?> renew(@Valid @RequestBody RenewalRequest renewalRequest) throws Exception {

        BookLoanDTO bookLoan = bookLoanService.renewCheckout(renewalRequest);
        return new ResponseEntity<>(bookLoan, HttpStatus.OK);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyBookLoans(
            @RequestParam(required = false)BookLoanStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws Exception {

        PageResponse<BookLoanDTO> bookLoans = bookLoanService.getMyBookLoans(status,page,size);
        return ResponseEntity.ok(bookLoans);
    }


    @PostMapping("/search")
    public ResponseEntity<?> getAllBookLoans(@RequestBody BookLoanSearchRequest searchRequest){

        PageResponse<BookLoanDTO> bookLoans = bookLoanService.getBookLoans(searchRequest);
        return ResponseEntity.ok(bookLoans);
    }

    @PostMapping("/admin/update-overdue")
    public ResponseEntity<?> updateOverdueBookLoans(){

        int updateCount = bookLoanService.updateOverdueBookLoan();
        return ResponseEntity.ok(new ApiResponse("overdue book loans are updated",true));
    }


}
