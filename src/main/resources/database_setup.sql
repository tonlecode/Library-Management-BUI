-- Database Schema and Data for Library Management System
-- Database: LibraryDB

-- 1. Table Structure for `books`
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    isbn VARCHAR(50),
    image_url VARCHAR(1000),
    publication_year INT,
    status VARCHAR(50)
);

-- 2. Table Structure for `members`
CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    member_since DATE,
    status VARCHAR(50)
);

-- 3. Table Structure for `loans`
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT,
    member_id BIGINT,
    issued_on DATE,
    due_on DATE,
    returned_on DATE,
    status VARCHAR(50),
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (member_id) REFERENCES members(id)
);

-- --------------------------------------------------------

-- 4. Insert Data into `books` (20 records)
INSERT INTO books (title, author, category, isbn, publication_year, status, image_url) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', 'Fiction', '9780743273565', 1925, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/7222161-L.jpg'),
('To Kill a Mockingbird', 'Harper Lee', 'Fiction', '9780061120084', 1960, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/1261770-L.jpg'),
('1984', 'George Orwell', 'Science Fiction', '9780451524935', 1949, 'CHECKED_OUT', 'https://covers.openlibrary.org/b/id/1532185-L.jpg'),
('Pride and Prejudice', 'Jane Austen', 'Romance', '9780141439518', 1813, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/7882471-L.jpg'),
('The Catcher in the Rye', 'J.D. Salinger', 'Fiction', '9780316769488', 1951, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/6487837-L.jpg'),
('The Hobbit', 'J.R.R. Tolkien', 'Fantasy', '9780547928227', 1937, 'CHECKED_OUT', 'https://covers.openlibrary.org/b/id/6979861-L.jpg'),
('Fahrenheit 451', 'Ray Bradbury', 'Science Fiction', '9781451673319', 1953, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/8389332-L.jpg'),
('The Lord of the Rings', 'J.R.R. Tolkien', 'Fantasy', '9780618640157', 1954, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/12555314-L.jpg'),
('Harry Potter and the Sorcerer\'s Stone', 'J.K. Rowling', 'Fantasy', '9780590353427', 1997, 'CHECKED_OUT', 'https://covers.openlibrary.org/b/id/10580436-L.jpg'),
('The Alchemist', 'Paulo Coelho', 'Fiction', '9780061122415', 1988, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/12776361-L.jpg'),
('The Da Vinci Code', 'Dan Brown', 'Thriller', '9780307474278', 2003, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/7892976-L.jpg'),
('The Hunger Games', 'Suzanne Collins', 'Science Fiction', '9780439023481', 2008, 'CHECKED_OUT', 'https://covers.openlibrary.org/b/id/12563852-L.jpg'),
('The Kite Runner', 'Khaled Hosseini', 'Fiction', '9781594480003', 2003, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/8259443-L.jpg'),
('Life of Pi', 'Yann Martel', 'Adventure', '9780156027328', 2001, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/12313627-L.jpg'),
('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 'History', '9780062316097', 2011, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/8381504-L.jpg'),
('Educated', 'Tara Westover', 'Memoir', '9780399590504', 2018, 'CHECKED_OUT', 'https://covers.openlibrary.org/b/id/8328769-L.jpg'),
('Becoming', 'Michelle Obama', 'Memoir', '9781524763138', 2018, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/8447823-L.jpg'),
('Atomic Habits', 'James Clear', 'Self-Help', '9780735211292', 2018, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/12726955-L.jpg'),
('Clean Code', 'Robert C. Martin', 'Technology', '9780132350884', 2008, 'AVAILABLE', 'https://covers.openlibrary.org/b/id/13150534-L.jpg'),
('Thinking, Fast and Slow', 'Daniel Kahneman', 'Psychology', '9780374275631', 2011, 'CHECKED_OUT', 'https://covers.openlibrary.org/b/id/10517901-L.jpg');

-- 5. Insert Data into `members` (20 records)
INSERT INTO members (full_name, email, phone, member_since, status) VALUES
('Sok Dara', 'sok.dara@example.com', '012345678', '2023-01-15', 'ACTIVE'),
('Chan Thida', 'chan.thida@example.com', '012987654', '2023-02-20', 'ACTIVE'),
('Keo Visal', 'keo.visal@example.com', '098765432', '2023-03-10', 'ACTIVE'),
('Ly Sophea', 'ly.sophea@example.com', '011223344', '2023-04-05', 'EXPIRED'),
('Vong Sothea', 'vong.sothea@example.com', '077889900', '2023-05-12', 'ACTIVE'),
('Chea Rithy', 'chea.rithy@example.com', '010556677', '2023-06-18', 'ACTIVE'),
('Heng Vanna', 'heng.vanna@example.com', '092334455', '2023-07-22', 'ACTIVE'),
('Lim Seyha', 'lim.seyha@example.com', '012445566', '2023-08-30', 'SUSPENDED'),
('Sam Nang', 'sam.nang@example.com', '017889977', '2023-09-14', 'ACTIVE'),
('Mao Srey', 'mao.srey@example.com', '018998877', '2023-10-01', 'ACTIVE'),
('Tep Bopha', 'tep.bopha@example.com', '096112233', '2023-11-05', 'ACTIVE'),
('Ros Srey', 'ros.srey@example.com', '012667788', '2023-12-10', 'ACTIVE'),
('Khieu Sam', 'khieu.sam@example.com', '099554433', '2024-01-02', 'ACTIVE'),
('Long Vithyea', 'long.vithyea@example.com', '012332211', '2024-01-15', 'ACTIVE'),
('Chhem Sopheak', 'chhem.sopheak@example.com', '088776655', '2024-02-01', 'ACTIVE'),
('Lay Nary', 'lay.nary@example.com', '011998877', '2024-02-15', 'EXPIRED'),
('Seng Tola', 'seng.tola@example.com', '093223344', '2024-03-01', 'ACTIVE'),
('Phan Ratanak', 'phan.ratanak@example.com', '070889900', '2024-03-15', 'ACTIVE'),
('Kim Sotheary', 'kim.sotheary@example.com', '012009988', '2024-04-01', 'ACTIVE'),
('Sao Piseth', 'sao.piseth@example.com', '017665544', '2024-04-10', 'ACTIVE');

-- 6. Insert Data into `loans` (20 records)
INSERT INTO loans (book_id, member_id, issued_on, due_on, returned_on, status) VALUES
(1, 1, '2024-01-01', '2024-01-15', '2024-01-14', 'RETURNED'),
(2, 2, '2024-01-05', '2024-01-19', '2024-01-18', 'RETURNED'),
(3, 3, '2024-02-01', '2024-02-15', NULL, 'ACTIVE'),
(4, 4, '2024-02-10', '2024-02-24', '2024-02-23', 'RETURNED'),
(5, 5, '2024-03-01', '2024-03-15', '2024-03-14', 'RETURNED'),
(6, 6, '2024-03-15', '2024-03-29', NULL, 'OVERDUE'),
(7, 7, '2024-03-20', '2024-04-03', '2024-04-01', 'RETURNED'),
(8, 8, '2024-04-01', '2024-04-15', '2024-04-14', 'RETURNED'),
(9, 9, '2024-04-05', '2024-04-19', NULL, 'ACTIVE'),
(10, 10, '2024-04-10', '2024-04-24', '2024-04-22', 'RETURNED'),
(11, 11, '2024-04-15', '2024-04-29', '2024-04-28', 'RETURNED'),
(12, 12, '2024-05-01', '2024-05-15', NULL, 'ACTIVE'),
(13, 13, '2024-05-05', '2024-05-19', '2024-05-18', 'RETURNED'),
(14, 14, '2024-05-10', '2024-05-24', '2024-05-23', 'RETURNED'),
(15, 15, '2024-05-15', '2024-05-29', '2024-05-28', 'RETURNED'),
(16, 16, '2024-06-01', '2024-06-15', NULL, 'OVERDUE'),
(17, 17, '2024-06-05', '2024-06-19', '2024-06-18', 'RETURNED'),
(18, 18, '2024-06-10', '2024-06-24', '2024-06-22', 'RETURNED'),
(19, 19, '2024-06-15', '2024-06-29', '2024-06-28', 'RETURNED'),
(20, 20, '2024-06-20', '2024-07-04', NULL, 'ACTIVE');
