package com.student.cafemaster;

public class productData {
    
    private String productID;
    private String productName;
    private String type;
    private Integer stock;
    private Double price;

    public productData(String productID, String productName, String type, Integer stock, Double price) {
        this.productID = productID;
        this.productName = productName;
        this.type = type;
        this.stock = stock;
        this.price = price;
    }

    public String getProductID() {
        return productID;
    }

    public String getProductName() {
        return productName;
    }

    public String getType() {
        return type;
    }

    public Integer getStock() {
        return stock;
    }

    public Double getPrice() {
        return price;
    }
}