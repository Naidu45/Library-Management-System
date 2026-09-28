package com.vdn.exception;

import com.vdn.payload.dto.GenreDTO;

public class GenreException extends Exception{
    public GenreException(String message){
        super(message);
    }
}
