package com.example.demo.controller;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class IssueLoanForm {

    @NotNull
    private Long bookId;

    @NotNull
    private Long memberId;

    @NotNull
    @Future
    private LocalDate dueOn;

    public static IssueLoanForm empty() {
        IssueLoanForm form = new IssueLoanForm();
        form.setDueOn(LocalDate.now().plusDays(14));
        return form;
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

    public LocalDate getDueOn() {
        return dueOn;
    }

    public void setDueOn(LocalDate dueOn) {
        this.dueOn = dueOn;
    }
}

