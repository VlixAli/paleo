package com.VlixAli.paleo.handler;

import com.VlixAli.paleo.dto.response.ErrorResponse;
import com.VlixAli.paleo.exception.EventNotFoundException;
import com.VlixAli.paleo.exception.EventNotJoinableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentException(MethodArgumentNotValidException exception){
       Map<String, String> errorMap = new HashMap<>();
       exception.getBindingResult().getFieldErrors().forEach(error -> errorMap.put(error.getField(), error.getDefaultMessage()));

       return errorMap;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException exception){
        return ErrorResponse.builder()
                .status("failed")
                .messages(List.of(exception.getMessage()))
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEventNotFoundException(EventNotFoundException exception){
        return ErrorResponse.builder()
                .status("failed")
                .messages(List.of(exception.getMessage()))
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
    }

    @ExceptionHandler(EventNotJoinableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEventNotJoinableException(EventNotJoinableException exception){
        return ErrorResponse.builder()
                .status("failed")
                .messages(List.of(exception.getMessage()))
                .statusCode(HttpStatus.CONFLICT.value())
                .build();
    }

}
