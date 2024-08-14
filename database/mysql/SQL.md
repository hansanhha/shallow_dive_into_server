[SQL](#sql-structured-query-language)

[DDL](#ddl-data-definition-language)
- [데이터베이스](#ddl---데이터베이스)
- [테이블](#ddl---테이블)
- [인덱스](#ddl---인덱스)
- [뷰](#ddl---뷰)
- [트리거](#ddl---트리거)

[DCL](#dcl-data-control-language)
- [계정 관리](#계정-관리)
- [권한 부여](#권한-부여)
- [권한 제거](#권한-제거)

[DML](#dml-data-manipulation-language)
- [삽입 (Insert)](#삽입-insert)
- [수정 (Update)](#수정-update)
- [삭제 (Delete)](#삭제-delete)

[DQL](#dql-data-query-language)

[TCL](#tcl-transaction-control-language)
- [COMMIT](#commit)
- [ROLLBACK](#rollback)
- [SAVAPOINT](#savepoint)
- [SET TRANSACTION](#set-transaction)

## SQL (Structured Query Language)

관계형 데이터베이스를 조작하고 관리하기 위한 질의 언어로, RDBMS와 상호작용할 수 있는 표준적인 방법임

또한 SQL은 선언형 언어로, 어떤 데이터에 접근할지 지정하면 RDBMS가 작업 수행 방법을 결정함

SQL은 작업 수행 성격에 따라 다음과 같이 분류함

- [DDL (Data Definition Language)](#ddl-data-definition-language)
  - 데이터베이스 객체 정의 언어
  - 테이블, 뷰, 인덱스, 제약조건 등
- [DCL (Data Control Language)](#dcl-data-control-language)
  - 데이터베이스 객체에 대한 접근 및 권한 관리 언어
  - `GRANT`, `REVOKE` 등
- [DML (Data Manipulation Language)](#dml-data-manipulation-language)
  - 테이블의 데이터를 조작하는 언어
  - `INSERT`, `UPDATE`, `DELETE` 등
- [DQL (Data Query Language)](#dql-data-query-language)
  - 데이터베이스로부터 데이터를 검색하는 언어로 DML에 포함됨
  - `SELECT` 등
- [TCL (Transaction Control Language)](#tcl-transaction-control-language)
  - 트랜잭션 흐름이나 결과를 제어하는 언어 
  - 데이터 무결성 및 일관성 보장

## DDL (Data Definition Language)

MySQL의 데이버테이스 객체를 정의하는 언어임
- 데이터베이스
- 테이블
- 제약조건
  - NOT NULL
  - UNIQUE
  - PRIMARY KEY
  - FOREIGN KEY
  - DEFAULT
- 인덱스
- 뷰
- 트리거 등

DDL의 명령어의 종류는 다음과 같음

**CREATE**
  - 새로운 데이터베이스 객체를 생성하는 명령어 

**ALTER**
  - 기존의 데이터베이스 객체를 수정하는 명령어

**DROP**
  - 기존의 데이터베이스 객체를 삭제하는 명령어
  - 이 명령어를 실행하면 해당 객체와 관련된 모든 데이터가 영구적으로 삭제됨 

**TRUNCATE** (테이블에만 적용되는 명령어)
  - 모든 테이블의 데이터를 삭제하는 명령어 
  - 테이블 자체는 유지하며, `DELETE` 명령보다 빠르고 AUTO_INCRENMENT 값을 초기화함

**RENAME**
  - 객체의 이름을 변경하는 명령어

**COMMENT** (테이블과 열에 적용되는 명령어)
  - 테이블이나 열에 주석을 추가하는 명령어
  - 데이터베이스 스키마를 문서화하는 데 유용함

### DDL - 데이터베이스

```mysql
# 생성
CREATE DATABASE my_database;

# 수정: 데이터베이스 수정은 보통 기본 문자 집합이나 정렬을 변경하는 데 사용함)
ALTER DATABASE my_database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 삭제
DROP DATABASE my_database;

# 데이터베이스 이름 변경은 MySQL에서 지원되지 않음
# 새로운 데이터베이스를 생성한 뒤 데이터를 복사해서 새 데이터베이스로 이동해야 됨
```

### DDL - 테이블

```mysql
# 생성
CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    position VARCHAR(100),
    salary DECIMAL(10, 2)
);

# 수정 (컬럼 추가)
ALTER TABLE employees ADD COLUMN hire_date DATE NOT NULL;

# 수정 (컬럼 삭제)
ALTER TABLE employees DROP COLUMN hire_date;

# 수정 (컬럼 이름 변경)
ALTER TABLE employees RENAME COLUMN name TO full_name;

# 수정 (컬럼 데이터 타입 변경)
ALTER TABLE employees MODIFY COLUMN salary DECIMAL(12, 2);

# 수정 (컬럼 기본 값 설정)
ALTER TABLE employees ALTER COLUMN salary SET DEFAULT 0;

# 수정 (컬럼 기본 값 제거)
ALTER TABLE employees ALTER COLUMN hire_date DROP DEFAULT;

# 수정 (인덱스 추가)
ALTER TABLE employees ADD INDEX idx_name (name);

# 수정 (인덱스 제거)
ALTER TABLE employees DROP INDEX idx_name;

# 수정 (제약조건 추가)
ALTER TABLE employees ADD CONSTRAINT unique_name UNIQUE(name);
ALTER TABLE employees MODIFY COLUMN name VARCHAR(100) NOT NULL; # NOT NULL 설정
ALTER TABLE employees MODIFY COLUMN name VARCHAR(100) NULL; # NULL 설정

# 수정 (컬럼 순서 변경)
ALTER TABLE employees MODIFY COLUMN position VARCHAR(100) AFTER name;
ALTER TABLE employees MODIFY COLUMN hire_date DATE FIRST;

# 수정 (AUTO_INCREMENT 값 재설정)
ALTER TABLE employees AUTO_INCREMENT = 100;

# 수정 (테이블의 기본 문자 집합 및 정렬 방식 변경)
ALTER TABLE employees CONVERT TO CHARACTER SET utf8mb4 COLLATE  utf8mb4_unicode_ci;

# 수정 (주석 추가)
ALTER TABLE employees COMMENT = "Employee table";
ALTER TABLE employees MODIFY COLUMN salary DECIMAL(12, 2) COMMENT 'Employee salary'; 

# 삭제
DROP TABLE employees;

# 전체 값 삭제
TRUNCATE TABLE employees;
  
# 이름 변경
RENAME TABLE employees TO staff;
```

### DDL - 인덱스

```mysql
# 세컨더리 인덱스 생성
CREATE INDEX idx_name ON employees (name);

# 유니크 인덱스 생성
CREATE UNIQUE INDEX idx_unique_name ON employees (name);

# 삭제
DROP INDEX idx_name ON employees;
DROP INDEX idx_unique_name ON employees;

# 수정, 이름 변경은 인덱스 삭제 후 재생성 필요
```

### DDL - 뷰

```mysql
# 생성 (이름과 직책만 포함된 뷰 생성)
CREATE VIEW employee_view AS
SELECT name, position
FROM employees

# 수정
ALTER VIEW employee_view AS
SELECT name, position, salary
FROM employees;

# 삭제
DROP VIEW employee_view;

# 이름 변경
RENAME TABLE employee_view TO staff_view;
```

### DDL - 트리거

```mysql
# 생성
CREATE TRIGGER before_insert_employee
BEFORE INSERT ON employees
FOR EACH ROW
BEGIN
  IF NEW.salary < 0 THEN
    SINGAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Salary cannot be negative';
  END IF;
END;

# 트리거는 수정 불가, 삭제 후 재생성
DROP TRIGGER before_insert_employee;
```

## DCL (Data Control Language)

### 계정 관리

```mysql
# 계정 생성 
CREATE USER 'username'@'localhost' IDENTIFIED BY 'password';

# 계정 삭제
DROP USER 'username'@'localhost';

# 계정 권한 확인
SHOW GRANTS FOR 'username'@'localhost';

# 계정 이름 변경
RENAME USER 'username'@'localhost' TO 'new_username'@'localhost';
```

### 권한 부여

GRANT: 권한 부여

```mysql
# 권한 부여 스니펫
GRANT 권한 종류(SELECT, UPDATE 등) ON 데이터베이스.테이블 TO 계정;

# 모든 데이터베이스에 모든 권한 부여
GRANT ALL ON *.* TO 'adam'@'localhost';

# my_database 데이터베이스의 모든 테이블에 SELECT, INSERT, UPDATE, DELETE 권한 부여
GRANT SELECT, INSERT, UPDATE, DELETE ON my_database.* TO 'username'@'localhost';

# my_database 데이터베이스의 특정 테이블에 권한 부여
GRANT SELECT, INSERT, UPDATE, DELETE ON my_database.employees TO 'username'@'localhost';

# 권한 변경 사항 적용
FLUSH PRIVILEGES;
```

### 권한 제거

REVOKE: 권한 제거

```mysql
# 권한 제거 스니펫
REVOKE 권한 종류(SELECT, UPDATE 등) ON 데이터베이스.테이블 FROM 계정;

# 모든 데이터베이스에 대한 모든 권한 제거
REVOKE ALL ON *.* FROM 'adam'@'localhost';

# my_database 데이터베이스의 모든 테이블에 대한 SELECT, INSERT, UPDATE, DELETE 권한 제거
REVOKE SELECT, INSERT, UPDATE, DELETE ON my_database.* FROM 'username'@'localhost';

# my_database 데이터베이스의 특정 테이블에 대한 권한 제거
REVOKE SELECT ON my_database.employees FROM 'username'@'localhost';

# 권한 변경 사항 적용
FLUSH PRIVILEGES;
```

## DML (Data Manipulation Language)

### 삽입 (INSERT)

```mysql
# 단 건 삽입
INSERT INTO employees (name, position, salary)
VALUES ('john', 'manager', 75000);

# 여러 건 삽입
INSERT INTO employees (name, position, salary)
VALUES
('john', 'manager', 75000),
('anna', 'delveoper', 90000);

# 특정 열만 삽입 (나머진 기본 값 처리)
INSERT INTO employees (name)
VALUES ('mike');

# 다른 테이블의 데이터를 통해 삽입
INSERT INTO employees (name, position, salary)
SELECT name, position, salary FROM old_employees;
```

### 수정 (UPDATE)

```mysql
# 특정 레코드 수정
# name이 'mike'인 레코드의 salary 값 수정
UPDATE employees
SET salary = 80000
WHERE name = 'mike';

# 여러 레코드 수정
# position이 'developer'인 모든 레코드들의 salary 값 수정 
UPDATE employees
SET salary = salary * 1.05
WHERE position = 'developer';

# 모든 레코드 수정
# 조건없이 UPDATE문을 사용하면 모든 레코드의 필드가 수정됨
UPDATE employees
SET salary = 0;

# 여러 필드 수정
UPDATE employees
SET position = 'designer', salary = 60000
WHERE name = 'mike';

# 조건에 따른 수정
# salary의 값에 따라 분기 처리
UPDATE employees
SET salary = CASE
    WHEN salary < 50000 THEN salary * 1.10
    ELSE salary * 1.05
END;
```

### 삭제 (DELETE)

```mysql
# 특정 레코드 삭제
DELETE FROM employees
WHERE name = 'mike';

# 모든 레코드 삭제
DELETE FROM employees;
```

## DQL (Data Query Language)

## TCL (Transaction Control Language)

TCL은 데이터베이스에서 트랜잭션을 제어와 관리에 사용되는 SQL 명령어임

트랜잭션은 데이터베이스 무결성과 일관성을 보장하기 위해 SQL 연산을 하나의 논리적인 단위로 묶어 처리하는 것을 말함

### COMMIT

트랜잭션을 데이터베이스에 영구히 반영하는 명령어

이 명령어를 실행하면, 현재 트랜잭션 내의 모든 변경사항이 확정되며, 롤백을 할 수 없게 됨

```mysql
# 트랜잭션 시작
START TRANSACTION

# 데이터 작업 ... 

# 변경사항 반영 
```
COMMIT

### ROLLBACK

현재 트랜잭션에서 이뤄진 모든 변경사항을 취소하고, 데이터베이스를 트랜잭션 시작 전의 상태로 되돌림

```mysql
# 트랜잭션 시작
START TRANSACTION

# 데이터 작업 ... 

# 원 상태로 복구
ROLLBACK
```

### SAVEPOINT

트랜잭션 내에서 특정 시점에 저장점을 설정하여, 해당 시점 이후의 변경사항만 롤백할 수 있도록 함

```mysql
START TRANSACTION 

# 데이터 작업 1 ...

SAVEPOINT savepoint1;

# 데이터 작업 2 ...

# savedpoint1 이후의 작업만 취소 (데이터 작업 1은 롤백하지 않음)
ROLLBACK savepoint1;

COMMIT
```

### SET TRANSACTION

트랜잭션의 격리 수준과 같은 특성을 설정하는 데 사용됨

```mysql
# 트랜잭션 시작 및 격리 수준 설정
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
START TRANSACTION;

# 데이터 작업 ...

COMMIT;
```