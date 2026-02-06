package com.example.demo.model;

public class Book {

    private Long id;

    private String title;
    private String author;
    private String category;
    private String isbn;
    private String imageUrl;
    private int year;
    private BookStatus status;

    public Book() {
    }

    public Book(Long id, String title, String author, String category, String isbn, int year, BookStatus status, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.year = year;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public Book(Long id, String title, String author, String category, String isbn, int year, BookStatus status) {
        this(id, title, author, category, isbn, year, status, null);
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }
}
