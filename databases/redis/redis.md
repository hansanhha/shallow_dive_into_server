[redis](#redis-remote-dictionary-server)

[레디스 설치](#레디스-설치)

[아키텍처](#아키텍처)

[메커니즘](#메커니즘)

[데이터 타입](#데이터-타입)

[주요 명령어](#주요-명령어)

고급 기능
- [pub/sub](#pubsub)
- [redlock](#redlock)
- [redis streams](#redis-streams)
- [pipeline](#pipeline)
- [redis cluster](#redis-cluster)

[성능 최적화](#성능-최적화)

[보안](#보안)

## redis (remote dictionary server)

데이터를 비정형 형식으로 저장하는 [nosql 데이터베이스](../nosql/nosql.md)의 일종으로 메모리 기반의 키-값 데이터 저장소다

### 주요 특징

#### in-memory data store

데이터를 메모리에 저장하여 읽기/쓰기 속도가 매우 빠르다

다만 대규모 데이터 저장 시 비용이 증가한다

데이터의 영구 저장도 필요한 경우에 지원한다 (rdb/aof) 

#### key-value store

데이터를 키와 값 형태로 저장한다

값에 넣을 수 있는 데이터 구조
- Strings, Lists, Sets, Sorted Sets, Hashes, Streams, HyperLogLogs, Bitmaps

#### 싱글 스레드 기반

I/O와 명령어 실행의 병목을 최소화하기 위해 싱글 스레드로 동작하기에 분산/멀티 스레드 환경에서의 동시성 제어 문제를 갖지 않는다 

#### 사용 사례

캐싱, 분산 락, 세션 관리, 실시간 데이터 처리, pub/sub 메시징, 레이트 리미팅 등

## 레디스 설치

## 아키텍처

### 메모리 기반 데이터 저장

모든 데이터를 key-value 형태로 메모리에 저장하고 필요에 따라 디스크에서 백업한다

메모리에서 데이터를 처리하므로 매우 빠르긴 하지만 메모리가 부족하면 성능 저하가 발생한다

### 싱글 스레드 아키텍처

싱글 스레드로 명령어를 fifo(first-in-first-out) 방식으로 처리한다

클라이언트의 요청(레디스 명령)을 싱글스레드로 처리하고 내부적으로 i/o 멀티플렉싱(비동기 이벤트 처리)을 사용하여 고성능을 유지한다

레디스의 성능은 cpu 작업보다는 메모리와 네트워크 속도에 의존한다

네트워크 i/o 작업이 레디스의 성능 병목 현상 중 하나인데 이를 해결하기 위해 네트워크 파싱을 멀티스레드로 처리할 수 있는 threaded io 기능을 도입했다

### 데이터 지속성

레디스는 메모리 기반이지만 영구성을 제공하여 데이터를 디스크에 저장할 수 있다

rdb (redis database snapshot)
- 특정 시점의 데이터를 스냅샷으로 디스크에 저장하는 방식
- 최근 데이터를 손실할 수 있다

aof (append-only file)
- 모든 쓰기 작업을 로그에 기록한다
- 데이터 손실 가능성이 적지만 파일 크기가 증가한다

혼합모드
- rdb와 aof를 조합하여 성능과 데이터 안전성을 모두 만족시킨다

### 네트워크 모델

레디스는 기본적으로 요청-응답(클라이언트-서버) 모델을 따른다

클라이언트는 TCP를 통해 레디스 서버와 통신한다

### 주요 구성 요소

#### 레디스 서버

레디스의 핵심 서버 프로세스

클라이언트의 요청을 처리하고 데이터를 관리한다

#### 레디스 클라이언트

다양한 언어에서 레디스와 통신할 수 있는 클라이언트를 지원한다

java: jedis, lettuce, redisson

#### 레디스 데이터

레디스의 메모리에 저장된 데이터

키-값 데이터외에도 다양한 데이터 구조를 지원한다


## 메커니즘

### 데이터 그룹화, 키와 값

레디스는 관계형 데이터베이스처럼 테이블을 사용하지 않는다

대신 **모든 데이터는 키(key)를 통해 그룹화**되며 **데이터의 구조나 타입은 키에 저장되는 값(value)에 의해 결정**된다

키는 데이터를 식별하는 고유한 문자열이고, 값의 데이터 타입은 미리 정의되지 않는다 (schemaless)

레디스는 **하나의 키에 하나의 데이터 타입만 저장**할 수 있다

따라서 각 키에 저장된 데이터 타입에 맞는 명령어로만 작업을 수행할 수 있다

```text
key: "user:1001" (키를 통해 데이터 그룹화)
value: {name: "hansanhha", job: "developer"} (값은 hash 타입으로 저장)
```

#### 키 네이밍 규칙

레디스에서 키의 이름은 일반적으로 **콜론(:)**을 사용해서 지정한다

`user:1001` `user:1001:techstack` `order:20250214:status`

### 데이터 삽입

데이터를 삽입하려면 명령어를 통해 키와 값을 지정해야 한다 (값은 특정 데이터 타입으로 저장)

#### 기본적인 데이터 삽입

```redis
SET user:1001 "hansanhha"         --  단순 문자열 저장
HSET user:1001 job "developer"    -- 해시에 필드와 값 저장
LPUSH user:1001:techstack "redis" -- 리스트에 값 추가
```

### 데이터 접근

#### 데이터 조회

RDBMS에서 SQL 쿼리를 통해 데이터에 접근하는 반면, 레디스는 키를 통해 데이터에 직접 접근한다

해당 키에 저장된 데이터 타입에 따라 적절한 명령어를 사용해야 한다 

```redis
GET user:1001                    -- 문자열 값을 가져온다 (hansanhha)
HGET user:1001 job               -- hash에서 특정 필드 값을 가져온다 (developer)
LRANGE user:1001:techstack 0 -1  -- list의 특정 범위를 가져온다 (redis)
```

#### 와일드카드 조회

키를 검색할 때 와일드카드 문자를 지원한다

```redis
KEYS "user:*"  -- "user:1001", "user:1002" ....
```

### 데이터 삭제

키를 사용하여 특정 키 또는 특정 데이터 타입 내의 일부 데이터를 삭제할 수 있다

#### 키 삭제

여러 키를 동시에 삭제할 수도 있다

```redis
DEL user:1001            -- user:1001 키 및 관련된 모든 데이터 삭제
DEL user:1001 user:1002
```

#### 데이터 타입 내부 삭제

```redis
HDEL user:1001 name                 -- 해시에서 name 필드 삭제
LREM user:1001:techstack 1 "redis"  -- 리스트에서 "redis" 1개 삭제
```

### 데이터 ttl (time-to-live)

레디스는 데이터에 유효 기간을 설정할 수 있는 기능을 제공한다

지정된 시간이 지나면 자동으로 데이터가 삭제된다

```redis
EXPIRE session:1234 300          -- 특정 키에 ttl 설정
SETEX session:1234 300 "active"  -- 값을 설정하면서 ttl 설정
```

#### ttl 확인

```redis 
TTL session:1234      -- 남은 유효 시간 확인 
PERSIST session:1234  -- ttl을 제거하여 데이터를 영구적으로 저장
```


## 데이터 타입

#### string

레디스에서 가장 기본적인 타입으로 모든 데이터는 내부적으로 문자열로 저장된다

단순 텍스트, json, 바이너리 데이터 등 다양한 형태로 저장할 수 있다

특징: 최대 저장 크기: 512mb

사용: 캐싱, 간단한 카운터(조회수)

#### hash

필드와 값을 저장할 수 있는 키-값 쌍의 집합이다

특징: 키는 하나, 그 아래 여러 필드-값의 쌍을 관리한다

사용: 객체 정보 저장(사용자 정보, 상품 상세 정보)

#### list

순서가 있는 문자열 리스트로 요소를 앞이나 뒤에서 삽입/삭제할 수 있다

특징: 순서 유지, 중복 허용, 스택과 큐로 활용 가능

사용: 작업 대기열(메시지 처리), 타임라인 데이터 저장

#### set

중복되지 않는 유일한 값들의 집합

특징: 집합 연산 최적화, 순서 없음, 중복된 값은 자동으로 제거

사용: 태그 관리(게시글 태그), 유니크한 사용자 id 저장

#### sorted set

점수를 기반으로 자동 정렬되는 집합으로 값과 점수를 함께 저장한다

특징: 값 중복 X, 점수를 기준으로 오름차순 정렬

사용: 순위표 관리(게임 랭킹), 시간 기반 데이터 정렬(최신 뉴스)

#### bitmaps

문자열을 비트 단위로 조작하여 효율적으로 데이터를 저장할 수 있다

특징: 효율적인 메모리 사용, 특정 비트를 설정하거나 조회 가능

사용: 사용자 활성 상태 추적, 대규모 플래그 관리

#### hyperloglog

대규모 데이터에서 고유값의 개수를 추정하는데 사용되는 확률적 데이터 구조

특징: 정확한 개수가 아닌 추정값 제공, 고정적 메모리 사용량(12kb)

사용: 대규묘 사용자의 고유 방문자 수 추적

#### streams

고성능 로그 데이터 구조, 스트리밍 데이터 처리 가능 

특징: 시간 순 정렬, 소비자 그룹을 지원하여 분산 처리 가능

사용: 실시간 이벤트 로그, 채팅 메시지 저장


## 주요 명령어

### 공통 명령어

`SET key value`: 키에 값을 저장

`SETNX key value` 또는 `SET key value NX`: 키가 없는 경우에만 값 저장(주로 락 처리에 사용)

`SET key value PX <millisecond>`: 데이터 저장 및 지정한 시간 이후 삭제 

`GET key`: 키의 값을 조회

`GETSET key value`: 키의 값을 조회하고 새로운 값으로 설정

`DEL key`: 키와 값을 삭제

`EXISTS key`: 키가 존재하는 지 확인 (존재: 1, 존재X: 0)

`EXPIRE key seconds`: 키의 ttl(time-to-live) 설정

`TTL key`: 키의 남은 ttl 확인

`RENAME key new_key`: 키 이름 변경

`TYPE key`: 키 데이터 타입 확인

### 데이터 타입 별 주요 명령어

#### string

증가/감소
- `INCR key`: 값 1 증가(정수형 값)
- `DECR key`: 값 1 감소
- `INCRBY key amount`: amount만큼 값 증가
- `DECRBY key amount`: amount만큼 값 감소

```redis
SET counter 10       -- counter에 10 저장
INCR counter         -- counter를 1 증가 → 11
DECRBY counter 5     -- counter를 5 감소 → 6
```

#### hash

필드 추가/수정
- `HSET key field value`: 필드와 값 저장
- `HMSET key field1 value1 field2 value2 ...` : 여러 필드와 값 저장

필드 조회
- `HGET key field`: 특정 필드의 값 조회
- `HMGET key field1 field2 ...`: 여러 필드 조회
- `HGETALL key`: 모든 필드와 값 조회

필드 삭제
- `HDEL key field`: 특정 필드 삭제

```redis
HSET user:1001 name "hansanhha"  -- name 필드에 "hansanhha" 값 저장
HSET user:1001 job "developer"   -- job 필드에 "developer" 값 저장
HGET user:1001 name              -- name 필드 조회 "hansanhha"
HGETALL user:1001                -- 모든 필드와 값 조회 {name: "hansanhha", job: "developer"}
```

#### list

값 추가
- `LPUSH key value`: 리스트 앞(왼쪽)에 값 추가
- `RPUSH key value`: 리스트 뒤(오른쪽)에 값 추가

값 조회
- `LRANGE key start stop`: 지정한 범위의 값 조회
- `LINDEX key index`: 특정 인덱스의 값 조회

값 삭제
- `LPOP key`: 리스트 앞(왼쪽) 값 제거 및 반환
- `RPOP key`: 리스트 뒤(오른쪽) 값 제거 및 반환

```redis
LPUSH user:1001:techstack "java"   -- "java" 값 추가 ["java"]
RPUSH user:1001:techstack "redis"  -- "redis" 값 추가 ["java", "redis"]
LRANGE user:1001:techstack 0 -1    -- 모든 값 조회 ["java", "redis"]
LPOP user:1001:techstack           -- 왼쪽(앞) 값 제거 및 반환: "java", 남은 값: ["redis"]
```

#### set

값 추가/삭제
- `SADD key value`: 값 추가
- `SREM key value`: 값 삭제

값 조회
- `SMEMEBERS key`: 모든 값 조회
- `SISMEMEBER key value`: 특정 값의 존재 여부 확인

집합 연산
- `SUNION key1 key2`: 두 집합의 합집합
- `SINTER key1 key2`: 두 집합의 교집합
- `SDIFF key1 key2`: 첫 번째 집합에서 두 번째 집합을 뺀 차집합

```redis
SADD user:1001:techstack "java" "redis" -- 값 추가  {"java", "redis"}
SISMEMBER user:1001:techstack "java"    -- "java 존재 여부 확인" -> 1 (존재)
SREM user:1001:techstack "java"         -- "java" 값 삭제
```

#### sorted set

값 추가/삭제
- `ZADD key score value`: 점수와 값을 함께 저장
- `ZREM key value`: 값 삭제

값 조회
- `ZRANGE key start stop [WITHSCORES]`: 지정한 범위의 값을 점수 기준으로 정렬하여 조회
- `ZRANK key value`: 값의 순위 조회

```redis
ZADD leaderboard 100 "hansanhha"    # hansanhha를 점수 100으로 추가
ZADD leaderboard 200 "userX"        # userX를 점수 200으로 추가
ZRANGE leaderboard 0 -1             # 모든 값 조회 ["hansanhha", "userX"]
ZRANGE leaderboard 0 -1 WITHSCORES  # 점수 포함 모든 값 조회 ["hansanhha", 100, "userX", 200]
```

### 트랜잭션 명령어

`MULTI`: 트랜잭션 시작

`EXEC`: 트랜잭션 실행

`DISCARD`: 트랜잭션 취소

`WATCH key [key...]`: 하나 이상의 키를 감시하여, 트랜잭션 실행 전에 키가 변경되면 트랜잭션이 취소된다

`UNWATCH`: 감시 중인 키 해제

```redis
MULTI                      -- 트랜잭션 시작
SET user:1001 "hansanhha"  -- 명령어 추가
SET user:1002 "userX"      -- 명령어 추가
EXEC                       -- 트랜잭션 실행
```

```redis
WATCH balance     -- balance 키 감시
MULTI             -- 트랜잭션 시작
DECR balance 100  -- balance에서 100 차감
EXEC              -- 트랜잭션 실행
```

EXEC 명령어를 수행하기 전에 다른 클라이언트에서 balance를 수정하면 트랜잭션이 취소된다

### lua 스크립트 명령어

lua 스크립트에 명령어를 작성하여 클라이언트와 서버 간의 네트워크 i/o를 줄이면서 복잡한 로직을 처리할 수 있다

스크립트를 통한 명령어 수행은 완전한 원자성을 보장한다

`EVAL script numkeys key [key ...] arg [arg ...]` : lua 스크립트 실행
- script: 실행할 lua 코드
- numkeys: 키의 개수
- key: 스크립트에서 사용할 redis 키
- arg: 스크립트에서 사용할 인수

`EVALSHA sha1 numkeys key [key ...] arg [arg ...]`: 캐시된 스크립트 실행

`SCRIPT LOAD script`: 스크립트를 서버에 로드하고 SHA1 해시 값 반환

`SCRIPT FLUSH`: 서버에서 모든 스크립트 삭제

`SCRIPT EXISTS sha1 [sha1 ...]`: 특정 스크립트가 서버에 존재하는지 확인

```redis
EVAL "return redis.call('SET', KEYS[1], ARGV[1])" 1 user:1001, "hansanhha"
-- KEYS[1]: user:1001 키로 전달된 레디스 키
-- ARGV[1]: "hansanhha"로 전달된 인수
```

```redis
SCRIPT LOAD "return redis.call('GET', KEYS[1])"
-- 스크립트를 로드하고 SHA1 해시를 반환한다
-- 이후 EVALSHA를 통해 스크립트를 실행할 수 있다

EVALSHA <sha1> 1 user:1001
-- user:1001 키의 값 조회
```



### 관리 명령어

`FLUSHDB`: 현재 레디스 데이터베이스의 모든 데이터 삭제

`FLUSHALL`: 모든 데이터베이스의 모든 데이터 삭제

`INFO`: 레디스 서버 정보 및 통계 확인

`PING`: 레디스 서버가 동작 중인지 확인

```redis
PING     -- "PONG" 반환
```


## pub/sub

### pub/sub 개념

publish/subscribe 기능은 발행자(publisher)가 메시지를 발행하면 해당 채널(channel)에 구독(subscribe)한 구독자(subscriber)들이 메시지를 실시간으로 받을 수 있는 메시징 시스템이다

#### 채널

메시지가 전달되는 경로

발행자는 특정 채널로 메시지를 보낸다

구독자는 하나 이상의 채널을 구독하여 메시지를 받을 수 있다

#### 발행자

채널에 메시지를 발행하는 역할을 한다

발행자와 구독자는 직접적으로 연결되지 않는다

#### 구독자

특정 채널을 구독하여 해당 채널로 전달된 메시지를 실시간으로 수신한다

#### 브로드캐스트 모델

메시지는 구독자에게만 전달되며 따로 저장되지 않는다

레디스의 pub/sub 기능은 브로드캐스트 모델로 작동한다

### pub/sub 명령어

#### 발행

`PUBLISH channel message`: 특정 채널에 메시지를 발행한다

```redis
PUBLISH tooniverse "짱구는 못말려 시작"
```

#### 구독

`SUBSCRIBE channel [channel ...]`: 하나 이상의 채널을 구독한다

구독 후, 해당 채널에서 메시지가 발행될 때마다 실시간으로 메시지를 수신한다

```redis
SUBSCRIBE tooniverse
```

#### 패턴 구독

`PSUBSCRIBE pattern [pattern ...]`: 정규식을 이용하여 하나 이상의 채널을 구독한다

```redis
PSUBSCRIBE news.*
-- news.sports, news.whether 등 news.로 시작하는 모든 채널을 구독한다
```

#### 구독 해제

`UNSUBSRCRIBE [channel ...]`: 특정 채널의 구독 해제한다

`PUNSUBSCRIBE [pattern ...]`: 특정 패턴 구독을 해제한다

```redis
UNSUBSCRIBE tooniverse
```

### pub/sub 동작 과정

레디스 서버는 해시 테이블을 사용해서 각 채널과 관련된 구독자 목록을 그룹화하여 관리한다

발행자가 메시지를 보낼 때 해당 채널에 등록된 모든 구독자에게 브로드캐스트 방식으로 메시지를 전달하며 레디스 서버는 메시지를 저장하지 않는다

#### 발행-구독 흐름

1. 구독자 A가 특정 채널을 구독한다 `SUBSCRIBE tooniverse`
2. 구독자 B도 동일한 채널을 구독한다
3. 발행자가 `PUBLISH tooniverse "짱구는 못말려 시작"` 명령어로 메시지를 발행한다
4. 레디스는 이 메시지를 tooniverse 채널을 구독한 모든 클라이언트(A, B)에게 전달한다

#### 패턴 구독 흐름

1. 구독자 A가 news.로 시작하는 모든 채널을 구독한다 `PSUBSCRIBE news.*`
2. 발행자가 `PUBLISH news.sports "손흥민 득점""` 메시지를 발행한다
3. 구독자 A는 해당 메시지를 수신한다

#### 장단점

장점
- 실시간 처리: 메시지가 발행되면 즉시 구독자에게 메시지 전달
- 간단하고 가벼움
- 다양한 클라이언트 지원: 다양한 프로그래밍 언어에서 레디스 클라이언트를 통해 pub/sub 기능 구현 가능

단점
- 메시지를 보존하지 않음: 메시지 저장이 필요한 경우 다른 메시지 브로커(kafka, rabbitmq)을 사용해야 한다
- 구독자 관리: 단순히 메시지를 전달할 뿐 구독자 상태를 추적하지 않는다
- 확장성: 단일 노드에서 동작하는 경우 성능이 뛰어나지만 매우 많은 구독자와 채널이 있는 경우 클러스터 구성이 필요하다

### 레디스 pub/sub 사용 사례

#### 채팅 애플리케이션

각 채팅방을 채널로 설정하여 메시지를 실시간으로 전달

#### 실시간 알림 시스템

구독자에게 실시간으로 이벤트 알림 전송

#### 라이브 데이터 스트림

스포츠 경기 점수 업데이트, 주식 데이터 등

#### iot 메시징

iot 센서에게 데이터 발행 -> 구독자로 처리 시스템 구현 


## redlock

[redlock](./redis%20distributed%20lock.md#redlock)

## redis streams

## pipeline

## redis cluster

### 혼합 모드

### 백업 및 복구

## 성능 최적화

### redis 메모리 관리

메모리 사용량 분석 

ttl 설정과 메모리 정책

### 샤딩

## 보안

### 비밀번호 설정

### 네트워크 보안

### acl (access control list)