package com.example.demo.model;

import java.time.LocalDate;

public class Loan {
    private Long id;
    private Long bookId;
    private Long memberId;
    private LocalDate issuedOn;
    private LocalDate dueOn;
    private LocalDate returnedOn;
    private LoanStatus status;

    public Loan() {
    }

    public Loan(
            Long id,
            Long bookId,
            Long memberId,
            LocalDate issuedOn,
            LocalDate dueOn,
            LocalDate returnedOn,
            LoanStatus status
    ) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issuedOn = issuedOn;
        this.dueOn = dueOn;
        this.returnedOn = returnedOn;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssuedOn() {
        return issuedOn;
    }

    public void setIssuedOn(LocalDate issuedOn) {
        this.issuedOn = issuedOn;
    }

    public LocalDate getDueOn() {
        return dueOn;
    }

    public void setDueOn(LocalDate dueOn) {
        this.dueOn = dueOn;
    }

    public LocalDate getReturnedOn() {
        return returnedOn;
    }

    public void setReturnedOn(LocalDate returnedOn) {
        this.returnedOn = returnedOn;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }
}

