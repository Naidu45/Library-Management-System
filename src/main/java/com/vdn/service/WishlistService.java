package com.vdn.service;

import com.vdn.payload.dto.WishlistDTO;
import com.vdn.payload.response.PageResponse;
import org.springframework.data.domain.PageRequest;

public interface WishlistService {

    WishlistDTO addToWishlist(Long bookId,String notes) throws Exception;

    void removeFromWishlist(Long bookId) throws Exception;

    PageResponse<WishlistDTO> getMYWishlist(int page, int size) throws Exception;

}
