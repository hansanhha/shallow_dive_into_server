[MySQL Server Architecture](#mysql-server-architecture)
- [SQL Layer(Server Layer)](#sql-layer-server-layer)
- [Storage Layer](#storage-layer---pluggable-storage-engine-architecture)
- [Handler](#handler)
- [Other Components](#other-components)
- [Plugin Architecture](#plugin-architecture)
- [Component: Advanced Plugin](#component-advanced-plugin)

**[Data Processing process](#data-processing-process)**

[MySQL Server Thread-Based Architecture](#mysql-server-thread-based-architecture)

[Memory Area](#memory-area)

## MySQL Server Architecture

<img src="./images/mysql architecture.png" alt="mysql architecture"/>

MySQL은 클라이언트-서버 아키텍처를 따름

**MySQL Client**
- MySQL Server와 상호작용하는 애플리케이션(standalone, web application등)
- Client에서 SQL 쿼리 전송 -> MySQL Server에서 처리
- MySQL 프로토콜과 `Connector`를 사용하여 네트워크를 거쳐 Server와 통신

**MySQL Server**
- MySQL 데이터베이스 관리 시스템 - 데이터 저장, 관리, 처리 등
- SQL 쿼리를 받아서 처리하고 `result set`을 반환
- Server는 여러 클라이언트의 스토리지, 보안, 동시성 접근(concurrent access)를 관리함

### SQL Layer (Server Layer)

**NoSQL**

비정형(unstructured) 또는 반정형(semi-structured) 데이터에 자주 사용되는 스키마 없는 데이터 저장소를 관리함

**SQL Interface**

사용자가 SQL 쿼리를 사용하여 관계형 데이터베이스를 상호작용하도록 표준 인터페이스를 정의함

**Query Parser**

SQL 쿼리 분석을 통해 구조와 구문 파악, 추가 처리를 위해 구성 요소(트리 형태)로 분해하는 역할

기본 문법 오류 발생 시 Client에게 오류 메시지 반환

**Preprocessor**

쿼리 파서가 만든 파서 트리 점검

각 토큰을 테이블 이름이나 컬럼 이름, 또는 내장 함수와 같은 객체를 매핑해 해당 객체의 존재 여부 및 접근 권한 확인

존재하지 않거나 접근할 수 없는 토큰은 걸러짐

**Query Optimizer**

주어진 쿼리에 대한 실행 계획 평가 후 성능적으로 가장 효율적인 실행 계획을 선택하여 데이터베이스 작업 성능 향상

**Cache & Buffers**

자주 접근하는 데이터 또는 쿼리 결과를 메모리에 저장하여 성능 향상

**Query Executor**

실행 계획에 따라 Handler에게 실행 요청 위임

핸들러에게 받은 결과를 Client에게 반환하거나 또 다른 Handler 요청의 입력으로 연결

### Storage Layer - Pluggable Storage Engine Architecture

MySQL의 각 Storage Engine은 실질적으로 디스크에 데이터 저장, 검색 및 관리를 수행하는 컴포넌트임

실제 구현 디테일을 사용자(개발자/DBA)로부터 분리하여 SQL Interface와 전반적인 데이터베이스 기능은 유지하면서 특정 성능 및 확장성 요구사항을 충족함   

여러 스토리지 엔진을 선택해서 사용할 수 있는 "**pluggable** storage engine architecture"이라고 하는데

이 구조 덕분에 개발자나 DBA가 요구사항(data warehousing, transaction processing, high availability 등)에 따라 특정 엔진을 사용하기 위해 별도로 변경해야 될 작업없이 일관적인 API를 사용하면서 내부적인 동작 방식을 바꿀 수 있음

### Handler

Storage Engine은 MySQL의 [Handler](https://dev.mysql.com/doc/dev/mysql-server/latest/dir_e3dbcb4c59dff57aadc278c62af915a5.html) 인터페이스를 구현하여 다양한 데이터 작업을 수행하는 실질적인 역할을 함

각 엔진은 자체적인 방식으로 데이터를 관리하며 Handler API를 통해 MySQL Server와 상호작용을 하는데, 테이블마다 사용할 엔진을 선택할 수 있음

### Other Components

#### Replication

#### Query Cache

쿼리 결과를 메모리에 캐시하고 동일 SQL 쿼리가 실행되면 캐시된 결과를 반환하는 요소

다만 테이블의 데이터가 변경되면 캐시된 것들은 모두 삭제해야 했으므로 성능 저하를 유발하여 8.0 버전부터 삭제됨

#### Transaction Supporting Metadata

**메타데이터** 또는 **데이터 딕셔너리**: 테이블 구조 정보나 스토어드 프로그램

**시스템 테이블**: MySQL 서버가 작동하는 데 기본적으로 필요한 테이블 

메타데이터는 파일 기반이므로 MySQL 5.7 버전까지 트랜잭션 지원이 되지 않았음

8.0 버전부터 트랜잭션을 지원하기 위해 메타데이터와 시스템 테이블 모두 `mysql` DB의 InnoDB를 사용한 테이블에 저장함 

`mysql` DB는 mysql.idb에 저장되고 Server의 각 Database의 테이블 별로 idb 파일이 존재함

**window 기준 idb 데이터 접근 경로**
- `C:\ProgramData\MySQL\MySQL Server 8.1\Data`

<img src="./images/table idb.png" alt="table idb"/>


### Plugin Architecture

스프링의 PSA처럼 MySQL은 다형성을 활용하여 특정 모듈을 원하는 대로 갈아끼울 수 있다 

이러한 모듈을 Plugin이라고 부르며 여러 종류의 플러그인을 MySQL Server에 런타임에 로드/언로드할 수 있다

각 플러그인은 타입에 따라 기능을 제공한다 [Types of Plugins](https://dev.mysql.com/doc/extending-mysql/8.4/en/plugin-types.html)

[MySQL Server Plugin](https://dev.mysql.com/doc/refman/8.4/en/server-plugins.html)

### Component: Advanced Plugin

플러그인의 단점을 보완한 아키텍처
- 플러그인 간 통신 불가(MySQL서버와 인터페이스만 가능)
- 플러그인 간 상호 의존 관계 설정 불가
- MySQL 서버의 변수, 함수 직접 호출

[MySQL Components](https://dev.mysql.com/doc/refman/8.4/en/components.html)

## Data Processing process

MySQL에서의 데이터 작업은 크게 SQL 레이어와 Storage Engine 레이어로 나뉨

전체적인 데이터 처리 과정은 다음과 같음

**1. Client - Server 커넥션 연결**
- Client -> MySQL Server: Connect 요청
- Connection Manager: 스레드 캐시 확인 후 요청을 처리할 스레드 할당
- 새 커넥션 생성

**2. SQL Layer에서 SQL문 파싱 및 최적화**
- Client -> MySQL Server: SQL문 전송
- SQL Parser: 파싱 후 구문 트리 생성
- Optimizer: 구문 트리를 기반으로 최적화된 실행 계획 수립

**3. 실행 계획에 따라 Handler API를 호출**
- 실행 계획에 따른 적절한 스토리지 엔진 선택(각 "테이블"과 연관되어 있는 Storage Engine을 선택)
- 실행 계획의 각 단계에서, MySQL Server는 Storage Engine의 "Handler API"를 호출하여 데이터 작업 수행

**4. Storage Engine의 Handler API를 통한 데이터 작업**
- Storage Engine은 Handler API를 구현하여 실제 데이터 작업 처리(read, write, update, delete 등)
- 물리적인 디스크로부터 데이터를 메모리에 로드하여 읽거나 씀
- Handler API를 호출한 곳에서 필요한 데이터를 반환함

**5. 결과 반환**
- Storage Engine의 작업 처리에 대한 결과를 MySQL Server는 Result Set으로 변환
- 최종 Result Set을 Client에게 반환

## MySQL Server Thread-Based Architecture

[참고](https://minervadb.xyz/understanding-mysqls-thread-based-architecture-internal-workings-connection-handling-and-performance-optimization/)

**3 key concepts**
- internal workings
- connection handling
- performance optimization

**Connection Handling Thread**

MySQL은 클라이언트가 Server에 연결되면 커넥션 별로 스레드를 새로 생성하거나 스레드 캐시로부터 하나를 할당함

스레드 캐시는 스레드 생성 오버헤드를 줄이기 위해 스레드를 보관하는 캐시 역할을 함

클라이언트에 할당된 스레드는 다음의 처리를 수행함
- 파싱
- 실행
- 결과 반환

**Main Threads**

- Main Thread
  - global task 처리 스레드(버퍼 풀의 dirty page flushing 등)
- I/O Threads
  - InnoDB에서 read/write 처리 시 사용하는 스레드들(log file write, 버퍼 풀 read 포함)
- Utility Threads
  - replication, delayed row handling, event scheduling 같은 작업을 처리하는 스레드들

**Thread States**

- `query end`
- `sending data`
- `waiting for table flush`

**Query Execution Threads**

- 파싱, 실행 계획 스레드
- storage engine 스레드와 상호작용하는 실행 스레드

**InnoDB Internal Threads**

- read/write 처리 스레드
- background flushing 스레드
- log write 스레드

**Resource Management Threads**

- 메모리, lock 등 리소스 관리 스레드
- storage engine의 locking 메커니즘과 상호작용하여 transaction isolation, consistency을 관리하는 스레드

## Memory Area

모든 스레드에 의해 **공유**되는 글로벌 메모리 영역과 **세션 당** 생성되는 로컬 메모리 영역으로 구분

**글로벌 메모리 영역**
- 테이블 캐시
- InnoDB 버퍼 풀
- InnoDB 어댑티브 해시 인덱스
- InnoDB 리두 로그 버퍼

**로컬 메모리 영역**
- 정렬 버퍼
- 조인 버퍼
- 바이너리 로그 캐시
- 네트워크 버퍼