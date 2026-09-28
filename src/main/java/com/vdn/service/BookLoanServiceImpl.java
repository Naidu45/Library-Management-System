package com.vdn.service;

import com.vdn.domain.BookLoanStatus;
import com.vdn.domain.BookLoanType;
import com.vdn.exception.BookException;
import com.vdn.mapper.BookLoanMapper;
import com.vdn.model.Book;
import com.vdn.model.BookLoan;
import com.vdn.model.User;
import com.vdn.payload.dto.BookLoanDTO;
import com.vdn.payload.dto.SubscriptionDTO;
import com.vdn.payload.request.BookLoanSearchRequest;
import com.vdn.payload.request.CheckinRequest;
import com.vdn.payload.request.CheckoutRequest;
import com.vdn.payload.request.RenewalRequest;
import com.vdn.payload.response.PageResponse;
import com.vdn.repository.BookLoanRepository;
import com.vdn.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookLoanServiceImpl implements BookLoanService {

    private final BookLoanRepository bookLoanRepository;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final BookRepository bookRepository;
    private final BookLoanMapper bookLoanMapper;

    @Override
    public BookLoanDTO checkoutBook(CheckoutRequest checkoutRequest) throws Exception {

        User user = userService.getCurrentUser();

        return checkoutBookForUser(user.getId(),checkoutRequest);
    }

    @Override
    public BookLoanDTO checkoutBookForUser(Long userId, CheckoutRequest checkoutRequest) throws Exception {
        //validate user exist
        User user = userService.findById(userId);

        //validate user has active subscription
        SubscriptionDTO subscription = subscriptionService
                .getUsersActiveSubscription(user.getId());

        //validate book exists and is available
        Book book=bookRepository.findById(checkoutRequest.getBookId())
                .orElseThrow(()->new BookException("book not found with id "+checkoutRequest.getBookId()));

        if(!book.getActive()){
            throw new BookException("book is not active");
        }
        if(book.getAvailableCopies()<=0){
            throw new BookException("book is not available");
        }

        //check if user already has this book checkout
        if(bookLoanRepository.hasActiveCheckout(userId,book.getId())){
            throw new BookException("book already has active checkout");
        }

        //check user's active checkout limit
        long activeCheckouts=bookLoanRepository.countActiveBookLoansByUser(userId);
        int maxBooksAllowed=subscription.getMaxBooksAllowed();

        if(activeCheckouts>=maxBooksAllowed){
            throw new Exception("you have reached your maximum number of books allowed");
        }

        //check for overdue books
        long overdueCount = bookLoanRepository.countOverdueBookLoansByUser(userId);
        if(overdueCount>0){
            throw new Exception("first return old overdue book!");
        }

        //create the book loan
        BookLoan bookLoan=BookLoan
                .builder()
                .user(user)
                .book(book)
                .type(BookLoanType.CHECKOUT)
                .status(BookLoanStatus.CHECKED_OUT)
                .checkoutDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(checkoutRequest.getCheckoutDays()))
                .renewalCount(0)
                .maxRenewals(2)
                .notes(checkoutRequest.getNotes())
                .isOverdue(false)
                .overdueDays(0)
                .build();


        //update book available copies
        book.setAvailableCopies(book.getAvailableCopies()-1);
        bookRepository.save(book);

        // save book loan
        BookLoan savedBookLoan=bookLoanRepository.save(bookLoan);

        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public BookLoanDTO checkinBook(CheckinRequest checkinRequest) throws Exception {

        //validate book loan exist
        BookLoan bookLoan=bookLoanRepository.findById(checkinRequest.getBookLoanId())
                .orElseThrow(()-> new Exception("book loan not found!"));

        //check if already returned
        if(!bookLoan.isActive()){
            throw new BookException("book loan is not active");
        }
        //set return date
        bookLoan.setReturnDate(LocalDate.now());

        BookLoanStatus condition = checkinRequest.getCondition();
        if(condition==null){
            condition=BookLoanStatus.RETURNED;
        }
        bookLoan.setStatus(condition);

        //fine todo
        bookLoan.setOverdueDays(0);
        bookLoan.setIsOverdue(false);
        //
        bookLoan.setNotes("book returned by user");
        //update book availability
        if(condition!=BookLoanStatus.LOST){
            Book book = bookLoan.getBook();
            book.setAvailableCopies(book.getAvailableCopies()+1);
            bookRepository.save(book);

        //    process next reservation todo
        }

        BookLoan savedBookLoan=bookLoanRepository.save(bookLoan);

        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public BookLoanDTO renewCheckout(RenewalRequest renewalRequest) throws Exception {

        //validate book loan exist
        BookLoan bookLoan=bookLoanRepository.findById(renewalRequest.getBookLoanId())
                .orElseThrow(()-> new Exception("book loan not found!"));

        //check if can be renewed
        if(!bookLoan.canRenew()){
            throw new BookException("book cannot be renewable");
        }

        //update due date
        bookLoan.setDueDate(bookLoan.getDueDate()
                .plusDays(renewalRequest.getExtensionDays()));

        bookLoan.setRenewalCount(bookLoan.getRenewalCount()+1);

        bookLoan.setNotes("book renewed by user");

        BookLoan savedBookLoan=bookLoanRepository.save(bookLoan);

        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status, int page, int size) throws Exception {

        User currentUser = userService.getCurrentUser();
        Page<BookLoan> bookLoanPage;

        if(status!=null){
            Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
            bookLoanPage = bookLoanRepository.findByStatusAndUser(
                    status,currentUser,pageable);
        }else{
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            bookLoanPage = bookLoanRepository.findByUserId(currentUser.getId(),pageable);
        }

        return convertToPageResponse(bookLoanPage);
    }

    @Override
    public PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest searchRequest) {

        //Build pageable with sorting,size,etc.
        Pageable pageable = createPageable(
                searchRequest.getPage(),
                searchRequest.getSize(),
                searchRequest.getSortBy(),
                searchRequest.getSortDirection()
        );

        Page<BookLoan> bookLoanPage;

        //Apply filtering logic dynamically
        if(Boolean.TRUE.equals(searchRequest.getOverdueOnly())){
            //Fetch overdue loans
            bookLoanPage = bookLoanRepository.findOverdueBookLoans(LocalDate.now(), pageable);
        }
        else if(searchRequest.getUserId() != null){
            //Fetch loans by specific user
            bookLoanPage = bookLoanRepository.findByUserId(searchRequest.getUserId(), pageable);
        }
        else if(searchRequest.getBookId() != null){
            //Fetch loans by specific book
            bookLoanPage = bookLoanRepository.findByBookId(searchRequest.getBookId(), pageable);
        }
        else if(searchRequest.getStatus() != null){
            //Fetch loans by loan status
            bookLoanPage = bookLoanRepository.findByStatus(searchRequest.getStatus(), pageable);
        }
        else if(searchRequest.getStartDate()!= null && searchRequest.getEndDate() != null){
            //Fetch loans by specific user
            bookLoanPage = bookLoanRepository.findBookLoansByDateRange(
                    searchRequest.getStartDate(),searchRequest.getEndDate(),pageable);
        }
        else{
            bookLoanPage = bookLoanRepository.findAll(pageable);
        }

        //convert entities to DTOs and wrap in response object
        return convertToPageResponse(bookLoanPage);
    }

    @Override
    public int updateOverdueBookLoan() {

        Pageable pageable = PageRequest.of(0, 1000);
        Page<BookLoan> overduePage = bookLoanRepository.findOverdueBookLoans(LocalDate.now(), pageable);

        int updateCount=0;
        for(BookLoan bookLoan:overduePage.getContent()){
            if(bookLoan.getStatus()==BookLoanStatus.CHECKED_OUT){
                bookLoan.setStatus(BookLoanStatus.OVERDUE);
                bookLoan.setIsOverdue(true);

                int overdueDays = calculateOverdueDate(bookLoan.getDueDate(),LocalDate.now());


                bookLoanRepository.save(bookLoan);
                updateCount++;
            }
        }
        return updateCount;
    }

    private Pageable createPageable(int page,int size,String sortBy,String sortDirection){

        size = Math.min(size,100);
        size = Math.max(size,1);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending():Sort.by(sortBy).descending();

        return PageRequest.of(page,size,sort);
    }

    private PageResponse<BookLoanDTO> convertToPageResponse(Page<BookLoan> bookLoanPage){
        List<BookLoanDTO> bookLoanDTOs=bookLoanPage.getContent()
                .stream()
                .map(bookLoanMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                bookLoanDTOs,
                bookLoanPage.getNumber(),
                bookLoanPage.getSize(),
                bookLoanPage.getTotalElements(),
                bookLoanPage.getTotalPages(),
                bookLoanPage.isLast(),
                bookLoanPage.isFirst(),
                bookLoanPage.isEmpty()
        );
    }


    public int calculateOverdueDate(LocalDate dueDate, LocalDate today){
        if(today.isBefore(dueDate) || today.isEqual(dueDate)){
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, today);
    }
}
