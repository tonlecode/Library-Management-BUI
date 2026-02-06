package com.example.demo.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

@ControllerAdvice
public class GlobalViewModel {

    @ModelAttribute("appName")
    public String appName() {
        return "Libris";
    }

    @ModelAttribute("appTagline")
    public String appTagline() {
        return "Library Management";
    }

    @ModelAttribute("demoUser")
    public String demoUser() {
        return "Demo Librarian";
    }

    @ModelAttribute("today")
    public LocalDate today() {
        return LocalDate.now();
    }
}

