package com.example.demo.service;

import com.example.demo.controller.BookForm;
import com.example.demo.controller.IssueLoanForm;
import com.example.demo.controller.MemberForm;
import com.example.demo.model.*;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.LoanRepository;
import com.example.demo.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LibraryStore {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public LibraryStore(BookRepository bookRepository, MemberRepository memberRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    @PostConstruct
    public void init() {
        if (bookRepository.count() == 0) {
            seed();
        }
    }

    public List<Book> listBooks(String query, BookStatus status, String category) {
        String q = normalize(query);
        String cat = normalize(category);

        return bookRepository.findAll().stream()
                .filter(book -> q.isBlank() || matchesBook(book, q))
                .filter(book -> status == null || book.getStatus() == status)
                .filter(book -> cat.isBlank() || normalize(book.getCategory()).equals(cat))
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<String> listCategories() {
        return bookRepository.findAll().stream()
                .map(Book::getCategory)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public Optional<Book> findBook(Long id) {
        return bookRepository.findById(id);
    }

    public Long createBook(BookForm form) {
        Book book = new Book(
                null, 
                form.getTitle().trim(),
                form.getAuthor().trim(),
                form.getCategory().trim(),
                Optional.ofNullable(form.getIsbn()).orElse("").trim(),
                form.getYear(),
                form.getStatus(),
                Optional.ofNullable(form.getImageUrl()).orElse("").trim()
        );
        Book saved = bookRepository.save(book);
        return saved.getId();
    }

    public boolean updateBook(Long id, BookForm form) {
        return bookRepository.findById(id).map(existing -> {
            existing.setTitle(form.getTitle().trim());
            existing.setAuthor(form.getAuthor().trim());
            existing.setCategory(form.getCategory().trim());
            existing.setIsbn(Optional.ofNullable(form.getIsbn()).orElse("").trim());
            existing.setYear(form.getYear());
            existing.setStatus(form.getStatus());
            existing.setImageUrl(Optional.ofNullable(form.getImageUrl()).orElse("").trim());
            bookRepository.save(existing);
            return true;
        }).orElse(false);
    }

    public boolean deleteBook(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Member> listMembers(String query, MemberStatus status) {
        String q = normalize(query);
        return memberRepository.findAll().stream()
                .filter(member -> q.isBlank() || matchesMember(member, q))
                .filter(member -> status == null || member.getStatus() == status)
                .sorted(Comparator.comparing(Member::getFullName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Optional<Member> findMember(Long id) {
        return memberRepository.findById(id);
    }

    public Long createMember(MemberForm form) {
        Member member = new Member(
                null,
                form.getFullName().trim(),
                form.getEmail().trim(),
                Optional.ofNullable(form.getPhone()).orElse("").trim(),
                form.getMemberSince(),
                form.getStatus()
        );
        Member saved = memberRepository.save(member);
        return saved.getId();
    }

    public boolean updateMember(Long id, MemberForm form) {
        return memberRepository.findById(id).map(existing -> {
            existing.setFullName(form.getFullName().trim());
            existing.setEmail(form.getEmail().trim());
            existing.setPhone(Optional.ofNullable(form.getPhone()).orElse("").trim());
            existing.setMemberSince(form.getMemberSince());
            existing.setStatus(form.getStatus());
            memberRepository.save(existing);
            return true;
        }).orElse(false);
    }

    public boolean deleteMember(Long id) {
        if (memberRepository.existsById(id)) {
            memberRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Loan> listLoans() {
        refreshOverdues();
        return loanRepository.findAll().stream()
                .sorted(Comparator.comparing(Loan::getIssuedOn).reversed())
                .collect(Collectors.toList());
    }

    public Optional<Loan> findLoan(Long id) {
        refreshOverdues();
        return loanRepository.findById(id);
    }

    public Optional<Loan> findActiveLoanForBook(Long bookId) {
        refreshOverdues();
        return loanRepository.findAll().stream()
                .filter(l -> l.getBookId().equals(bookId))
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .findFirst();
    }

    public Optional<Long> issueLoan(IssueLoanForm form) {
        Optional<Book> bookOpt = bookRepository.findById(form.getBookId());
        Optional<Member> memberOpt = memberRepository.findById(form.getMemberId());
        
        if (bookOpt.isEmpty() || memberOpt.isEmpty()) {
            return Optional.empty();
        }
        Book book = bookOpt.get();
        Member member = memberOpt.get();

        if (book.getStatus() != BookStatus.AVAILABLE) {
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();
        Loan loan = new Loan(null, book.getId(), member.getId(), today, form.getDueOn(), null, LoanStatus.ACTIVE);
        Loan savedLoan = loanRepository.save(loan);
        
        book.setStatus(BookStatus.CHECKED_OUT);
        bookRepository.save(book);
        
        return Optional.of(savedLoan.getId());
    }

    public boolean returnLoan(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            return false;
        }
        Loan loan = loanOpt.get();
        if (loan.getStatus() == LoanStatus.RETURNED) {
            return true;
        }

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedOn(LocalDate.now());
        loanRepository.save(loan);

        bookRepository.findById(loan.getBookId()).ifPresent(book -> {
            if (book.getStatus() == BookStatus.CHECKED_OUT) {
                book.setStatus(BookStatus.AVAILABLE);
                bookRepository.save(book);
            }
        });
        return true;
    }

    public long countBooks() {
        return bookRepository.count();
    }

    public long countAvailableBooks() {
        return bookRepository.findAll().stream().filter(b -> b.getStatus() == BookStatus.AVAILABLE).count();
    }

    public long countMembers() {
        return memberRepository.count();
    }

    public long countActiveLoans() {
        refreshOverdues();
        return loanRepository.findAll().stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .count();
    }

    public long countOverdueLoans() {
        refreshOverdues();
        return loanRepository.findAll().stream()
                .filter(l -> l.getStatus() == LoanStatus.OVERDUE)
                .count();
    }

    public long countMonthlyLoans() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return loanRepository.findAll().stream()
                .filter(l -> l.getIssuedOn() != null && !l.getIssuedOn().isBefore(startOfMonth))
                .count();
    }

    public long countNewMembers() {
         LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
         return memberRepository.findAll().stream()
                .filter(m -> m.getMemberSince() != null && !m.getMemberSince().isBefore(startOfMonth))
                .count();
    }

    public List<Map<String, Object>> getTopBooks(int limit) {
        Map<Long, Long> loanCounts = loanRepository.findAll().stream()
                .collect(Collectors.groupingBy(Loan::getBookId, Collectors.counting()));

        if (loanCounts.isEmpty()) {
            return Collections.emptyList();
        }

        long maxCount = loanCounts.values().stream().max(Long::compare).orElse(1L);

        return loanCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Optional<Book> book = bookRepository.findById(entry.getKey());
                    if (book.isEmpty()) return null;

                    Map<String, Object> map = new HashMap<>();
                    map.put("title", book.get().getTitle());
                    map.put("author", book.get().getAuthor());
                    map.put("count", entry.getValue());
                    map.put("percent", (entry.getValue() * 100) / maxCount);
                    return map;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Book> recentBooks(int limit) {
        return bookRepository.findAll().stream()
                .sorted(Comparator.comparing(Book::getId).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Loan> overdueLoans(int limit) {
        refreshOverdues();
        return loanRepository.findAll().stream()
                .filter(l -> l.getStatus() == LoanStatus.OVERDUE)
                .sorted(Comparator.comparing(Loan::getDueOn))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Book> availableBooks() {
        return bookRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookStatus.AVAILABLE)
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private void refreshOverdues() {
        LocalDate today = LocalDate.now();
        List<Loan> loans = loanRepository.findAll();
        for (Loan loan : loans) {
            if ((loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.OVERDUE)
                    && loan.getDueOn() != null
                    && loan.getDueOn().isBefore(today)) {
                if (loan.getStatus() != LoanStatus.OVERDUE) {
                    loan.setStatus(LoanStatus.OVERDUE);
                    loanRepository.save(loan);
                }
            }
        }
    }

    private static boolean matchesBook(Book book, String q) {
        return normalize(book.getTitle()).contains(q)
                || normalize(book.getAuthor()).contains(q)
                || normalize(book.getIsbn()).contains(q)
                || normalize(book.getCategory()).contains(q);
    }

    private static boolean matchesMember(Member member, String q) {
        return normalize(member.getFullName()).contains(q)
                || normalize(member.getEmail()).contains(q)
                || normalize(member.getPhone()).contains(q);
    }

    private static String normalize(String value) {
        return Optional.ofNullable(value).orElse("").trim().toLowerCase(Locale.ROOT);
    }

    private void seed() {
        // Create Books
        Book b1 = bookRepository.save(new Book(null, "The Great Gatsby", "F. Scott Fitzgerald", "Classic Fiction", "978-0743273565", 1925, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg"));
        Book b2 = bookRepository.save(new Book(null, "1984", "George Orwell", "Science Fiction", "978-0451524935", 1949, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg"));
        Book b3 = bookRepository.save(new Book(null, "To Kill a Mockingbird", "Harper Lee", "Classic Fiction", "978-0061120084", 1960, BookStatus.CHECKED_OUT, "https://covers.openlibrary.org/b/isbn/9780061120084-L.jpg"));
        Book b4 = bookRepository.save(new Book(null, "Pride and Prejudice", "Jane Austen", "Romance", "978-1503290563", 1813, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9781503290563-L.jpg"));
        Book b5 = bookRepository.save(new Book(null, "The Hobbit", "J.R.R. Tolkien", "Fantasy", "978-0547928227", 1937, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780547928227-L.jpg"));
        Book b6 = bookRepository.save(new Book(null, "Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy", "978-0590353427", 1997, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780590353427-L.jpg"));
        Book b7 = bookRepository.save(new Book(null, "The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy", "978-0547928210", 1954, BookStatus.CHECKED_OUT, "https://covers.openlibrary.org/b/isbn/9780547928210-L.jpg"));
        Book b8 = bookRepository.save(new Book(null, "Dune", "Frank Herbert", "Science Fiction", "978-0441013593", 1965, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780441013593-L.jpg"));
        Book b9 = bookRepository.save(new Book(null, "Clean Code", "Robert C. Martin", "Technology", "978-0132350884", 2008, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg"));
        Book b10 = bookRepository.save(new Book(null, "The Pragmatic Programmer", "Andrew Hunt", "Technology", "978-0201616224", 1999, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780201616224-L.jpg"));
        Book b11 = bookRepository.save(new Book(null, "Thinking, Fast and Slow", "Daniel Kahneman", "Psychology", "978-0374275631", 2011, BookStatus.CHECKED_OUT, "https://covers.openlibrary.org/b/isbn/9780374275631-L.jpg"));
        Book b12 = bookRepository.save(new Book(null, "Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "History", "978-0062316097", 2014, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg"));
        Book b13 = bookRepository.save(new Book(null, "Atomic Habits", "James Clear", "Self-Help", "978-0735211292", 2018, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780735211292-L.jpg"));
        Book b14 = bookRepository.save(new Book(null, "The Catcher in the Rye", "J.D. Salinger", "Classic Fiction", "978-0316769488", 1951, BookStatus.DAMAGED, "https://covers.openlibrary.org/b/isbn/9780316769488-L.jpg"));
        Book b15 = bookRepository.save(new Book(null, "Steve Jobs", "Walter Isaacson", "Biography", "978-1451648539", 2011, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9781451648539-L.jpg"));
        Book b16 = bookRepository.save(new Book(null, "The Da Vinci Code", "Dan Brown", "Thriller", "978-0307474278", 2003, BookStatus.LOST, "https://covers.openlibrary.org/b/isbn/9780307474278-L.jpg"));

        // Create Members
        Member m1 = memberRepository.save(new Member(null, "Alice Johnson", "alice@example.com", "555-0101", LocalDate.of(2023, 1, 15), MemberStatus.ACTIVE));
        Member m2 = memberRepository.save(new Member(null, "Bob Smith", "bob@example.com", "555-0102", LocalDate.of(2023, 2, 20), MemberStatus.ACTIVE));
        Member m3 = memberRepository.save(new Member(null, "Charlie Brown", "charlie@example.com", "555-0103", LocalDate.of(2023, 3, 10), MemberStatus.SUSPENDED));
        Member m4 = memberRepository.save(new Member(null, "Diana Prince", "diana@example.com", "555-0104", LocalDate.of(2022, 5, 12), MemberStatus.ACTIVE));
        Member m5 = memberRepository.save(new Member(null, "Evan Wright", "evan@example.com", "555-0105", LocalDate.of(2023, 6, 01), MemberStatus.ACTIVE));
        Member m6 = memberRepository.save(new Member(null, "Fiona Gallagher", "fiona@example.com", "555-0106", LocalDate.of(2023, 7, 15), MemberStatus.BLOCKED));
        Member m7 = memberRepository.save(new Member(null, "George Martin", "george@example.com", "555-0107", LocalDate.of(2021, 8, 20), MemberStatus.ACTIVE));
        Member m8 = memberRepository.save(new Member(null, "Hannah Montana", "hannah@example.com", "555-0108", LocalDate.of(2023, 9, 10), MemberStatus.ACTIVE));

        LocalDate today = LocalDate.now();

        // Create Active Loans (preserving original logic)
        issueLoanSilently(b3.getId(), m1.getId(), today.minusDays(10), today.plusDays(7));
        issueLoanSilently(b7.getId(), m2.getId(), today.minusDays(10), today.minusDays(2)); // Overdue
        issueLoanSilently(b11.getId(), m4.getId(), today.minusDays(10), today.plusDays(1));

        // Create Chart Data (Loans issued in last 7 days)
        // Day -6: 2 loans
        createReturnedLoan(b1.getId(), m5.getId(), today.minusDays(6));
        createReturnedLoan(b2.getId(), m7.getId(), today.minusDays(6));

        // Day -5: 1 loan
        createReturnedLoan(b4.getId(), m8.getId(), today.minusDays(5));

        // Day -4: 3 loans
        createReturnedLoan(b5.getId(), m1.getId(), today.minusDays(4));
        createReturnedLoan(b6.getId(), m2.getId(), today.minusDays(4));
        createReturnedLoan(b8.getId(), m4.getId(), today.minusDays(4));

        // Day -3: 0 loans (gap)

        // Day -2: 2 loans
        createReturnedLoan(b9.getId(), m5.getId(), today.minusDays(2));
        createReturnedLoan(b10.getId(), m7.getId(), today.minusDays(2));

        // Day -1: 1 loan
        createReturnedLoan(b12.getId(), m8.getId(), today.minusDays(1));

        // Today: 1 loan (Active, effectively)
        issueLoanSilently(b13.getId(), m1.getId(), today, today.plusDays(14));

        // Historical Returned Loan
        loanRepository.save(new Loan(null, b9.getId(), m5.getId(), today.minusDays(30), today.minusDays(16), today.minusDays(18), LoanStatus.RETURNED));
    }

    private void createReturnedLoan(Long bookId, Long memberId, LocalDate issuedOn) {
        Loan loan = new Loan(null, bookId, memberId, issuedOn, issuedOn.plusDays(14), issuedOn.plusDays(2), LoanStatus.RETURNED);
        loanRepository.save(loan);
    }

    private void issueLoanSilently(Long bookId, Long memberId, LocalDate issuedOn, LocalDate dueOn) {
        Book book = bookRepository.findById(bookId).orElse(null);
        Member member = memberRepository.findById(memberId).orElse(null);
        
        if (book == null || member == null || book.getStatus() != BookStatus.AVAILABLE) {
            return;
        }

        Loan loan = new Loan(null, bookId, memberId, issuedOn, dueOn, null, LoanStatus.ACTIVE);
        loanRepository.save(loan);
        
        book.setStatus(BookStatus.CHECKED_OUT);
        bookRepository.save(book);
    }
}
