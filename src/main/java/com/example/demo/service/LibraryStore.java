package com.example.demo.service;

import com.example.demo.controller.BookForm;
import com.example.demo.controller.IssueLoanForm;
import com.example.demo.controller.MemberForm;
import com.example.demo.model.Book;
import com.example.demo.model.BookStatus;
import com.example.demo.model.Loan;
import com.example.demo.model.LoanStatus;
import com.example.demo.model.Member;
import com.example.demo.model.MemberStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class LibraryStore {

    private final AtomicLong bookSeq = new AtomicLong(1000);
    private final AtomicLong memberSeq = new AtomicLong(5000);
    private final AtomicLong loanSeq = new AtomicLong(9000);

    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final Map<Long, Member> members = new ConcurrentHashMap<>();
    private final Map<Long, Loan> loans = new ConcurrentHashMap<>();

    public LibraryStore() {
        seed();
    }

    public List<Book> listBooks(String query, BookStatus status, String category) {
        String q = normalize(query);
        String cat = normalize(category);

        return books.values().stream()
                .filter(book -> q.isBlank() || matchesBook(book, q))
                .filter(book -> status == null || book.getStatus() == status)
                .filter(book -> cat.isBlank() || normalize(book.getCategory()).equals(cat))
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<String> listCategories() {
        return books.values().stream()
                .map(Book::getCategory)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public Optional<Book> findBook(Long id) {
        return Optional.ofNullable(books.get(id));
    }

    public Long createBook(BookForm form) {
        Long id = bookSeq.incrementAndGet();
        Book book = new Book(
                id,
                form.getTitle().trim(),
                form.getAuthor().trim(),
                form.getCategory().trim(),
                Optional.ofNullable(form.getIsbn()).orElse("").trim(),
                form.getYear(),
                form.getStatus(),
                Optional.ofNullable(form.getImageUrl()).orElse("").trim()
        );
        books.put(id, book);
        return id;
    }

    public boolean updateBook(Long id, BookForm form) {
        Book existing = books.get(id);
        if (existing == null) {
            return false;
        }

        existing.setTitle(form.getTitle().trim());
        existing.setAuthor(form.getAuthor().trim());
        existing.setCategory(form.getCategory().trim());
        existing.setIsbn(Optional.ofNullable(form.getIsbn()).orElse("").trim());
        existing.setYear(form.getYear());
        existing.setStatus(form.getStatus());
        existing.setImageUrl(Optional.ofNullable(form.getImageUrl()).orElse("").trim());
        return true;
    }

    public boolean deleteBook(Long id) {
        return books.remove(id) != null;
    }

    public List<Member> listMembers(String query, MemberStatus status) {
        String q = normalize(query);
        return members.values().stream()
                .filter(member -> q.isBlank() || matchesMember(member, q))
                .filter(member -> status == null || member.getStatus() == status)
                .sorted(Comparator.comparing(Member::getFullName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<Member> findMember(Long id) {
        return Optional.ofNullable(members.get(id));
    }

    public Long createMember(MemberForm form) {
        Long id = memberSeq.incrementAndGet();
        Member member = new Member(
                id,
                form.getFullName().trim(),
                form.getEmail().trim(),
                Optional.ofNullable(form.getPhone()).orElse("").trim(),
                form.getMemberSince(),
                form.getStatus()
        );
        members.put(id, member);
        return id;
    }

    public boolean updateMember(Long id, MemberForm form) {
        Member existing = members.get(id);
        if (existing == null) {
            return false;
        }

        existing.setFullName(form.getFullName().trim());
        existing.setEmail(form.getEmail().trim());
        existing.setPhone(Optional.ofNullable(form.getPhone()).orElse("").trim());
        existing.setMemberSince(form.getMemberSince());
        existing.setStatus(form.getStatus());
        return true;
    }

    public boolean deleteMember(Long id) {
        return members.remove(id) != null;
    }

    public List<Loan> listLoans() {
        refreshOverdues();
        return loans.values().stream()
                .sorted(Comparator.comparing(Loan::getIssuedOn).reversed())
                .toList();
    }

    public Optional<Loan> findLoan(Long id) {
        refreshOverdues();
        return Optional.ofNullable(loans.get(id));
    }

    public Optional<Loan> findActiveLoanForBook(Long bookId) {
        refreshOverdues();
        return loans.values().stream()
                .filter(l -> l.getBookId().equals(bookId))
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .findFirst();
    }

    public Optional<Long> issueLoan(IssueLoanForm form) {
        Book book = books.get(form.getBookId());
        Member member = members.get(form.getMemberId());
        if (book == null || member == null) {
            return Optional.empty();
        }
        if (book.getStatus() != BookStatus.AVAILABLE) {
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();
        Long id = loanSeq.incrementAndGet();
        Loan loan = new Loan(id, book.getId(), member.getId(), today, form.getDueOn(), null, LoanStatus.ACTIVE);
        loans.put(id, loan);
        book.setStatus(BookStatus.CHECKED_OUT);
        return Optional.of(id);
    }

    public boolean returnLoan(Long loanId) {
        Loan loan = loans.get(loanId);
        if (loan == null) {
            return false;
        }
        if (loan.getStatus() == LoanStatus.RETURNED) {
            return true;
        }

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedOn(LocalDate.now());

        Book book = books.get(loan.getBookId());
        if (book != null && book.getStatus() == BookStatus.CHECKED_OUT) {
            book.setStatus(BookStatus.AVAILABLE);
        }
        return true;
    }

    public long countBooks() {
        return books.size();
    }

    public long countAvailableBooks() {
        return books.values().stream().filter(b -> b.getStatus() == BookStatus.AVAILABLE).count();
    }

    public long countMembers() {
        return members.size();
    }

    public long countActiveLoans() {
        refreshOverdues();
        return loans.values().stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .count();
    }

    public long countOverdueLoans() {
        refreshOverdues();
        return loans.values().stream()
                .filter(l -> l.getStatus() == LoanStatus.OVERDUE)
                .count();
    }

    public List<Book> recentBooks(int limit) {
        return books.values().stream()
                .sorted(Comparator.comparing(Book::getId).reversed())
                .limit(limit)
                .toList();
    }

    public List<Loan> overdueLoans(int limit) {
        refreshOverdues();
        return loans.values().stream()
                .filter(l -> l.getStatus() == LoanStatus.OVERDUE)
                .sorted(Comparator.comparing(Loan::getDueOn))
                .limit(limit)
                .toList();
    }

    public List<Book> availableBooks() {
        return books.values().stream()
                .filter(b -> b.getStatus() == BookStatus.AVAILABLE)
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private void refreshOverdues() {
        LocalDate today = LocalDate.now();
        loans.values().forEach(loan -> {
            if ((loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.OVERDUE)
                    && loan.getDueOn() != null
                    && loan.getDueOn().isBefore(today)) {
                loan.setStatus(LoanStatus.OVERDUE);
            }
        });
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
        Long book1 = bookSeq.incrementAndGet();
        books.put(book1, new Book(book1, "The Great Gatsby", "F. Scott Fitzgerald", "Classic Fiction", "978-0743273565", 1925, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg"));

        Long book2 = bookSeq.incrementAndGet();
        books.put(book2, new Book(book2, "1984", "George Orwell", "Science Fiction", "978-0451524935", 1949, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg"));

        Long book3 = bookSeq.incrementAndGet();
        books.put(book3, new Book(book3, "To Kill a Mockingbird", "Harper Lee", "Classic Fiction", "978-0061120084", 1960, BookStatus.CHECKED_OUT, "https://covers.openlibrary.org/b/isbn/9780061120084-L.jpg"));

        Long book4 = bookSeq.incrementAndGet();
        books.put(book4, new Book(book4, "Pride and Prejudice", "Jane Austen", "Romance", "978-1503290563", 1813, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9781503290563-L.jpg"));

        Long book5 = bookSeq.incrementAndGet();
        books.put(book5, new Book(book5, "The Hobbit", "J.R.R. Tolkien", "Fantasy", "978-0547928227", 1937, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780547928227-L.jpg"));

        Long book6 = bookSeq.incrementAndGet();
        books.put(book6, new Book(book6, "Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy", "978-0590353427", 1997, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780590353427-L.jpg"));

        Long book7 = bookSeq.incrementAndGet();
        books.put(book7, new Book(book7, "The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy", "978-0547928210", 1954, BookStatus.CHECKED_OUT, "https://covers.openlibrary.org/b/isbn/9780547928210-L.jpg"));

        Long book8 = bookSeq.incrementAndGet();
        books.put(book8, new Book(book8, "Dune", "Frank Herbert", "Science Fiction", "978-0441013593", 1965, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780441013593-L.jpg"));

        Long book9 = bookSeq.incrementAndGet();
        books.put(book9, new Book(book9, "Clean Code", "Robert C. Martin", "Technology", "978-0132350884", 2008, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg"));

        Long book10 = bookSeq.incrementAndGet();
        books.put(book10, new Book(book10, "The Pragmatic Programmer", "Andrew Hunt", "Technology", "978-0201616224", 1999, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780201616224-L.jpg"));

        Long book11 = bookSeq.incrementAndGet();
        books.put(book11, new Book(book11, "Thinking, Fast and Slow", "Daniel Kahneman", "Psychology", "978-0374275631", 2011, BookStatus.CHECKED_OUT, "https://covers.openlibrary.org/b/isbn/9780374275631-L.jpg"));

        Long book12 = bookSeq.incrementAndGet();
        books.put(book12, new Book(book12, "Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "History", "978-0062316097", 2014, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg"));

        Long book13 = bookSeq.incrementAndGet();
        books.put(book13, new Book(book13, "Atomic Habits", "James Clear", "Self-Help", "978-0735211292", 2018, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9780735211292-L.jpg"));

        Long book14 = bookSeq.incrementAndGet();
        books.put(book14, new Book(book14, "The Catcher in the Rye", "J.D. Salinger", "Classic Fiction", "978-0316769488", 1951, BookStatus.DAMAGED, "https://covers.openlibrary.org/b/isbn/9780316769488-L.jpg"));

        Long book15 = bookSeq.incrementAndGet();
        books.put(book15, new Book(book15, "Steve Jobs", "Walter Isaacson", "Biography", "978-1451648539", 2011, BookStatus.AVAILABLE, "https://covers.openlibrary.org/b/isbn/9781451648539-L.jpg"));

        Long book16 = bookSeq.incrementAndGet();
        books.put(book16, new Book(book16, "The Da Vinci Code", "Dan Brown", "Thriller", "978-0307474278", 2003, BookStatus.LOST, "https://covers.openlibrary.org/b/isbn/9780307474278-L.jpg"));

        // Create Members
        Long member1 = memberSeq.incrementAndGet();
        members.put(member1, new Member(member1, "Alice Johnson", "alice@example.com", "555-0101", LocalDate.of(2023, 1, 15), MemberStatus.ACTIVE));

        Long member2 = memberSeq.incrementAndGet();
        members.put(member2, new Member(member2, "Bob Smith", "bob@example.com", "555-0102", LocalDate.of(2023, 2, 20), MemberStatus.ACTIVE));

        Long member3 = memberSeq.incrementAndGet();
        members.put(member3, new Member(member3, "Charlie Brown", "charlie@example.com", "555-0103", LocalDate.of(2023, 3, 10), MemberStatus.SUSPENDED));

        Long member4 = memberSeq.incrementAndGet();
        members.put(member4, new Member(member4, "Diana Prince", "diana@example.com", "555-0104", LocalDate.of(2022, 5, 12), MemberStatus.ACTIVE));

        Long member5 = memberSeq.incrementAndGet();
        members.put(member5, new Member(member5, "Evan Wright", "evan@example.com", "555-0105", LocalDate.of(2023, 6, 01), MemberStatus.ACTIVE));

        Long member6 = memberSeq.incrementAndGet();
        members.put(member6, new Member(member6, "Fiona Gallagher", "fiona@example.com", "555-0106", LocalDate.of(2023, 7, 15), MemberStatus.BLOCKED));

        Long member7 = memberSeq.incrementAndGet();
        members.put(member7, new Member(member7, "George Martin", "george@example.com", "555-0107", LocalDate.of(2021, 8, 20), MemberStatus.ACTIVE));

        Long member8 = memberSeq.incrementAndGet();
        members.put(member8, new Member(member8, "Hannah Montana", "hannah@example.com", "555-0108", LocalDate.of(2023, 9, 10), MemberStatus.ACTIVE));

        // Create Loans
        // Active loan (due in future)
        issueLoanSilently(book3, member1, LocalDate.now().plusDays(7));
        
        // Overdue loan (due in past)
        issueLoanSilently(book7, member2, LocalDate.now().minusDays(2));

        // Active loan (due soon)
        issueLoanSilently(book11, member4, LocalDate.now().plusDays(1));

        // Returned Loan (History)
        Long loanId = loanSeq.incrementAndGet();
        Loan returnedLoan = new Loan(loanId, book9, member5, LocalDate.now().minusDays(30), LocalDate.now().minusDays(16), LocalDate.now().minusDays(18), LoanStatus.RETURNED);
        loans.put(loanId, returnedLoan);
    }

    private void issueLoanSilently(Long bookId, Long memberId, LocalDate dueOn) {
        Book book = books.get(bookId);
        Member member = members.get(memberId);
        if (book == null || member == null || book.getStatus() != BookStatus.AVAILABLE) {
            return;
        }

        Long id = loanSeq.incrementAndGet();
        Loan loan = new Loan(id, bookId, memberId, LocalDate.now().minusDays(10), dueOn, null, LoanStatus.ACTIVE);
        loans.put(id, loan);
        book.setStatus(BookStatus.CHECKED_OUT);
        refreshOverdues();
    }
}

