package com.vdn.service;

import com.vdn.model.User;
import com.vdn.payload.dto.UserDTO;

import java.util.List;

public interface UserService {

    public User getCurrentUser() throws Exception;
    public List<UserDTO> getAllUsers();
    User findById(Long id) throws Exception;

}
