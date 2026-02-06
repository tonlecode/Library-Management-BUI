package com.example.demo.repository;

import com.example.demo.model.Loan;
import com.example.demo.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    long countByStatus(LoanStatus status);
}
