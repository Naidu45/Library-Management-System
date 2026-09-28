package com.vdn.payload.dto;

import com.vdn.domain.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    @NotNull(message = "email is required")
    private String email;

    @NotNull(message = "password is required")
    private String password;
    private String phone;

    @NotNull(message = "fullName is required")
    private String fullName;
    private UserRole role;
    private String username;

    private LocalDateTime lastLogin;
}
