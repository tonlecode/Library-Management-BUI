package com.example.demo.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseFixer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseFixer.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Running DatabaseFixer to correct mismatched Enum values...");
        
        try {
            // Count total books first
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Integer.class);
            logger.info("Current total books in database: {}", count);

            // Fix Book Status: 'BORROWED' -> 'CHECKED_OUT'
            int booksUpdated = jdbcTemplate.update(
                "UPDATE books SET status = 'CHECKED_OUT' WHERE status = 'BORROWED'"
            );
            if (booksUpdated > 0) {
                logger.info("Fixed {} books with invalid status 'BORROWED' -> 'CHECKED_OUT'", booksUpdated);
            } else {
                logger.info("No books found with invalid status 'BORROWED'.");
            }

            // Fix Book Status: Empty/Null -> 'AVAILABLE'
            int emptyStatusFixed = jdbcTemplate.update(
                "UPDATE books SET status = 'AVAILABLE' WHERE status IS NULL OR status = '' OR trim(status) = ''"
            );
            if (emptyStatusFixed > 0) {
                logger.info("Fixed {} books with empty/null status -> 'AVAILABLE'", emptyStatusFixed);
            } else {
                logger.info("No books found with empty/null status.");
            }

            // Fix Loan Status: Empty/Null -> 'RETURNED' (if returned_on is set)
            int loansReturnedFixed = jdbcTemplate.update(
                "UPDATE loans SET status = 'RETURNED' WHERE (status IS NULL OR status = '' OR trim(status) = '') AND returned_on IS NOT NULL"
            );
            if (loansReturnedFixed > 0) {
                logger.info("Fixed {} loans with empty/null status -> 'RETURNED'", loansReturnedFixed);
            }

            // Fix Loan Status: Empty/Null -> 'ACTIVE' (if returned_on is null)
            int loansActiveFixed = jdbcTemplate.update(
                "UPDATE loans SET status = 'ACTIVE' WHERE (status IS NULL OR status = '' OR trim(status) = '') AND returned_on IS NULL"
            );
            if (loansActiveFixed > 0) {
                logger.info("Fixed {} loans with empty/null status -> 'ACTIVE'", loansActiveFixed);
            }

            // Fix Member Status: Empty/Null -> 'ACTIVE'
            int membersFixed = jdbcTemplate.update(
                "UPDATE members SET status = 'ACTIVE' WHERE status IS NULL OR status = '' OR trim(status) = ''"
            );
            if (membersFixed > 0) {
                logger.info("Fixed {} members with empty/null status -> 'ACTIVE'", membersFixed);
            }
              
            // Check sample book
            if (count != null && count > 0) {
                String sampleTitle = jdbcTemplate.queryForObject("SELECT title FROM books LIMIT 1", String.class);
                logger.info("Sample book title: {}", sampleTitle);
            }

        } catch (Exception e) {
            logger.error("Error running DatabaseFixer: {}", e.getMessage());
        }
    }
}
