package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.model.BookStatus;
import com.example.demo.service.LibraryStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/books")
public class BooksController {

    private final LibraryStore store;

    public BooksController(LibraryStore store) {
        this.store = store;
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    @GetMapping
    public String list(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "status", required = false) String statusStr,
            @RequestParam(name = "category", required = false) String category,
            Model model
    ) {
        BookStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = BookStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }
        
        List<Book> books = store.listBooks(query, status, category);

        model.addAttribute("title", "Books");
        model.addAttribute("activeNav", "books");
        model.addAttribute("q", Optional.ofNullable(query).orElse(""));
        model.addAttribute("status", status);
        model.addAttribute("category", Optional.ofNullable(category).orElse(""));
        model.addAttribute("categories", store.listCategories());
        model.addAttribute("statuses", BookStatus.values());
        model.addAttribute("books", books);
        return "pages/books/index";
    }

    @GetMapping("/new")
    public String newBook(Model model, HttpServletRequest request) {
        model.addAttribute("title", "New book");
        model.addAttribute("activeNav", "books");
        model.addAttribute("formMode", "create");
        model.addAttribute("actionUrl", "/books");
        model.addAttribute("statuses", BookStatus.values());
        model.addAttribute("bookForm", BookForm.empty());
        if (isAjax(request)) {
            return "pages/books/form :: modalContent";
        }
        return "pages/books/form";
    }

    @PostMapping
    public Object create(@Valid @ModelAttribute("bookForm") BookForm form, BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "New book");
            model.addAttribute("activeNav", "books");
            model.addAttribute("formMode", "create");
            model.addAttribute("actionUrl", "/books");
            model.addAttribute("statuses", BookStatus.values());
            if (isAjax(request)) {
                return "pages/books/form :: modalContent";
            }
            return "pages/books/form";
        }

        Long id = store.createBook(form);
        
        if (isAjax(request)) {
            return ResponseEntity.ok("SUCCESS");
        }

        redirectAttributes.addFlashAttribute("message", "Book created.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/books/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Book> book = store.findBook(id);
        if (book.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Book not found.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/books";
        }

        model.addAttribute("title", book.get().getTitle());
        model.addAttribute("activeNav", "books");
        model.addAttribute("book", book.get());
        model.addAttribute("activeLoan", store.findActiveLoanForBook(id).orElse(null));
        return "pages/books/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        Optional<Book> book = store.findBook(id);
        if (book.isEmpty()) {
            if (isAjax(request)) {
                return "fragments/not_found_modal :: modalContent";
            }
            redirectAttributes.addFlashAttribute("message", "Book not found.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/books";
        }

        model.addAttribute("title", "Edit book");
        model.addAttribute("activeNav", "books");
        model.addAttribute("formMode", "edit");
        model.addAttribute("actionUrl", "/books/" + id);
        model.addAttribute("bookId", id);
        model.addAttribute("statuses", BookStatus.values());
        model.addAttribute("bookForm", BookForm.from(book.get()));
        if (isAjax(request)) {
            return "pages/books/form :: modalContent";
        }
        return "pages/books/form";
    }

    @PostMapping("/{id}")
    public Object update(@PathVariable("id") Long id, @Valid @ModelAttribute("bookForm") BookForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Edit book");
            model.addAttribute("activeNav", "books");
            model.addAttribute("formMode", "edit");
            model.addAttribute("actionUrl", "/books/" + id);
            model.addAttribute("bookId", id);
            model.addAttribute("statuses", BookStatus.values());
            if (isAjax(request)) {
                return "pages/books/form :: modalContent";
            }
            return "pages/books/form";
        }

        if (!store.updateBook(id, form)) {
             if (isAjax(request)) {
                 return "fragments/not_found_modal :: modalContent";
             }
             redirectAttributes.addFlashAttribute("message", "Book not found.");
             redirectAttributes.addFlashAttribute("messageType", "danger");
             return "redirect:/books";
        }
        
        if (isAjax(request)) {
            return ResponseEntity.ok("SUCCESS");
        }
        
        redirectAttributes.addFlashAttribute("message", "Book updated.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/books/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (!store.deleteBook(id)) {
            redirectAttributes.addFlashAttribute("message", "Book not found.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/books";
        }

        redirectAttributes.addFlashAttribute("message", "Book deleted.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/books";
    }
}
