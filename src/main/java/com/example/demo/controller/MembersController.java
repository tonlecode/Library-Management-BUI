package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.model.Loan;
import com.example.demo.model.LoanStatus;
import com.example.demo.model.Member;
import com.example.demo.model.MemberStatus;
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
@RequestMapping("/members")
public class MembersController {

    private final LibraryStore store;

    public MembersController(LibraryStore store) {
        this.store = store;
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    @GetMapping
    public String list(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "status", required = false) String statusStr,
            Model model
    ) {
        MemberStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = MemberStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        List<Member> members = store.listMembers(query, status);

        model.addAttribute("title", "Members");
        model.addAttribute("activeNav", "members");
        model.addAttribute("q", Optional.ofNullable(query).orElse(""));
        model.addAttribute("status", status);
        model.addAttribute("statuses", MemberStatus.values());
        model.addAttribute("members", members);
        return "pages/members/index";
    }

    @GetMapping("/new")
    public String newMember(Model model, HttpServletRequest request) {
        model.addAttribute("title", "New member");
        model.addAttribute("activeNav", "members");
        model.addAttribute("formMode", "create");
        model.addAttribute("actionUrl", "/members");
        model.addAttribute("statuses", MemberStatus.values());
        model.addAttribute("memberForm", MemberForm.empty());
        if (isAjax(request)) {
            return "pages/members/form :: modalContent";
        }
        return "pages/members/form";
    }

    @PostMapping
    public Object create(@Valid @ModelAttribute("memberForm") MemberForm form, BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "New member");
            model.addAttribute("activeNav", "members");
            model.addAttribute("formMode", "create");
            model.addAttribute("actionUrl", "/members"); // Ensure actionUrl is set for the fragment
            model.addAttribute("statuses", MemberStatus.values());
            if (isAjax(request)) {
                return "pages/members/form :: modalContent";
            }
            return "pages/members/form";
        }

        Long id = store.createMember(form);
        
        if (isAjax(request)) {
            return ResponseEntity.ok("SUCCESS");
        }

        redirectAttributes.addFlashAttribute("message", "Member created.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/members/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Member> member = store.findMember(id);
        if (member.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Member not found.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/members";
        }

        List<Loan> allLoans = store.listLoans().stream()
                .filter(l -> l.getMemberId().equals(id))
                .sorted((l1, l2) -> l2.getIssuedOn().compareTo(l1.getIssuedOn()))
                .toList();

        List<Loan> activeLoans = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .toList();

        List<Loan> loanHistory = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.RETURNED)
                .toList();

        Map<Long, Book> booksById = store.listBooks(null, null, null).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        model.addAttribute("title", member.get().getFullName());
        model.addAttribute("activeNav", "members");
        model.addAttribute("member", member.get());
        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("loanHistory", loanHistory);
        model.addAttribute("booksById", booksById);
        return "pages/members/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        Optional<Member> member = store.findMember(id);
        if (member.isEmpty()) {
            if (isAjax(request)) {
                return "fragments/not_found_modal :: modalContent";
            }
            redirectAttributes.addFlashAttribute("message", "Member not found.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/members";
        }

        model.addAttribute("title", "Edit member");
        model.addAttribute("activeNav", "members");
        model.addAttribute("formMode", "edit");
        model.addAttribute("actionUrl", "/members/" + id);
        model.addAttribute("memberId", id);
        model.addAttribute("statuses", MemberStatus.values());
        model.addAttribute("memberForm", MemberForm.from(member.get()));
        if (isAjax(request)) {
            return "pages/members/form :: modalContent";
        }
        return "pages/members/form";
    }

    @PostMapping("/{id}")
    public Object update(@PathVariable("id") Long id, @Valid @ModelAttribute("memberForm") MemberForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Edit member");
            model.addAttribute("activeNav", "members");
            model.addAttribute("formMode", "edit");
            model.addAttribute("actionUrl", "/members/" + id);
            model.addAttribute("memberId", id);
            model.addAttribute("statuses", MemberStatus.values());
            if (isAjax(request)) {
                return "pages/members/form :: modalContent";
            }
            return "pages/members/form";
        }

        if (!store.updateMember(id, form)) {
            if (isAjax(request)) {
                return "fragments/not_found_modal :: modalContent";
            }
            redirectAttributes.addFlashAttribute("message", "Member not found.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/members";
        }

        if (isAjax(request)) {
            return ResponseEntity.ok("SUCCESS");
        }

        redirectAttributes.addFlashAttribute("message", "Member updated.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/members/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (!store.deleteMember(id)) {
            redirectAttributes.addFlashAttribute("message", "Member not found.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/members";
        }

        redirectAttributes.addFlashAttribute("message", "Member deleted.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/members";
    }
}

