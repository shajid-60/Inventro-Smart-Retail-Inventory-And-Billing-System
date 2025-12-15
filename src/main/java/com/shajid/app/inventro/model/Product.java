package com.shajid.app.inventro.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Product {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty category = new SimpleStringProperty(this, "category");
    private final IntegerProperty stock = new SimpleIntegerProperty(this, "stock");
    private final DoubleProperty price = new SimpleDoubleProperty(this, "price");

    public Product() {
    }

    public Product(int id, String name, String category, int stock, double price) {
        this.id.set(id);
        this.name.set(name);
        this.category.set(category);
        this.stock.set(stock);
        this.price.set(price);
    }

    public Product(String name, String category, int stock, double price) {
        this(0, name, category, stock, price);
    }

    // id
    public int getId() {
        return id.get();
    }
    public void setId(int id) {
        this.id.set(id);
    }
    public IntegerProperty idProperty() {
        return id;
    }

    // name
    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public StringProperty nameProperty() {
        return name;
    }

    // category
    public String getCategory() {
        return category.get();
    }
    public void setCategory(String category) {
        this.category.set(category);
    }
    public StringProperty categoryProperty() {
        return category;
    }

    // stock
    public int getStock() {
        return stock.get();
    }
    public void setStock(int stock) {
        this.stock.set(stock);
    }
    public IntegerProperty stockProperty() {
        return stock;
    }

    // price
    public double getPrice() {
        return price.get();
    }
    public void setPrice(double price) {
        this.price.set(price);
    }
    public DoubleProperty priceProperty() {
        return price;
    }
}
