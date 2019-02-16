package com.avsatum.codegen.model;

public class ErrorResponse extends AbstractResponse {
    private String message = "";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}