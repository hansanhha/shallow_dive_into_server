[Jeremy github](https://github.com/jeremycole/innodb_diagrams/blob/master/images/InnoDB_Structures.pdf)

[How InnoDB Handles Data on the Physical Disk](https://hidetatz.medium.com/how-innodb-writes-data-on-the-disk-1b109a8a8d14)

[Deep Dive Into InnoDB](https://medium.com/@sameersoin/deep-dive-into-data-storage-in-databases-the-innodb-engine-7ec0a55e3886)

## 간단한 DB 구현

**simple database**
- INSERT, UPDATE, DELETE
    - SQL 쿼리 요청
    - database server에서 직접 disk의 data file을 열고 write
    - 파일 닫기
    - 결과 응답
- SELECT
    - SQL 쿼리 요청
    - database server에서 직접 disk data file을 열고 read
    - 반환해야 할 결과를 모은 뒤 파일 닫기
    - 결과 응답

직접 table file에 접근하다보니 많은 disk i/o 발생이 되고 처리량이 떨어져 성능적으로 좋지 않음

또한 실제 data만 변경하는 게 아니라 index도 같이 변경되어야 함(b-tree 구조)

**memory cache database**


성능을 업그레이드 시키기 위해 database server와 disk 사이에 메모리에 캐시를 해보자
- INSERT, UPDATE, DELETE
    - SQL 쿼리 요청
    - database server는 메모리 캐시에 변경 사항을 write
    - 결과 응답
- SELECT
    - SQL 쿼리 요청
    - database server는 메모리 캐시에 data가 있는지 확인, 없다면 직접 disk의 file을 read
    - 결과 응답

위의 simple database보다 직접적으로 disk i/o를 하는 횟수가 줄기 때문에 성능이 향상되지만 트랜잭션 ACID 중 Durability를 보장할 수 없게 됨

Durability는 트랜잭션이 커밋되면 무슨 경우(정전 등)라도 데이터가 유지되어야 하는 속성임

disk와 데이터를 캐시한 memory 사이에서 writing 실패가 일어난 경우 커밋된 트랜잭션을 읽게 됨

**요약**

**simple database**의 경우 트랜잭션 ACID를 준수하지만 disk i/o가 빈번하게 일어나는 탓에 성능이 떨어지고

**memory cache database**의 경우 상대적으로 성능이 향상됐으나 트랜잭션 ACID 중 Durability 속성을 지킬 수 없음

MySQL InnoDB는 어떻게 성능을 향상시키면서 트랜잭션 ACID를 준수할까?

## InnoDB Architecture

InnoDB는 Buffer Pool과 Log Buffer 등을 통해 고성능을 유지하면서 트랜잭션 ACID도 준수함 (상세 구성요소는 아래 설명 참고)

<img src="./images/innodb architecture.png" alt="innodb architecture" style="width: 80%; height: 80%"/>

---

<img src="./images/innodb architecture2.png" alt="innodb architecture2" style="width: 90%; height: 90%"/>

## InnoDB Data Units

### Tablespace

tablespace는 하나 이상의 **data file**(binary file)을 저장하는 논리적 저장 컨테이너임

data file은 binary로 디스크에 저장되며 InnoDB의 기본 저장단위인 page 또는 block으로 구성됨

데이터베이스의 테이블과 인덱스같은 연관된(related) 데이터베이스 객체를 그룹화하고

InnoDB의 데이터 저장을 위한 최상위 구조 역할을 함(InnoDB의 테이블과 인덱스가 Tablespace에 저장됨)

**tablespace 종류**
- System Tablespace
  - MySQL의 기본 Tablespace로, data dictionary, change buffer, transaction log 등 중요한 시스템 데이터를 저장함
  - `ibdata1` 파일로 저장됨
  - <img src="./images/ibdata1.png" alt="ibdata1"/>
- General Tablespace
  - 하나의 Tablespace에 여러 테이블을 저장한 Tablespace
  - 특정 용도의 테이블들을 논리적으로 그룹화하여 저장하는 데 유용함
- File-per-Table Tablespace
  - 각 테이블과 인덱스를 별도의 파일로 저장하는 Tablespace
  - 테이블마다 `*.idb` 파일에 저장됨
  - <img src="./images/table idb.png" alt="table idb"/>
- Temporary Tablespace
  - 임시 데이터를 저장하는 데 사용되는 Tablespace
  - 임시 테이블, 중간 결과 등 저장
  - 글로벌 temp: `ibtmp1`, 세션별 temp: `temp_n.ibt`
  - <img src="./images/ibtmp1.png" alt="ibtmp1"/> <img src="./images/temp_ibt.png" alt="temp_ibt"/>

**Tablespace 구성 요소**
- Data page
  - Tablespace 내에서 데이터를 저장하는 기본 단위(일반적으로 16KB)
- Extent
  - 여러 Data Page를 묶은 단위로, 효율적으로 데이터 저장 공간을 관리하기 위해 사용
- Segment
  - Index Segment, Data Segment 등 Data Page와 Extent를 포함하는 논리적 그룹

### Pages

tablespace에 저장되는 각 data file는 page로 구성됨

page는 InnoDB에서 disk에 저장된 물리적인 binary data file을 다루는 기본 저장단위로 보통 16KB 사이즈를 가짐

테이블의 실제 row/index를 저장하는 요소로 각 page는 한 개 이상의 row나 index를 저장함 

**page 종류** 
- Data Pages  : 실제 table 데이터를 포함한 page
- Index Pages : B-tree 구조로 인덱스를 저장한 page
- Undo Pages  : 트랜잭션 롤백 시 사용될 데이터를 저장한 page
- System Pages: tablespace 자체에 대한 정보를 포함한 page

**page 구조**
- page header
- data area
- page trailer

### Blocks

보통 **"block"** 은 OS나 disk 수준에서 물리적 저장 단위를 일컬음

InnoDB에선 block을 고정된 크기의 데이터 단위로써 page와 혼용하여 표현함

### Relationship and Interaction between Tablespaces,Data files, Pages(Blocks) 

Hierarchy
- Tablespace > Data Files> Pages(Blocks)
- tablespaces  : data file에 대한 논리적 컨테이너
- data files   : pages(block)로 구성되어 디스크에 위치한 물리적 파일
- pages(blocks): 실제 데이터를 저장하는 기본 단위 

Physical and Logical Management
- tablespaces, data files: 스토리지 관리를 위한 논리적 구조
- pages: 스토리지 물리적 할당을 나타냄 

I/O Operations: 
- page 수준에서 read/write 작업 수행
- 단 하나의 row만 필요하거나 수정되는 경우에도 전체 page를 read/write함

### Clustered Index with Primary Key

InnoDB는 클러스터된 인덱스(일반적으로 기본 키)를 기반으로 테이블 데이터를 구성함

즉, 실제 데이터 row는 기본 키 순서로 저장됨

기본 키를 기반으로 한 쿼리(정렬이나 range scan)를 사용할 때 row에 접근하는 속도가 빨라짐

### Secondary Indexes

InnoDB의 보조 인덱스는 실제 row의 물리적 위치에 대한 포인터가 대신 해당 row의 기본 키 값을 저장함

## Buffer Pool, Log Buffer

매번 디스크 i/o 작업을 하면 성능이 떨어지므로 InnoDB는 메모리 영역에서 두 종류의 캐시를 사용해서 디스크 i/o 작업을 줄여 성능을 최적화함

Buffer Pool
- page(data page, index page 등)을 가지고 있는 메모리 영역
- LRU(Least Recently Used) 알고리즘 사용 (자주 사용되는 page는 보관하고, 적게 사용되는 page는 버리고 새로운 page 저장)
- 트랜잭션 커밋 시 변경사항은 **Dirty Page**로 표시되고 Buffer Pool의 Flush 리스트에 보관됨
 
Log Buffer
- Log 데이터를 가지고 있는 Buffer
- 시스템이 실패해도 트랜잭션 보장(무결성)을 위해 사용되며 트랜잭션 로그는 InnoDB의 Log와 동일함
- Log의 공식 명칭은 "Redo log"이며 `ib_log_file` 형태로 표현됨
- Log는 변경된 값을 포함하고 있어서 시스템이 실패한 경우, log 파일을 통해 복구할 수 있음
- "Binlog"는 MySQL server layer에서 작성되는 log로 storage engine의 Log와 전혀 다른 것임

## Log System in InnoDB

InnoDB는 데이터베이스 무결성, 일관성, 지속성을 위해 로그 파일에 변경사항을 기록함

로그 파일은 시스템 실패, 충돌, 복제, 추적, 트랜잭션 롤백, 재수행 등의 용도로 사용됨

### Write-Ahead Logging (WAL)

WAL은 데이터베이스가 데이터의 무결성(Integrity)과 지속성(Durability)를 보장하기 위해 사용되는 기본 개념임

**실제 물리 디스크에 반영하기 전에, 먼저 로그 파일에 변경사항을 로그**해야

시스템 실패, 충돌이 발생한 경우 데이터베이스는 로그를 통해 데이터를 복구하여 트랜잭션 ACID를 유지할 수 있음

Log Buffer와 Redo Log가 이 역할을 담당함

**InnoDB WAL 동작 과정**
- 트랜잭션 커밋으로 데이터 변경사항이 메모리의 Buffer Pool에 작성됨(dirty page)
- 변경사항이 disk에 반영되기 전에 Redo Log에도 기록함
- Buffer Pool의 데이터가 디스크의 데이터 파일에 flush
- 문제가 발생한 경우 InnoDB는 redo log를 사용해서 트랜잭션을 다시 실행해서 데이터베이스를 일관된 상태로 만듦

### Types of Log Files in InnoDB

#### Redo Logs (WAL Logs)

InnoDB의 WAL 시스템 주요 구성 요소로, 실제 디스크의 데이터 파일에 반영되기 전에 데이터 수준에서 발생된 모든 변경사항(insert, delete, update)을 기록함

시스템 충돌, 실패 시 데이터베이스 일관성 유지를 위해 트랜잭션을 다시 실행하는 데 사용됨

또한 데이터베이스 스키마(메모리를 거치지 않고 직접 disk에 작성) 변경사항(테이블의 물리적인 구조 변경)도 redo log에 기록됨 - 다만 복제에 관한 건 binary log에서 기록

#### Undo Logs

트랜잭션 롤백과 MVCC에서 사용되는 로그 파일로, DML(INSERT, UPDATE, DELETE) 트랜잭션에 의해 수정된 데이터베이스 레코드의 이전 값(old value)을 기록함

MVCC(Multi-Version Concurrency Control)에 사용되는 버전 관리 메커니즘을 제공

-> 특정 커넥션에서 데이터를 변경하는 도중에 다른 커넥션에서 데이터를 조회하면 트랜잭션 격리 수준에 맞게 변경 중인 레코드를 읽지 않고 언두 로그에 백업해둔 데이터를 읽어서 반환함 (격리 수준 보장)

**주의점**

오랜 시간동안 트랜잭션이 실행되면 언두 로그의 데이터를 유지해야 하므로 다른 트랜잭션의 언두 로그를 삭제하지 못한 채로 언두 로그에 저장되는 데이터 양이 급격히 늘어날 수 있음

빈번하게 변경된 레코드를 조회하는 쿼리가 실행되면 InnoDB 스토리지 엔진은 언두 로그의 이력을 필요한 만큼 스캔해야 하므로 필요한 레코드를 찾을 수 있기 때문에 쿼리의 성능이 떨어짐

**언두 테이블스페이스와 롤백 세그먼트**

언두 로그가 저장되는 공간

언두 테이블 스페이스 당: 1~128개 롤백 세그먼트를 가짐

롤백 세그먼트 당: InnoDB의 페이지 크기를 16바이트로 나눈 값의 개수만큼 언두 슬롯을 가짐

페이지 크기가 16KB인 경우: (16 * 1024) / 16 = 1024개의 언두 슬롯

```text
mysql> SELECT TABLESPACE_NAME, FILE_NAME
    -> FROM INFORMATION_SCHEMA.FILES
    -> WHERE FILE_TYPE LIKE 'UNDO LOG';
+-----------------+------------+
| TABLESPACE_NAME | FILE_NAME  |
+-----------------+------------+
| innodb_undo_001 | ./undo_001 |
| innodb_undo_002 | ./undo_002 |
+-----------------+------------+
2 rows in set (0.01 sec)
```

**최대 동시 트랜잭션 수**

InnoDB 페이지 크기(기본 값: 16KB) / 16 * 롤백 세그먼트 개수(기본 값: 128) * 언두 테이블 스페이스 개수(기본 값: 2)

= 131072(16 * 1024 / 16 * 128 * 2 /2)

#### Binary Logs (Binlogs)

MySQL의 복제(replication), 데이터 복구 절차에 사용되는 로그 파일

데이터베이스의 모든 변경사항을 기록함 (redo log와 달리 다른 데이터베이스로의 복제에 적합한 형식으로 기록)

또한 모든 DDL 변경사항을 기록하여 master에서 실행된 스키마 변경사항을 slave 데이터베이스에도 적용할 수 있도록 하거나 추적을 가능하게 함

#### General Query and Slow Query Logs

모니터링, 트러블 슈팅 용 로그 파일

general query log: server가 받은 모든 쿼리 기록

slow qeury log: 실행 시간이 특정 시간보다 더 오래 걸린 쿼리 기록

## Transaction Manangement

### Checkpoint

데이터 무결성을 유지하고 시스템 실패로 인해 로그 파일로부터 데이터를 복구해야 될 때 복구 시간을 줄이기 위해 사용되는 메커니즘으로, Buffer Pool에 있는 Dirty page를 disk에 주기적으로 기록하여 데이터 일관성을 보장함

Checkpoint가 발생하면 InnoDB는 log 파일과 tablespace file의 동기화를 위해 Buffer Pool의 dirty page들을 tablespace file에 flush하고 log 파일에 "Checkpoint record"를 write함

Checkpoint 알고리즘 중 InnoDB는 "Fuzzy checkpoint" 알고리즘을 사용함

## Multi-Version Concurrency Control (MVCC)

MVCC는 read operation 시 row를 잠구지 않고 높은 트랜잭션 처리량과 일관성을 달성하기 위해 InnoDB에서 사용하는 동시성 제어 방법임

주 목적은 잠금을 사용하지 않는 일관된 읽기를 제공하는 데 있음

**Versioning Mechanism**

row를 여러 가지 버전으로 관리해서, 트랜잭션 격리 수준(isolation)에 따라 특정 시점의 데이터베이스 스냅샷을 확인함

다른 트랜잭션의 작업이 완료되기를 기다리지 않고 여러 트랜잭션이 동시에 read/write를 수행할 수 있음

- 예시
    - A 트랜잭션이 row를 업데이트
    - 해당 row는 undo log에 원래 버전이 기록되고, 새로운 버전은 buffer pool에 저장됨
    - B 트랜잭션에서 row를 읽을 때 `READ_UNCOMMITTED`인 경우 buffer pool의 새로운 버전을 읽음
    - `READ_COMMITTED` 인 경우 undo log의 원래 버전을 읽음

**Read Views**

트랜잭션이 데이터를 읽을 때 일관적인 스냅샷을 볼 수 있도록 InnoDB가 "read view"를 생성함으써 트랜잭션에 표시되는 row의 버전을 식별함

**Row Versions**

트랜잭션이 row를 수정할 때 기존 데이터를 덮어쓰지 않고, 새로운 row version을 **undo log**에 기록함

다른 트랜잭션에서 변경사항이 반영되기 전의 오리지널 버전을 확인할 수 있음

**Visibility Check**

row에 접근하기 전 InnoDB는 visibility check를 수행해서 기다려야 되는지, 현재 버전이나 이전 버전 확인해야 되는지 결정함

**Purge Process**

InnoDB는 주기적으로 더이상 필요없는 오래된 버전의 row(undo log)를 삭제하여 공간을 비우고 성능을 유지함

오랜 시간 동안 특정 트랜잭션이 유지되면 undo log를 삭제하지 못하고 유지해야 하기 때문에 문제가 발생할 수 있다

## Double Write Buffer

리두 로그는 변경된 내용만 기록하므로 시스템 실패/충돌 발생 시 일부분만 기록될 수 있기 때문에

InnoDB는 tablespace file에 dirty page를 기록할 때 변경된 데이터를 모두 모아서 먼저 system tablespace의 **"Double Write Buffer"** 이라는 곳에 변경사항을 기록함

이후 적절한 tablespace에 dirty page를 기록

double write buffer는 메모리가 아닌 "disk"에 위치하며 시스템 실패로 인해 데이터가 깨지거나 일부분만 write된 경우 log file 이전에 복구하는 용도로 사용됨

## Change Buffer

INSERT, DELETE의 작업으로 데이터 파일을 변경하는 작업은 인덱스를 업데이트하는 작업을 필요로 함

InnoDB의 인덱스 업데이트 동작
- 인덱스 페이지가 버퍼 풀에 있으면 즉시 업데이트 수행
- 그렇지 않은 경우 체인지 버퍼에 두고 클라이언트에 결과 반환
    - 다만 중복 여부를 반드시 체크해야 하는 유니크 인덱스의 경우 체인지 버퍼를 사용하지 않음

이후 머지 스레드(Merge Thread)에 의해 인덱스 레코드가 디스크에 반영됨

체인지 버퍼는 버퍼 풀로 설정된 메모리 공간의 25%까지 사용(설정을 통해 조정 가능)

## Transaction Commit Synchronazation Component

시스템 비정상 종료, 충돌 등으로 인해 복구하거나

데이터 무결성을 지키기 위해 트랜잭션 커밋 시 복구 컴포넌트에 내용을 반영해야 함

동기화 필요 컴포넌트: Double Write Buffer, Redo Log, Binary Log 등

## Adaptive Hash Index

InnoDB 성능 최적화를 위한 기술로, 자주 접근하는 페이지의 특정 부분을 메모리에 유지하여 검색 속도를 높임

버퍼 풀에 존재하는 특정 페이지의 접근 빈도가 높아지면 InnoDB는 해당 페이지에 대한 해시 인덱스를 생성함

이후 동일한 데이터를 검색할 때, 해시 인덱스를 사용하여 빠르게 접근 가능

**장점**

해시 인덱스를 통해 데이터에 빠르게 접근 가능

**단점**

메모리 내에서 동작하므로 메모리 사용량을 증가시킴

데이터 페이지의 인덱스 키를 해시 인덱스로 만들어야 됨

불필요한 경우나 테이블이 변경/삭제된 경우 해당 테이블과 관련된 모든 내용을 어댑티브 해시 인덱스 제거 해야됨

**도움되는 경우**

디스크 읽기가 많지 않은 경우

동등 조건 검색(동등 비교, IN 연산자)이 많은 경우

쿼리가 데이터 중에서 일부 데이터에만 집중되는 경우

**도움되지 않는 경우**

조인이나 LIKE 패턴 검색같이 특정 패턴의 쿼리가 많은 경우

디스크 읽기가 많은 경우

매우 큰 데이터를 가진 테이블의 레코드를 폭넓게 읽는 경우


## Data Operation

**Insert, Update, Delete**

데이터 삽입, 수정, 삭제 시 InnoDB는 Buffer Pool의 page를 수정함

변경사항은 제어된 방식으로 I/O 작업을 최적화 디스크에 플러시됨  

page가 데이터 삽입으로 가득차면 page가 분할되고 데이터가 재배포되어 인덱스의 b-tree 구조를 유지함 

**read**

InnoDB는 데이터를 가져올 때 가장 먼저 Buffer Pool을 확인하고, 만약 없다면 Tablespace file에 접근하여 데이터를 가져온 뒤 Buffer Poll에 저장하면서 결과를 반환함

Tablespace file은 disk에 저장된 실제 데이터와 인덱스를 가진 파일로 `*.idb` 확장자를 가짐

**write**

트랜잭션이 데이터 변경 작업을 수행할 때, 해당 변경 사항은 먼저 Log Buffer에 기록됨

Log Buffer가 가득 차거나 트랜잭션이 커밋되면 Log 파일들은 disk에 flush됨

데이터는 Buffer Pool의 dirty page로 표시되며 Checkpoint가 발생하면서 disk에 flush되는데,

Double Write Buffer를 활성화한 경우 dirty page는 Double Write Buffer에 작성되며, 이후에 tablespace에 fsync됨

그리고 log file에 "Checkpoint Record"를 작성함(시스템 실패 시 이 부분부터 복구 시작)
