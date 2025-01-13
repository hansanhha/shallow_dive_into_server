[User Identification](#user-identification)

[Privilege](#privilege)

[Role](#role)

## User Identification

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

## User Management

### User Table

MySQL 내부적인 데이터는 MySQL의 기본 스키마인 mysql에 존재함

MySQL의 계정은 mysql.user 테이블에서 확인 가능

### SYSTEM USER, NORMAL USER

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

### CREATE USER

계정 생성 관련 옵션
- 인증 방식
- 비밀번호 및 관련 옵션(비밀번호 유효기간, 비밀번호 이력 개수, 비밀번호 재사용 불가 기간)
- 역할(Role)
- SSL 옵션
- 계정 잠금 여부

```mysql
CREATE USER 'lorens'@'%'
    IDENTIFIED WITH 'mysql_native_password' BY 'password'
    REQUIRE NONE
    PASSWORD EXPIRE INTERVAL 30 DAY
    ACCOUNT UNLOCK 
    PASSWORD HISTORY DEFAULT 
    PASSWORD REUSE INTERVAL DEFAULT 
    PASSWORD REQUIRE CURRENT DEFAULT;
```

**IDENTIFIED WITH**
- 사용자 인증방식 및 비밀번호 설정 옵션
- `IDENTIFIED WITH '인증 방식(인증 플러그인 이름)'` or `IDENTIFIED BY 'password'` (기본 인증 방식 사용)
- 인증 플러그인
  - Native Pluggable Authentication : 비밀번호 해시 값 저장, 클라이언트가 보낸 값과 일치하는 지 비교하는 인증 방식
  - Caching SHA-2 Pluggable Authentication : 위의 방식과 유사하나 암호화 해시값 생성을 위해 SHA-2(256) 알고리즘 사용 및 MySQL 서버에서 해시 결과값을 메모리에 캐시하며 SSL/TLS 또는 RSA 키페어 필수
  - PAM Pluggable Authentication: 외부 인증 사용 방식, 엔터프라이즈 에디션에서만 사용 가능
  - LDAP Pluggable Authentication: 외부 인증 사용 방식, 엔터프라이즈 에디션에서만 사용 가능
- MySQL 8.0 기본 인증 방식: **Caching SHA-2 Pluggable Authentication**

<img src="./images/default authentication plugin.png" alt="default authentication plugin" width="50%" height="50%"/>

**REQUIRE**
- MySQL 서버에 접속할 때 SSL/TLS 채널을 사용 옵션
- 기본 값: 비암호화 채널 사용
- 다만 Caching SHA-2 Authentication 방식 사용 시 암호화된 채널만으로 접속

**PASSWORD EXPIRE**
- 비밀번호 유효기간 설정 옵션
- 기본 값: `default_password_lifetime` 시스템 변수에 저장된 기간으로 유효 기간 설정
- `PASSWORD EXPIRE INTERVAL n DAY`: 비밀번호 유효 기간을 오늘부터 n일자로 설정
- `PASSWORD EXPIRE NEVER`: 비밀번호 만료 기간 없음

**PASSWORD HISTORY**
- 비밀번호 이력을 저장하여 재사용하지 못하게 하는 옵션
- `PASSWORD HISTORY DEFAULT`: `password_history` 시스템 변수에 저장된 개수만큼 비밀번호 이력 저장
- `PASSWORD HISTORY n`: 비밀번호의 이력을 최근 n개까지만 저장
- password_history 테이블에 비밀번호 이력을 저장해둠

**PASSWORD REUSE INTERVAL**
- 한 번 사용했던 비밀번호 재사용 금지 기간 설정 옵션
- 기본 값: `password_reuse_interval` 시스템 변수에 저장된 기간으로 설정
- `PASSWORD REUSE INTERVAL DEFAULT`: `password_reuse_interval` 변수에 저장된 기간으로 설정
- `PASSWORD REUSE INTERVAL n DAY`: n일자 이후에 비밀번호 재사용 가능 설정

**PASSWORD REQUIRE**
- 비밀번호 만료 시 새로운 비밀번호를 변경할 때 현재 비밀번호를 필요로 하는지 결정하는 옵션
- 기본 값: `password_require_current` 시스템 변수 값으로 설정
- `PASSWORD REQUIRE DEAFULT`: `password_require_current` 시스템 변수 값으로 설정 
- `PASSWORD REQUIRE OPTIONAL`: 비밀번호 변경 시, 현재 비밀번호를 입력하지 않아도 되도록 설정
- `PASSWORD REQUIRE CURRENT`: 비밀번호 변경 시, 현재 비밀번호를 먼저 입력하도록 설정

**ACCOUNT LOCK/UNLOCK**
- 계정을 사용하지 못하게 하는 옵션
- `ACCOUNT LOCK`: 계정 잠금
- `ACCOUNT UNLOCK`: 계정 잠금 해제

## Privilege

### Types of Privileges

**정적 권한**
- MySQL 서버의 소스코드에 고정적으로 명시되어 있는 권한들
- 글로벌 권한
  - 데이터베이스나 테이블 이외의 객체(파일, 서버관리 등)에 적용되는 권한
- 객체 권한 
  - 데이터베이스나 테이블을 제어하는 데 필요한 권한

**동적 권한**
- MySQL 서버가 시작되면서 동적으로 생성되는 권한(컴포넌트나 플러그인 설치로 인해 등록되는 권한 등) - 일부는 정적 권한과 같이 명시되어 있음
- MySQL 5.7 버전의 SUPER 권한을 잘개 쪼갠 권한들
- 백업 관리, 서버 관리, 리두 로그 관리, 리소스 관리 등

### GRANT

사용자에게 권한을 부여할 때 `GRANT` 명령 사용

**Format**

```mysql
GRANT privilege_list # 권한 (여러 개 명시 가능)
    ON db.table # 권한의 특성(범위)에 따라 명시되는 객체(DB, 테이블) 
    TO 'user'@'host' # 권한 부여 대상
    ; 
```

**글로벌 권한 부여**

글로벌 권한을 부여할 때는 특정 DB나 테이블에 부여될 수 없으므로 ON 절에는 항상 *.*을 사용함

`*.*`: 모든 DB의 모든 객체를 포함한 MySQL 서버 전체를 의미

```mysql
# 'user'@'localhost' 계정에 모든 DB에 대한 SUPER 권한 부여
GRANT SUPER ON *.* TO 'user'@'localhost';
```

**DB 권한 부여**

DB 권한은 ON 절에 모든 DB 또는 특정 DB를 명시할 수 있음

단, 특정 DB의 특정 테이블까지 명시할 수는 없음

```mysql
# 'user'@'localhost' 계정에 모든 DB에 대한 EVENT 권한 부여
GRANT EVENT ON *.* TO 'user'@'localhost';

# 'user'@'localhost' 계정에 test DB에 한 EVENT 권한 부여
GRANT EVENT ON test.* TO 'user'@'localhost';
```

**테이블 권한**

테이블 권한은 모든 DB, 특정 DB, 특정 DB의 특정 테이블까지 명시 가능

특정 컬럼을 지정해서 권한을 부여할 수 있는데, 해당 권한 뒤에 컬럼을 명시해야 됨

다만 테이블/컬럼 단위의 권한을 사용하게 되면 세세한 권한 체크를 하기 때문에 전체적인 성능에 영향을 미칠 가능성이 있음

따라서 테이블에서 권한을 허용하고 컬럼에 대한 별도의 뷰를 만들어 사용하는 대체 방법이 있음

```mysql
GRANT SELECT, INSERT, UPDATE, DELETE ON *.* TO 'user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON test.* TO 'user'@'localhost';

# 'user'@'localhost' 계정에 test.account 테이블에 대한 SELECT, INSERT, UPDATE, DELETE 권한 부여
GRANT SELECT, INSERT, UPDATE, DELETE ON test.account TO 'user'@'localhost';

# 'user'@'localhost' 계정에 test.account 테이블에 대한 SELECT, INSERT, UPDATE 권한 부여 및 nickname 대한 DELETE 권한 부여
GRANT SELECT, INSERT, UPDATE, DELETE(nickname) ON test.account TO 'user'@'localhost';
```

### Privilege Check 

**각 권한에 대한 관련 테이블**

mysql.user: 계정 정보, 계정/역할에 부여된 글로벌 권한

mysql.db: 계정/역할에 DB 단위로 부여된 권한

mysql.tables_priv: 계정/역할에 테이블 단위로 부여된 권한

mysql.columns_priv: 계정/역할에 컬럼 단위로 부여된 권한

mysql.procs_priv: 계정/역할에 스토어드 프로그램 단위로 부여된 권한

mysql.global_grants: 계정/역할에 부여된 동적 글로벌 권한

## Role

권한을 묶어 놓은 것으로 역할의 이름으로 용도를 나타낼 수 있고 재사용될 수 있음

**기본 역할, 역할 그래프 조회**

mysql.default_roles: 계정 별 기본 역할

mysql.role_edges: 역할에 부여된 역할 관계 그래프

**역할 생성**

```mysql
CREATE ROLE role_test_read, role_test_write; # 생성할 역할 이름(여러 개 가능)
```

**역할에 권한 부여**

```mysql
GRANT SELECT ON test.* TO role_test_read;

GRANT INSERT, UPDATE, DELETE ON test.* TO role_test_write;
```

**계정에 역할 부여**

```mysql
GRANT role_test_read TO test_reader@'127.0.0.1';

GRANT role_test_write TO test_writer@'127.0.0.1';
```

**현재 로그인한 계정의 역할 확인 및 역할 자동 부여 옵션 설정**

MySQL은 역할을 자동적으로 부여해주지 않음

따라서 수동으로 부여를 하거나 로그인 시 자동으로 역할을 부여하는 옵션을 설정하면 됨

```text
mysql> SELECT current_role();
+----------------+
| current_role() |
+----------------+
| NONE           |
+----------------+
1 row in set (0.00 sec)
```

```mysql
# 수동 역할 부여
SET ROLE 'role_test_read';

# 자동 역할 부여
SET GLOBAL activate_all_roles_on_login=ON;
```

```text
mysql> SELECT current_role();
+----------------------+
| current_role()       |
+----------------------+
| `role_test_read`@`%` |
+----------------------+
1 row in set (0.00 sec)
```

**재밌는 사실**

MySQL은 ROLE과 USER는 동일한 것으로 취급함

보안을 강화하는 측면에서 둘을 구분하여 사용할 수 있음

계정 정보가 담겨 있는 `mysql.user` 테이블을 조회해보면 역할도 같이 출력됨

역할도 host를 가지고 있는데, 역할로써 사용한다면 호스트 부분은 어떻든 상관이 없음

```text
mysql> select user, host, account_locked from mysql.user;
+------------------+-----------+----------------+
| user             | host      | account_locked |
+------------------+-----------+----------------+
| role_test_write  | %         | Y              |
| role_test_read   | %         | Y              |
| reader           | 127.0.0.1 | N              |
| writer           | 127.0.0.1 | N              |
| mysql.infoschema | localhost | Y              |
| mysql.session    | localhost | Y              |
| mysql.sys        | localhost | Y              |
| root             | localhost | N              |
+------------------+-----------+----------------+
8 rows in set (0.00 sec)
```