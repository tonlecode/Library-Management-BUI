package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.model.BookStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Year;

public class BookForm {

    @NotBlank
    @Size(max = 140)
    private String title;

    @NotBlank
    @Size(max = 120)
    private String author;

    @NotBlank
    @Size(max = 80)
    private String category;

    @Size(max = 32)
    private String isbn;

    @Size(max = 255)
    private String imageUrl;

    @NotNull
    @Min(1400)
    @Max(3000)
    private Integer year;

    @NotNull
    private BookStatus status;

    public static BookForm empty() {
        BookForm form = new BookForm();
        form.setStatus(BookStatus.AVAILABLE);
        form.setYear(Year.now().getValue());
        return form;
    }

    public static BookForm from(Book book) {
        BookForm form = new BookForm();
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setCategory(book.getCategory());
        form.setIsbn(book.getIsbn());
        form.setImageUrl(book.getImageUrl());
        form.setYear(book.getYear());
        form.setStatus(book.getStatus());
        return form;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }
}

