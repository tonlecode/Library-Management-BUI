package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PagesController {

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("title", "របាយការណ៍");
        model.addAttribute("activeNav", "reports");
        return "pages/reports";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("title", "ការកំណត់");
        model.addAttribute("activeNav", "settings");
        return "pages/settings";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("title", "ប្រភេទសៀវភៅ");
        model.addAttribute("activeNav", "books");
        return "pages/categories";
    }

    @GetMapping("/authors")
    public String authors(Model model) {
        model.addAttribute("title", "អ្នកនិពន្ធ");
        model.addAttribute("activeNav", "books");
        return "pages/authors";
    }

    @GetMapping("/staff")
    public String staff(Model model) {
        model.addAttribute("title", "បុគ្គលិក");
        model.addAttribute("activeNav", "members");
        return "pages/staff";
    }

    @GetMapping("/fines")
    public String fines(Model model) {
        model.addAttribute("title", "ការផាកពិន័យ");
        model.addAttribute("activeNav", "loans");
        return "pages/fines";
    }

    @GetMapping("/reservations")
    public String reservations(Model model) {
        model.addAttribute("title", "ការកក់សៀវភៅ");
        model.addAttribute("activeNav", "loans");
        return "pages/reservations";
    }

    @GetMapping("/scan")
    public String scan(Model model) {
        model.addAttribute("title", "ស្កេន Barcode");
        model.addAttribute("activeNav", "scan");
        return "pages/scan";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("title", "ការជូនដំណឹង");
        return "pages/notifications";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("title", "ប្រវត្តិរូប");
        return "pages/profile";
    }

    @GetMapping("/activity-logs")
    public String activityLogs(Model model) {
        model.addAttribute("title", "កំណត់ត្រាសកម្មភាព");
        model.addAttribute("activeNav", "settings");
        return "pages/activity_logs";
    }

    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("title", "ជំនួយ & ឯកសារ");
        return "pages/help";
    }

    @GetMapping("/returns")
    public String returns(Model model) {
        model.addAttribute("title", "Returns");
        model.addAttribute("icon", "bi-arrow-return-left");
        model.addAttribute("message", "Process book returns here.");
        model.addAttribute("activeNav", "loans");
        return "pages/placeholder";
    }
}

