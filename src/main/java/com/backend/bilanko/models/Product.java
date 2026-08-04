package com.backend.bilanko.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.util.List;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String name;
    @Min(0)
    private int quantity;
    @Min(0)
    private double price;

    @ManyToMany
    private List<Category> categories;


    public Product(long id, String name, int quantity, int price, List<Category> categories) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.categories = categories;
    }

    public Product() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
