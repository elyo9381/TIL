# 트랜잭션 / 잠금 / 격리수준 테스트 시나리오

## 준비

```sql
-- 데이터 초기화 (매 테스트 전 실행)
DELETE FROM account;
INSERT INTO account VALUES (1, 'Alice', 1000), (2, 'Bob', 0);

DELETE FROM product;
INSERT INTO product VALUES (1, 'Apple', 500);
```

---

## 1. 트랜잭션 - 원자성 증명 (계좌 이체)

> 1번 성공 + 2번 실패 → 롤백으로 둘 다 실패해야 함

**세션 A (하나의 터미널에서 순서대로 실행)**

```sql
START TRANSACTION;

-- 1번: Alice 출금
UPDATE account SET balance = balance - 500 WHERE id = 1;

-- 2번: 의도적 실패 (Bob 계좌 없음)
UPDATE account SET balance = balance + 500 WHERE id = 999;

-- 실패 확인 후 롤백
ROLLBACK;

-- 결과 확인: Alice 잔액이 1000 그대로여야 함
SELECT * FROM account;
```

**기대 결과**: Alice 1000, Bob 0 (이체 없던 것처럼)

---

## 2. 잠금 - 동시 출금 (Lost Update 방지)

> 잔액 1000원에서 두 세션이 동시에 500원 출금 시도
> 잠금 없으면 잔액 500원(오답), 잠금 있으면 두 번째는 대기 후 실패

**세션 A**
```sql
START TRANSACTION;
SELECT balance FROM account WHERE id = 1 FOR UPDATE;  -- 잠금 획득
-- (잠시 대기, 세션 B 실행 후 진행)
UPDATE account SET balance = balance - 500 WHERE id = 1;
COMMIT;
```

**세션 B** (세션 A의 SELECT FOR UPDATE 직후 실행)
```sql
START TRANSACTION;
SELECT balance FROM account WHERE id = 1 FOR UPDATE;  -- 세션 A 커밋까지 대기
UPDATE account SET balance = balance - 500 WHERE id = 1;
COMMIT;
```

**기대 결과**: 세션 A 커밋 후 세션 B가 잔액 500을 읽고 차감 → 최종 0원
(잠금 없이 일반 SELECT로 하면 둘 다 1000을 읽어 최종 500원이 됨)

---

## 3. 격리수준 - Dirty Read

> READ UNCOMMITTED에서만 발생

**세션 A, B 모두 설정**
```sql
SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
```

**세션 A**
```sql
START TRANSACTION;
UPDATE product SET price = 9999 WHERE id = 1;
-- 커밋 안 함
```

**세션 B**
```sql
START TRANSACTION;
SELECT price FROM product WHERE id = 1;
-- 9999 보임 (커밋 안 된 값) → Dirty Read 발생
```

**세션 A**
```sql
ROLLBACK;
```

**세션 B**
```sql
SELECT price FROM product WHERE id = 1;
-- 500 보임 (롤백된 값) → 세션 B가 읽은 9999는 유령 데이터였음
```

---

## 4. 격리수준 - Non-Repeatable Read

> READ COMMITTED에서 발생, REPEATABLE READ에서 방지

**세션 A, B 모두 설정**
```sql
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

**세션 A**
```sql
START TRANSACTION;
SELECT price FROM product WHERE id = 1;  -- 500
```

**세션 B**
```sql
START TRANSACTION;
UPDATE product SET price = 1000 WHERE id = 1;
COMMIT;
```

**세션 A**
```sql
SELECT price FROM product WHERE id = 1;  -- 1000 (같은 트랜잭션인데 값이 바뀜)
COMMIT;
```

**REPEATABLE READ로 바꾸면**: 세션 A의 두 번째 SELECT도 500 반환

---

## 5. 격리수준 - Phantom Read

> READ COMMITTED에서 발생, REPEATABLE READ + InnoDB에서는 Gap Lock으로 방지

**세션 A, B 모두 설정**
```sql
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

**세션 A**
```sql
START TRANSACTION;
SELECT * FROM product WHERE price > 100;  -- 1건 (Apple 500)
```

**세션 B**
```sql
START TRANSACTION;
INSERT INTO product VALUES (2, 'Banana', 300);
COMMIT;
```

**세션 A**
```sql
SELECT * FROM product WHERE price > 100;  -- 2건 (Phantom 발생)
COMMIT;
```

**REPEATABLE READ로 바꾸면**: 세션 A의 두 번째 SELECT도 1건 반환 (InnoDB Gap Lock)
