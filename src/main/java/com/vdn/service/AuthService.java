package com.vdn.service;

import com.vdn.exception.UserException;
import com.vdn.payload.dto.UserDTO;
import com.vdn.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse login(String username, String password) throws UserException;
    AuthResponse signup(UserDTO req) throws UserException;

    void createPasswordResetToken(String email) throws UserException;
    void resetPassword(String token, String newPassword) throws Exception;
}
