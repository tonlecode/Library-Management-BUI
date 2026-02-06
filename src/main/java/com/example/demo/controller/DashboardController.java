package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.model.Loan;
import com.example.demo.model.Member;
import com.example.demo.service.LibraryStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final LibraryStore store;

    public DashboardController(LibraryStore store) {
        this.store = store;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "Dashboard");
        model.addAttribute("activeNav", "dashboard");

        model.addAttribute("totalBooks", store.countBooks());
        model.addAttribute("availableBooks", store.countAvailableBooks());
        model.addAttribute("totalMembers", store.countMembers());
        model.addAttribute("activeLoans", store.countActiveLoans());
        model.addAttribute("overdueLoansCount", store.countOverdueLoans());

        List<Book> recentBooks = store.recentBooks(5);
        List<Loan> overdueLoans = store.overdueLoans(5);

        // Optimization: Only fetch books/members that are needed for the overdue list
        Map<Long, Book> booksById = overdueLoans.stream()
                .map(Loan::getBookId)
                .distinct()
                .map(store::findBook)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(Book::getId, b -> b));

        Map<Long, Member> membersById = overdueLoans.stream()
                .map(Loan::getMemberId)
                .distinct()
                .map(store::findMember)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(Member::getId, m -> m));

        model.addAttribute("recentBooks", recentBooks);
        model.addAttribute("overdueLoans", overdueLoans);
        model.addAttribute("booksById", booksById);
        model.addAttribute("membersById", membersById);

        LoanTrend trend = LoanTrend.last7Days(store.listLoans(), LocalDate.now());
        model.addAttribute("trend", trend);

        return "pages/dashboard";
    }

    public static class LoanTrend {
        private final List<String> labels;
        private final List<Integer> values;

        private LoanTrend(List<String> labels, List<Integer> values) {
            this.labels = labels;
            this.values = values;
        }

        public List<String> getLabels() {
            return labels;
        }

        public List<Integer> getValues() {
            return values;
        }

        static LoanTrend last7Days(List<Loan> loans, LocalDate today) {
            List<LocalDate> days = List.of(
                    today.minusDays(6),
                    today.minusDays(5),
                    today.minusDays(4),
                    today.minusDays(3),
                    today.minusDays(2),
                    today.minusDays(1),
                    today
            );

            Map<LocalDate, Long> byDay = loans.stream()
                    .collect(Collectors.groupingBy(Loan::getIssuedOn, Collectors.counting()));

            List<String> labels = days.stream().map(d -> d.getMonthValue() + "/" + d.getDayOfMonth()).toList();
            List<Integer> values = days.stream().map(d -> byDay.getOrDefault(d, 0L).intValue()).toList();
            return new LoanTrend(labels, values);
        }
    }
}
