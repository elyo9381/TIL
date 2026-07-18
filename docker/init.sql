-- 계좌 테이블 (트랜잭션 / 잠금 테스트용)
CREATE TABLE account (
    id      INT PRIMARY KEY,
    name    VARCHAR(20),
    balance INT NOT NULL
);

INSERT INTO account VALUES (1, 'Alice', 1000), (2, 'Bob', 0);

-- 상품 테이블 (격리수준 테스트용)
CREATE TABLE product (
    id    INT PRIMARY KEY,
    name  VARCHAR(20),
    price INT NOT NULL
);

INSERT INTO product VALUES (1, 'Apple', 500);
