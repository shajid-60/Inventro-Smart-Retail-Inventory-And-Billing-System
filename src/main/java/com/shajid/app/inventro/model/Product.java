package com.shajid.app.inventro.model;

public class Product {
    private Integer id;
    private String name;
    private String category;
    private int stock;
    private double price;
    private double soldPrice;
    private String imagePath;
    private double averageRating;
    private int ratingCount;

    public Product() {}

    public Product(Integer id, String name, String category, int stock, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.stock = stock;
        this.price = price;
        this.soldPrice = price * 1.15;
        this.imagePath = null;
        this.averageRating = 0.0;
        this.ratingCount = 0;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        this.price = price;
        if (soldPrice == 0) {
            this.soldPrice = price * 1.15;
        }
    }

    public double getSoldPrice() { return soldPrice; }
    public void setSoldPrice(double soldPrice) { this.soldPrice = soldPrice; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
}
