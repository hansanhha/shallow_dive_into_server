## InnoDB

MySQL의 기본 Storage Engine

## InnoDB Architecture Overview

<img src="./images/innodb architecture.png" alt="innodb architecture"/>

---

<img src="./images/innodb architecture2.png" alt="innodb architecture2"/>

[출처](https://github.com/jeremycole/innodb_diagrams/blob/master/images/InnoDB_Structures.pdf)

## [How InnoDB Handles Data on the Physical Disk](https://hidetatz.medium.com/how-innodb-writes-data-on-the-disk-1b109a8a8d14)

### 간단한 DB 구현

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

### InnoDB


