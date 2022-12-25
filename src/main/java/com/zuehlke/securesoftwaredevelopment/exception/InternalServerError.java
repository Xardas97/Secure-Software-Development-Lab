package com.zuehlke.securesoftwaredevelopment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerError extends RuntimeException{
    public InternalServerError() { super("The server is experiencing issues. Please be patient."); }
}
