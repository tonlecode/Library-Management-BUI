package com.example.demo.controller;

import com.example.demo.model.Member;
import com.example.demo.model.MemberStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class MemberForm {

    @NotBlank
    @Size(max = 120)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;

    @Size(max = 32)
    private String phone;

    @NotNull
    private LocalDate memberSince;

    @NotNull
    private MemberStatus status;

    public static MemberForm empty() {
        MemberForm form = new MemberForm();
        form.setMemberSince(LocalDate.now());
        form.setStatus(MemberStatus.ACTIVE);
        return form;
    }

    public static MemberForm from(Member member) {
        MemberForm form = new MemberForm();
        form.setFullName(member.getFullName());
        form.setEmail(member.getEmail());
        form.setPhone(member.getPhone());
        form.setMemberSince(member.getMemberSince());
        form.setStatus(member.getStatus());
        return form;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(LocalDate memberSince) {
        this.memberSince = memberSince;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }
}

