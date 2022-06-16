-- https://www.baeldung.com/spring-mybatis
CREATE TABLE IF NOT EXISTS ARTICLES (
    id          INTEGER PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    author      VARCHAR(100) NOT NULL
);
INSERT INTO ARTICLES
VALUES (1, 'Working with MyBatis in Spring', 'Baeldung');