package com.vdn.controller;

import com.vdn.exception.UserException;
import com.vdn.payload.dto.UserDTO;
import com.vdn.payload.request.ForgotPasswordRequest;
import com.vdn.payload.request.LoginRequest;
import com.vdn.payload.request.ResetPasswordRequest;
import com.vdn.payload.response.ApiResponse;
import com.vdn.payload.response.AuthResponse;
import com.vdn.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signupHandler(
            @Valid @RequestBody UserDTO req
            ) throws UserException {
        AuthResponse res = authService.signup(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandler(
            @Valid @RequestBody LoginRequest req
    ) throws UserException {
        AuthResponse res = authService.login(req.getEmail(),req.getPassword());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest request
    ) throws UserException {
        authService.createPasswordResetToken(request.getEmail());

        ApiResponse res = new ApiResponse("A Reset link was sent to your email.", true);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestBody ResetPasswordRequest request) throws Exception{
        authService.resetPassword(request.getToken(),request.getPassword());
        ApiResponse res = new ApiResponse("password reset successful.", true);
        return ResponseEntity.ok(res);
    }

}
