package com.vdn.service;

import com.vdn.domain.FineStatus;
import com.vdn.domain.FineType;
import com.vdn.payload.dto.FineDTO;
import com.vdn.payload.request.CreateFineRequest;
import com.vdn.payload.request.WaiveFineRequest;
import com.vdn.payload.response.PageResponse;
import com.vdn.payload.response.PaymentInitiateResponse;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface FineService {

    FineDTO createFine(CreateFineRequest createFineRequest) throws Exception;

    PaymentInitiateResponse payFine(Long fineId,String transactionId) throws Exception;

    void markFineAsPaid(Long fineId,Long amount,String transactionId) throws Exception;

    FineDTO waiveFine(WaiveFineRequest waiveFineRequest) throws Exception;

    List<FineDTO> getMyFines(FineStatus status, FineType type) throws Exception;

    PageResponse<FineDTO> getAllFines(FineStatus status, FineType type, Long userId,int page,int size);


}
