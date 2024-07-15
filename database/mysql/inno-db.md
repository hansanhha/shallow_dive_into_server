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

## InnoDB Architecture, Components

<img src="./images/innodb architecture.png" alt="innodb architecture" style="width: 80%; height: 80%"/>

---

<img src="./images/innodb architecture2.png" alt="innodb architecture2" style="width: 90%; height: 90%"/>

MySQL의 기본 Storage Engine

### Tablespace

데이터베이스의 데이터와 인덱스를 물리적 디스크에 저장하는 논리적 저장 구조로, InnoDB의 테이블과 인덱스가 Tablespace에 저장됨

**Tablespace 종류**
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
  
### Buffer Pool, Log Buffer

물리적인 disk에 접근하는 i/o 작업은 느리기 때문에 InnoDB는 두 종류의 캐시를 사용하는데, 이 캐시들은 메모리에서 작동함

Buffer Pool
- LRU 캐시로 동작
- 스레드 풀처럼 Buffer(실제 데이터)를 가지고 있는 Buffer Pool 

Log Buffer
- Log 데이터를 가지고 있는 Buffer
- 시스템이 실패해도 트랜잭션 보장(무결성)을 위해 사용되며 트랜잭션 로그는 InnoDB의 Log와 동일함
- Log의 공식 명칭은 "Redo log"이며 `ib_log_file` 형태로 표현됨
- Log는 변경된 값을 포함하고 있어서 시스템이 실패한 경우, log 파일을 통해 복구할 수 있음
- "Binlog"는 MySQL server layer에서 작성되는 log로 storage engine의 Log와 전혀 다른 것임

### Buffer Pool's Page, Checkpoint

**Page**

page는 InnoDB에서 disk에 저장된 물리적인 binary data file을 다루는 기본 저장단위임

page를 구성하는 각 data file은 보통 16KB 사이즈를 가짐

page 종류
- Data Pages  : 실제 table 데이터를 포함한 page
- Index Pages : B-tree 구조로 인덱스를 저장한 page
- Undo Pages  : 트랜잭션 롤백 시 사용될 데이터를 저장한 page
- System Pages: tablespace 자체에 대한 정보를 포함한 page 

데이터 변경이 적용되면 변경사항은 disk의 tablespace가 아닌 Buffer Pool에만 적용됨

Buffer Pool은 여러 개의 page로 구성되어 있는데, 해당 변경사항의 page를 **"Dirty Page"**로 표시함

**Checkpoint**

데이터 무결성을 유지하고 시스템 실패로 인해 로그 파일로부터 데이터를 복구해야 될 때 복구 시간을 줄이기 위해 사용되는 메커니즘으로, Buffer Pool에 있는 Dirty page를 disk에 주기적으로 기록하여 데이터 일관성을 보장함

Checkpoint가 발생하면 InnoDB는 log 파일과 tablespace file의 동기화를 위해 Buffer Pool의 page들을 tablespace file에 flush하고 log 파일에 "Checkpoint record"를 write함

Checkpoint 알고리즘 중 InnoDB는 "Fuzzy checkpoint" 알고리즘을 사용함

### Double Write Buffer

InnoDB가 tablespace file에 dirty page를 write할 때 내부적으로 가장 먼저 **"Double Write Buffer"** 이라는 곳에 변경사항을 write함

이후 tablespace file에 fsync함

double write buffer는 메모리가 아닌 "disk"에 위치하며 시스템 실패로 인해 데이터가 깨지거나 일부분만 write된 경우 log file 이전에 복구하는 용도로 사용됨

## Read, Write Operation

**read**

InnoDB는 데이터를 가져올 때 가장 먼저 Buffer Pool을 확인하고, 만약 없다면 Tablespace file에 접근하여 데이터를 가져온 뒤 Buffer Poll에 저장하면서 결과를 반환함

Tablespace file은 disk에 저장된 실제 데이터와 인덱스를 가진 파일로 `*.idb` 확장자를 가짐

**write**

트랜잭션이 데이터 변경 작업을 수행할 때, 해당 변경 사항은 먼저 Log Buffer에 기록됨

Log Buffer가 가득 차거나 트랜잭션이 커밋되면 Log 파일들은 disk에 flush됨

데이터는 Buffer Pool의 dirty page로 표시되며 Checkpoint가 발생하면서 disk에 flush되는데,

Double Write Buffer를 활성화한 경우 dirty page는 Double Write Buffer에 작성되며, 이후에 tablespace에 fsync됨

그리고 log file에 "Checkpoint Record"를 작성함(시스템 실패 시 이 부분부터 복구 시작)
