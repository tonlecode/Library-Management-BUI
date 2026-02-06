package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Sign in");
        model.addAttribute("loginForm", new LoginForm());
        return "pages/login";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute("loginForm") LoginForm form, RedirectAttributes redirectAttributes) {
        if (form.username == null || form.username.isBlank()) {
            redirectAttributes.addFlashAttribute("message", "Please enter a username to continue.");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute("message", "Welcome, " + form.username.trim() + ".");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "Signed out.");
        redirectAttributes.addFlashAttribute("messageType", "info");
        return "redirect:/login";
    }

    public static class LoginForm {
        public String username;
        public String password;
    }
}
