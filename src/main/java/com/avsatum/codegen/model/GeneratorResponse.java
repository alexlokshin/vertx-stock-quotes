package com.avsatum.codegen.model;

public class GeneratorResponse extends AbstractResponse {
    private StockResponse response = new StockResponse();

    public StockResponse getResponse() {
        return response;
    }

    public void setResponse(StockResponse response) {
        this.response = response;
    }
}