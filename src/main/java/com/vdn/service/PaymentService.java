package com.vdn.service;

import com.vdn.payload.dto.PaymentDTO;
import com.vdn.payload.request.PaymentInitiateRequest;
import com.vdn.payload.request.PaymentVerifyRequest;
import com.vdn.payload.response.PaymentInitiateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentInitiateResponse initiatePayment(PaymentInitiateRequest req) throws Exception;

    PaymentDTO verifyPayment(PaymentVerifyRequest req) throws Exception;

    Page<PaymentDTO> getAllPayments(Pageable pageable);
}
