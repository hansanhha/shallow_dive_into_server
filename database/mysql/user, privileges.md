## 사용자 식별

Mysql User Format: `'username'@'hostname'`

username: 사용자 아이디

hostname: 사용자 접속 지점(클라이언트가 실행된 호스트명, 도메인 또는 IP 주소)

**로컬 호스트에서만 접속 가능한 id**

`'lorens_id'@'127.0.0.1'`

**모든 호스트에서 접속 가능한 id**

`'lorens_id'@'%'`: %는 모든 IP 또는 모든 호스트명을 의미

**동일한 id가 있는 경우** 

좁은 범위의 host를 가진 id가 먼저 적용됨

`'lorens_id'@'192.168.0.2'`, `'lorens_id'@'%'` : 192.168.0.2 호스트에서 접속 시 `'lorens_id'@'192.168.0.2'`가 먼저 선택됨

## 사용자 계정 관리

### 계정 테이블

MySQL은 mysql.user이라는 테이블에 계정을 보관함

### 시스템 계정, 일반 계정

시스템 계정: SYSTEM_USER 권한 보유 (DBA용)

일반 계정: SYSTEM_USER 권한 미보유 (개발자용)

**SYSTEM_USER 권한**
- 계정 관리: 계정 생성 및 삭제/계정 권한 부여 및 제거
- 다른 세션(connection) 또는 해당 세션에서 실행 중인 쿼리 강제 종료
- 스토어드 프로그램 생성 시 DEFINER 설정

**기본 내장 계정**
- `'root'@'localhost'`
- `'mysql.sys'@'localhost'`: MySQL 8.0부터 내장된 sys 스키마 객체(뷰, 함수, 프로시저)들의 DEFINER로 사용되는 계정
- `'mysql.session'@'localhost'`: MySQL 플러그인이 서버로 접근할 때 사용되는 계정
- `'mysql.infoschema'@'localhost'`: information_schema에 정의된 뷰의 DEFINER로 사용되는 계정

### 계정 생성

계정 생성 관련 옵션
- 인증 방식
- 비밀번호 및 관련 옵션(비밀번호 유효기간, 비밀번호 이력 개수, 비밀번호 재사용 불가 기간)
- 역할(Role)
- SSL 옵션
- 계정 잠금 여부

```mysql
CREATE USER 'lorens'@'%'
    IDENTIFIED WITH 'mysql_native_passoword' BY 'password'
    REQUIRE NONE
    PASSWORD EXPIRE INTERVAL 30 DAY
    ACCOUNT UNLOCK 
    PASSWORD HISTORY DEFAULT 
    PASSWORD REUSE INTERVAL DEFAULT 
    PASSWORD REQUIRE CURRENT DEFAULT;
```

**IDENTIFIED WITH**
- 사용자 인증방식 및 비밀번호 설정
- `IDENTIFIED WITH '인증 방식(인증 플러그인 이름)'` or `IDENTIFIED BY 'password'` (기본 인증 방식 사용)
- 인증 플러그인
  - Native Pluggable Authentication : 비밀번호 해시 값 저장, 클라이언트가 보낸 값과 일치하는 지 비교하는 인증 방식
  - Caching SHA-2 Pluggable Authentication : 위의 방식과 유사하나 암호화 해시값 생성을 위해 SHA-2(256) 알고리즘 사용 및 MySQL 서버에서 해시 결과값을 메모리에 캐시하며 SSL/TLS 또는 RSA 키페어 필수
  - PAM Pluggable Authentication: 외부 인증 사용 방식, 엔터프라이즈 에디션에서만 사용 가능
  - LDAP Pluggable Authentication: 외부 인증 사용 방식, 엔터프라이즈 에디션에서만 사용 가능
- MySQL 8.0 기본 인증 방식: **Caching SHA-2 Pluggable Authentication**

<img src="./images/default authentication plugin.png" alt="default authentication plugin" width="50%" height="50%"/>



