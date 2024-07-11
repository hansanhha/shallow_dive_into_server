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

### Data Processing process

MySQL에서의 데이터 작업은 크게 SQL 레이어와 Storage Engine 레이어로 나뉨

전체적인 데이터 처리 과정은 다음과 같음

**1. SQL Layer에서 SQL문 파싱 및 최적화**
- Client -> MySQL Server: SQL문 전송 
- SQL Parser: 파싱 후 구문 트리 생성
- Optimizer: 구문 트리를 기반으로 최적화된 실행 계획 수립

**2. 실행 계획에 따라 Handler API를 호출**
- 실행 계획에 따른 적절한 스토리지 엔진 선택(각 "테이블"과 연관되어 있는 Storage Engine을 선택)
- 실행 계획의 각 단계에서, MySQL Server는 Storage Engine의 "Handler API"를 호출하여 데이터 작업 수행

**3. Storage Engine의 Handler API를 통한 데이터 작업**
- Storage Engine은 Handler API를 구현하여 실제 데이터 작업 처리(read, write, update, delete 등)
- 물리적인 디스크로부터 데이터를 메모리에 로드하여 읽거나 씀
- Handler API를 호출한 곳에서 필요한 데이터를 반환함

**4. 결과 반환**
- Storage Engine의 작업 처리에 대한 결과를 MySQL Server는 Result Set으로 변환
- 최종 Result Set을 Client에게 반환

### MySQL Server daemon processes (SQL Layer)

**NoSQL**

비정형(unstructured) 또는 반정형(semi-structured) 데이터에 자주 사용되는 스키마 없는 데이터 저장소를 관리함

**SQL Interface**

사용자가 SQL 쿼리를 사용하여 관계형 데이터베이스를 상호작용하도록 표준 인터페이스를 정의함

**Query Parser**

SQL 쿼리 분석을 통해 구조와 구문 파악, 추가 처리를 위해 구성 요소로 분해하는 역할

**Query Optimizer**

주어진 쿼리에 대한 실행 계획 평가 후 성능적으로 가장 효율적인 실행 계획을 선택하여 데이터베이스 작업 성능 향상

**Cache & Buffers**

자주 접근하는 데이터 또는 쿼리 결과를 메모리에 저장하여 성능 향상

### Pluggable Storage Engine Architecture (Storage Engine Layer)

MySQL의 각 Storage Engine은 물리적인 Server 단계에서 실질적으로 데이터 저장, 검색 및 관리를 수행하는 컴포넌트임

스토리지 레벨에서의 implementation 디테일과 개발자/DBA로부터 분리하여 SQL Interface와 전반적인 데이터베이스 기능은 유지하면서 특정 성능 및 확장성 요구사항을 충족함   

여러 스토리지 엔진을 선택해서 사용할 수 있는 "**pluggable** storage engine architecture"이라고 하는데

이 구조 덕분에 개발자나 DBA가 요구사항(data warehousing, transaction processing, high availability 등)에 따라 특정 엔진을 사용하기 위해 별도로 변경해야 될 작업없이 일관적인 API를 사용하면서 내부적인 동작 방식을 바꿀 수 있음

### Handler

Storage Engine은 MySQL의 [Handler](https://dev.mysql.com/doc/dev/mysql-server/latest/dir_e3dbcb4c59dff57aadc278c62af915a5.html) 인터페이스를 구현하여 다양한 데이터 작업을 수행하는 실질적인 역할을 함

각 엔진은 자체적인 방식으로 데이터를 관리하며 Handler API를 통해 MySQL Server와 상호작용을 함

## MySQL Server Thread-Based Architecture

**3 key concepts**
- internal workings
- connection handling
- performance optimization

