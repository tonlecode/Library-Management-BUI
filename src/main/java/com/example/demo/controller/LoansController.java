package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.model.Loan;
import com.example.demo.model.LoanStatus;
import com.example.demo.model.Member;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/loans")
public class LoansController {

    private final LibraryStore store;

    public LoansController(LibraryStore store) {
        this.store = store;
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    @GetMapping
    public String list(
            @RequestParam(name = "status", required = false) LoanStatus status,
            Model model
    ) {
        List<Loan> loans = store.listLoans().stream()
                .filter(l -> status == null || l.getStatus() == status)
                .toList();
        
        Map<Long, Book> booksById = store.listBooks(null, null, null).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));
        Map<Long, Member> membersById = store.listMembers(null, null).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        model.addAttribute("title", "Loans");
        model.addAttribute("activeNav", "loans");
        model.addAttribute("loans", loans);
        model.addAttribute("status", status);
        model.addAttribute("statuses", LoanStatus.values());
        model.addAttribute("booksById", booksById);
        model.addAttribute("membersById", membersById);
        return "pages/loans/index";
    }

    @GetMapping("/new")
    public String newLoan(Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        List<Book> availableBooks = store.availableBooks();
        if (availableBooks.isEmpty()) {
            if (isAjax(request)) {
                return "pages/loans/no_books_modal :: modalContent"; // We'll need to create this or handle it
            }
            redirectAttributes.addFlashAttribute("message", "No available books to loan.");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/loans";
        }

        model.addAttribute("title", "Issue loan");
        model.addAttribute("activeNav", "loans");
        model.addAttribute("books", availableBooks);
        model.addAttribute("members", store.listMembers(null, null));
        model.addAttribute("loanForm", IssueLoanForm.empty());
        if (isAjax(request)) {
            return "pages/loans/form :: modalContent";
        }
        return "pages/loans/form";
    }

    @PostMapping
    public Object issue(@Valid @ModelAttribute("loanForm") IssueLoanForm form, BindingResult bindingResult, Model model,
                        RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Issue loan");
            model.addAttribute("activeNav", "loans");
            model.addAttribute("books", store.availableBooks());
            model.addAttribute("members", store.listMembers(null, null));
            if (isAjax(request)) {
                return "pages/loans/form :: modalContent";
            }
            return "pages/loans/form";
        }

        Optional<Long> id = store.issueLoan(form);
        if (id.isEmpty()) {
            if (isAjax(request)) {
                return ResponseEntity.badRequest().body("Could not issue loan. Check availability and selections.");
            }
            redirectAttributes.addFlashAttribute("message", "Could not issue loan. Check availability and selections.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/loans";
        }

        if (isAjax(request)) {
            return ResponseEntity.ok("SUCCESS");
        }

        redirectAttributes.addFlashAttribute("message", "Loan issued.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/loans";
    }

    @PostMapping("/{id}/return")
    public String returnLoan(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (!store.returnLoan(id)) {
            redirectAttributes.addFlashAttribute("message", "Loan not found.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/loans";
        }

        redirectAttributes.addFlashAttribute("message", "Book returned.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/loans";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Loan> loan = store.findLoan(id);
        if (loan.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Loan not found.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/loans";
        }

        Optional<Book> book = store.findBook(loan.get().getBookId());
        Optional<Member> member = store.findMember(loan.get().getMemberId());

        model.addAttribute("title", "Loan #" + loan.get().getId());
        model.addAttribute("activeNav", "loans");
        model.addAttribute("loan", loan.get());
        model.addAttribute("book", book.orElse(null));
        model.addAttribute("member", member.orElse(null));
        model.addAttribute("canReturn", loan.get().getStatus() == LoanStatus.ACTIVE || loan.get().getStatus() == LoanStatus.OVERDUE);
        return "pages/loans/detail";
    }
}

