package com.tsystem.web;

import com.tsystem.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ErrorHandling {
    @ResponseStatus(HttpStatus.NOT_FOUND) @ExceptionHandler(NotFoundException.class) String nf(NotFoundException e){return e.getMessage();}
    @ResponseStatus(HttpStatus.FORBIDDEN) @ExceptionHandler(ForbiddenException.class) String fb(ForbiddenException e){return e.getMessage();}
}