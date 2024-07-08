## MySQL Server Architecture

<img src="./images/mysql architecture.png" alt="mysql architecture"/>

MySQL은 클라이언트-서버 아키텍처를 따름

**MySQL Client**
- MySQL Server와 상호작용하는 애플리케이션(standalone, web application등)
- Client에서 SQL 쿼리 전송 -> MySQL Server에서 처리

**MySQL Server**
- MySQL 데이터베이스 관리 시스템 - 데이터 저장, 관리, 처리 등
- SQL 쿼리를 받아서 처리하고 `result set`을 반환
- Server는 여러 클라이언트의 스토리지, 보안, 동시성 접근(concurrent access)를 관리함


### SQL Interface

사용자가 SQL 쿼리를 통해 MySQL 서버와 상호작용하는 곳

**주요 구성 요소**
- Connection Handler
    - MySQL 서버가 클라이언트 연결을 관리하는 부분
-