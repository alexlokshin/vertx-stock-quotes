package com.avsatum.codegen.model;

public class StockResponse extends AbstractResponse {
    private String symbol;
    private float price;

    public String getSymbol() {
        return symbol;
    }
    
    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}