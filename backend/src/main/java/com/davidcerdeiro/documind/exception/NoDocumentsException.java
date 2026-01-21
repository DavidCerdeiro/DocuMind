package com.davidcerdeiro.documind.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No documents related to the question were found.")
public class NoDocumentsException extends RuntimeException{
    public NoDocumentsException(String message) {
        super(message);
    }
}
