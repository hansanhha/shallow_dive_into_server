[Transaction](#transaction)

[MySQL Isolation Level](#mysql-isolation-level)

[MySQL Engine Lock](#mysql-engine-lock)
- [Global Lock](#global-lock)
- [Backup Lock](#backup-lock)
- [Table Lock](#table-lock)
- [Named Lock](#named-lock)
- [Metadata Lock](#metadata-lock)
[Storage Engine Lock](#storage-engine-lock)
- [InnoDB Lock](#innodb-lock)
  - [Record Lock](#레코드-락record-lock)
  - [Gap Lock](#갭-락gap-lock)
  - [Next-Key Lock](#넥스트-키-락next-key-lock)
  - [Auto-Increment Lock](#자동-증가-락autoinc-lock)
  - [Record Lock vs Next-Key Lock](#레코드-락-vs-넥스트-키-락)

## Transaction

컴퓨터는 확실한 것을 좋아함

0 또는 1

성공 또는 실패

데이터베이스의 트랜잭션은 이와 비슷하게 작업(operation)이 완전하게 성공했는지, 아니면 실패했는지로 구분함

성공한 경우 변경 이력을 실제로 반영(Commit)하고, 그렇지 않다면 원래대로 되돌림(Rollback)

성공과 실패의 기준이 뭘까?

트랜잭션은 하나의 논리적인 작업의 단위로 쿼리가 하나이든, 여러 개이든 모든 쿼리가 성공적으로 실행되야 함

중간에 시스템 실패/충돌, 애플리케이션의 예외 발생, 쿼리 자체의 오류 등으로 인해 에러가 발생한 경우 실패로 간주함

실패로 간주된 트랜잭션은 일부분만 성공된 부분 데이터를 데이터베이스에 반영하지 않기 때문에 

작업의 완전성을 지켜서 데이터 정합성을 보장함

MySQL의 경우 InnoDB 테이블이 트랜잭션을 지원하고, MyISAM과 MEMORY 테이블은  지원하지 않음

## MySQL Isolation Level

트랜잭션 ACID 중 Isolation(고립성) 특성은 동시성 제어에 관한 것임

여러 트랜잭션이 동시에 처리될 때 특정 트랜잭션이 다른 트랜잭션에서 변경하거나 조회하는 데이터를 볼 수 있게 할건지 결정함

격리 수준 
```text
READ UNCOMMITTED(DIRTY READ)       : 고립성 가장 낮음, 동시 처리 성능 높음

READ COMMITTED(NON-REPEATABLE READ): Oracle Default

REPEATABLE READ                    : MySQL InnoDB Default

SERIALIZABLE                       : 고립성 가장 높음, 동시 처리 성능 낮음
```

| 격리 수준 | DIRTY READ  | NON-REPEATABLE READ  |  PHANTOM READ  |
| --- |:-----------:|:--------------------:|:--------------:|
| READ UNCOMMITTED |      O      |          O           |       O        |
| READ COMMITTED |      X      |          O           |       O        |
| REPEATABLE READ |      X      |          X           |  O(InnoDB X)   |
| SERIALIZABLE |      X      |          X           |       X        |

#### READ UNCOMMITTED

커밋 여부와 상관없이 다른 트랜잭션에서 임시적으로 반영한 데이터까지 읽을 수 있음 (**Dirty Read**)

#### READ COMMITTED

커밋된 데이터만 다른 트랜잭션에서 조회 가능(Dirty Read 방지)

A 트랜잭션에서 a 레코드를 변경 중일 때 B 트랜잭션에서 해당 레코드를 조회하면 언두 로그에 있는 데이터가 조회됨

이후 A 트랜잭션에서 변경사항을 커밋하고, B 트랜잭션에서 다시 조회하면 변경된 데이터가 조회됨(이전 데이터와 불일치, **Non-Repeatable Read**)

#### REPEATABLE READ

바이너리 로그를 가진 MySQL 서버의 최소 요구 격리 수준으로

트랜잭션 내에서 조회한 데이터는 다른 트랜잭션이 해당 데이터의 값을 변경한 뒤에 다시 조회해도 트랜잭션이 종료될 때까지 변경되지 않음(Non-Repeatable Read 방지)

동일한 레코드에 대한 변경 사항을 언두 로그에 여러 가지 버전(MVCC)로 저장하는데, 이 때 각 언두 로그에 트랜잭션 ID를 저장함

즉, 트랜잭션에서 조회한 데이터는 특정 트랜잭션 ID를 가진 언두 로그의 데이터이므로, 이 ID 값을 통해 트랜잭션이 종료될 때까지 같은 데이터를 조회할 수 있게 됨

다만, 다른 트랜잭션에서 새로운 레코드를 추가하거나 삭제하는 경우 기존에 조회했던 결과와 다르게 행이 추가되거나 삭제될 수 있음(**Phantom Read**)

#### SERIALIZABLE

가장 높은 격리 수준

읽기 작업도 공유 잠금을 획득해야 됨

트랜잭션에서 읽고 쓰는 레코드를 다른 트랜잭션은 접근할 수 없게 하고, 쿼리가 영향을 미치는 범위 전체에 대해 새로운 데이터를 삽입하지 못하게 함(Phantom Read 방지)

InnoDB의 경우 REPEATABLE READ 격리 수준에는 넥스트 키 락(레코드 락 + 갭 락)을 사용하기 때문에 Phantom Read가 발생하지 않음

## MySQL Engine Lock

MySQL의 Lock은 크게 MySQL Engine Lock과 Storage Engine Lock으로 나뉨

MySQL Engine Lock: 모든 스토리지에 영향을 미치는 Lock

Storage Engine Lock: 특정 스토리지 엔진만의 Lock

MySQL Engine에서는 **Global Lock**, **Backup Lock**, **Table Lock**, **Named Lock**, **Metadata Lock**을 제공함

### Global Lock

글로벌 락과 백업 락은 유지보수 작업에서 데이터 일관성과 무결성을 보장하기 위해 사용됨

주로 스냅샷 생성, 데이터 덤프 등의 작업을 수행할 때 글로벌 락을 사용하여 모든 데이터를 일관된 상태로 유지함

`FLUSH TABLES WITH READ LOCK` 명령을 통해 글로벌 락을 획득할 수 있음

글로벌 락은 MySQL 서버 전체(전체 데이터베이스)에 영향을 미치는 락으로, 어떤 세션에서 글로벌 락을 획득하면 다른 세션에서 실행한 DML, DDL (SELECT 제외) 문장이 대기 상태로 남게 됨

다만 글로벌 락을 걸기 전에 수행된 쿼리가 수행되어야 하는데, 장시간 수행되는 경우 MySQL 서버의 모든 테이블에 대한 INSERT, UPDATE, DELETE 쿼리가 그만큼 대기 상태에 놓이게 됨

### Backup Lock

InnoDB 엔진의 경우 트랜잭션을 지원하기 때문에 일관된 데이터 상태를 위해 전체 데이터베이스의 변경 작업을 멈추지 않아도 됨

백업 락은 Xtrabackup, Enterprise Backup 같은 백업 툴들의 안정적인 실행을 위해 8.0 버전부터 도입됨

읽기 작업만 허용하는 글로벌 락에 비해 백업 락은 백업 작업 동안 데이터 일관성을 유지하면서도 데이터 읽기/쓰기/삭제(DML) 작업을 허용하여 성능에 미치는 영향을 최소화함

다만 다음의 변경 사항은 방지함
- 데이터베이스 및 테이블 생성 및 변경, 삭제(DDL)
- REPAIR TABLE, OPTIMIZE TABLE
- 사용자 관리 및 비밀번호 변경

백업 락은 인스턴스 레벨에서 영향을 끼침

`LOCK INSTANCE FOR BACKUP` 명령으로 백업 락을 획득할 수 있음


### Table Lock

개별 테이블 단위로 설정되는 락임

`LOCK TABLES table_name [ READ | WRITE ]` 명령으로 특정 테이블의 락을 획득할 수 있음

이후 `UNLOCK TABLES` 명령으로 락을 해제 가능

MyISAM이나 MEMORY 테이블에 데이터 변경 쿼리를 실행하면 묵시적으로 테이블 락이 발생하고, 쿼리가 수행된 후 자동적으로 해제됨

InnoDB의 경우 테이블 락이 설정되지만 레코드 기반의 락을 제공하기 때문에 단순 데이터 변경 쿼리로 인해 묵시적 테이블 락이 설정되지 않음

### Named Lock

이름을 지정하여 락을 걸 수 있는 사용자 정의 락임

**락 획득**

`SELECT GET_LOCK(my_lock, 10)`: 10초 동안 "my_lock" 이름을 가진 락을 획득

**락 반납**

`SELECT RELEASE_LOCK(my_lock)`: "my_lock" 반납

`SELECT RELEASE_ALL_LOCKS()`: 모든 네임드 락 반납

네임드 락은 해당 락을 획득한 세션에서 특정 작업을 동기화하는 데 사용됨

다른 세션에서 동일한 네임드 락을 획득하려고 시도하면, 락을 해제하기 전까지 대기하거나 타임아웃이 됨

네임드 락을 통해 여러 애플리케이션에서 하나의 데이터베이스를 공유하는 상황에서 충돌하지 않도록 동기화하거나, 서로 다른 세션이 동시에 배치 작업을 실행하지 않도록 조정할 수 있음

### Metadata Lock

데이터베이스 객체(테이블, 뷰 등)의 이름이나 구조를 변경하는 경우에 **자동**으로 획득하는 락임

`RENAME TABLE tab_a TO tab_b` 같이 테이블 이름을 변경하는 경우 원본 이름과 변경될 이름을 한꺼번에 락을 검

## Storage Engine Lock

### InnoDB Lock

InnoDB는 MySQL Engine Lock과 별개로 자체적인 레코드 기반의 잠금 방식을 탑재함

**InnoDB의 락 종류**
- 레코드 락
    - 배타 락(Exclusive Lock)
    - 공유 락(Shared Lock, S-lock)
- 갭 락
- 넥스트 키 락
- 자동 증가 락

**InnoDB 락 특징**
- 락 에스컬레이션 없음
  - 레코드 락 -> 페이지 락, 테이블 락 등으로 업그레이드되지 않음
- 레코드 기반 락
  - 레코드 단위로 락을 걸어 다른 트랜잭션의 영향을 최소화함
- 쓰기 작업(INSERT, UPDATE, DELETE)는 자동으로 락이 걸림
- 읽기 작업(SELECT)은 기본적으로 락을 걸지 않음 (별도의 락을 걸어야 함)

#### 레코드 락(Record Lock)

- 인덱스의 레코드에 거는 락 
- 다른 트랜잭션에서 해당 레코드에 접근하지 못하도록 함 
- 인덱스가 하나도 없는 테이블이더라도 내부적으로 자동 생성된 클러스터 인덱스를 이용해 잠금을 설정함
- 레코드 자체를 잠그는 다른 DBMS와의 달리 MySQL InnoDB 스토리지 엔진은 인덱스의 레코드에 락을 거는 게 특징임
- 기본 키(Primary Key), 유니크 인덱스에 의한 변경 작업에선 갭(Gap)에 대해 잠그지 않음

레코드 락은 배타 락과 공유 락으로 구성됨
- 배타 락
  - 단일 트랜잭션만이 해당 레코드에 읽기/쓰기 작업(INSERT, UPDATE, DELETE)을 수행
  - ```sql
    -- 특정 레코드에 배타락을 걸어 다른 트랜잭션이 해당 레코드를 읽거나 쓰지 못하도록 함
    SELECT * FROM employees WHERE emp_no = 100 FOR UPDATE;
    ```
- 공유 락 
  - 여러 트랜잭션이 동시에 동일한 레코드를 읽을 수 있도록 허용하지만, 쓰기 작업은 하나의 트랜잭션에서만 수행
  - ```sql
    -- 특정 레코드에 공유락을 걸어 다른 트랜잭션이 해당 레코드를 읽기만 가능하도록 함
    SELECT * FROM employees WHERE emp_no = 100 FOR SHARE;
    ```

**레코드 락에 대한 중요한 사실**

InnoDB는 테이블의 레코드가 아닌 해당 레코드를 가리키는 인덱스 레코드를 잠금

성능을 최적화하고 필요한 행에만 잠금을 걸어 다른 행에 대한 접근을 허용하기 목적인데

만약 인덱스 값을 기준으로 여러 행이 존재할 경우, 해당 인덱스 값을 가진 모든 인덱스 레코드에 락을 검

```mysql
UPDATE employees
SET last_name = 'Doe'
WHERE first_name = 'John';
```

위처럼 first_name 칼럼을 멤버로 가진 인덱스가 있는 경우

John이라는 값을 가진 모든 인덱스 레코드에 락을 검

만약 인덱스가 없는 경우, 테이블 전체에 락을 검(성능 저하 발생)

#### 갭 락(Gap Lock)

레코드와 바로 인접한 레코드 사이의 간격을 잠그는 락

레코드와 레코드 사이의 간격에 새로운 레코드가 생성(INSERT)되는 것을 제어함

팬텀 읽기 방지 목적

```sql
// 특정 범위의 인덱스 간격에 락을 걸어 다른 트랜잭션이 레코드 중간에 INSERT 하지 못하도록 함
SELECT * FROM employees WHERE emp_no > 100 FOR UPDATE;
```

#### 넥스트 키 락(Next-Key Lock)

레코드 락과 갭 락을 결합한 락

특정 레코드와 그 주변의 인덱스 간격을 동시에 잠금

팬텀 읽기 방지 목적

주로 `REPEATABLE READ` 격리 수준에서 사용됨

```sql
// 특정 레코드와 그 주변 간격에 락을 걸어 다른 트랜잭션 접근 방지 
SELECT * FROM employees WHERE emp_no = 100 FOR UPDATE;
```

#### 자동 증가 락(Autoinc Lock)

MySQL에서는 자동 증가 컬럼(`AUTO_INCREMENT`)을 사용하는데, 여러 INSERT/REPLACE 명령어가 실행될 때(새로운 레코드를 저장하는 쿼리) 

중복된 값이 생성되지 않도록 자동 증가 락이라고 하는 테이블 수준의 락을 검

자동 증가 값이 한 번 증가하면 줄어들지 않는 이유가 AUTO_INCREMENT 락을 최소화하기 위함임

실행 INSERT 쿼리가 실패했더라도 한 번 증가된 값은 줄어들지 않음

#### 레코드 락 vs 넥스트 키 락

레코드 락
- 배타 락 + 공유 락
- 특정 인덱스 레코드에 걸리는 락
- 특정 행에 대한 동시 접근 제어
- READ COMMITTED 격리 수준에선 레코드 락만 사용

넥스트 키 락
- 레코드 락 + 갭 락
- 특정 인덱스 레코드와 그 주변의 간격에 걸리는 락
- REPEATABLE READ 격리 수준에서 적용(팬텀 읽기 방지)

**락 모니터링**

information_schema 데이터베이스의 INNODB_TRX, INNODB_LOCKS, INNODB_LOCK_WAITS를 통해 트랜잭션 락을 확인하거나 클라이언트를 종료시킬 수 있음

Performance Schema를 통해 InnoDB 엔진 내부 락(세마 포어)에 대한 모니터링도 가능