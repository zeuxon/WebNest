package com.example.webnest;

public class Course {
    private String title;
    private String description;
    private String imageUrl;
    private boolean isPaid;
    private int price;
    private String material;

    public Course() {}

    public Course(String title, String description, String imageUrl, boolean isPaid, int price, String material) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isPaid = isPaid;
        this.price = price;
        this.material = material;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
}