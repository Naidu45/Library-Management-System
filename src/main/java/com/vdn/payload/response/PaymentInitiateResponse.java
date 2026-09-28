package com.vdn.payload.response;

import com.vdn.domain.PaymentGateway;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInitiateResponse {

    private Long paymentId;

    private PaymentGateway gateway;

    private String transactionId;

    private String razorpayOrderId;

    private Long amount;

    private String description;

    private String checkoutUrl;

    private String message;

    private Boolean success;

}
