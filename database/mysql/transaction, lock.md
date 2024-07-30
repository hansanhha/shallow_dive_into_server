[Transaction](#transaction)

[MySQL Engine Lock](#mysql-engine-lock)
- [Global Lock](#global-lock)
- [Backup Lock](#backup-lock)
- [Table Lock](#table-lock)
- [Named Lock](#named-lock)
- [Metadata Lock](#metadata-lock)

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
