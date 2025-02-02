[nosql databases](#nosql-databases)

[특징](#특징)

[키-값 nosql vs rdbms](#키-값-nosql-vs-rdbms)

[데이터 저장 방식에 따른 nosql db 구분](#데이터-저장-방식에-따른-nosql-db-구분)

[nosql 트랜잭션과 일관성 제약](#nosql-트랜잭션과-일관성-제약)

## nosql databases

애플리케이션은 소셜 미디어, 스트리밍, 데이터 분석 등 서로 다른 포맷을 가진 대용량 데이터를 처리한다

기존 데이블, 행, 열로 데이터를 관리하는 관계형 데이터베이스를 강제한다면 데이터 중복, 성능 저하 등의 문제를 불러일으킬 수 있다

nosql 데이터베이스는 비관계형 데이터 모델을 위해 구축되었으며 다양한 데이터 포맷을 지원할 수 있도록 유연한 스키마를 갖추고 있다

## 특징

#### 비관계형 데이터 모델

nosql은 관계형 데이터베이스처럼 테이블 간의 관계를 강제로 정의하지 않는다

주로 키-값, 도큐먼트, 그래프, 컬럼-패밀리 모델로 저장된다

-> 다양한 형식의 데이터를 지원하여 관계를 단순화하고 유연한 데이터를 관리할 수 있도록 한다

#### 유연한 데이터 모델

nosql의 스키마는 데이터 구조가 고정되지 않고 유동적으로 변경될 수 있다

json, bson, xml 등 다양한 형식의 데이터를 저장할 수 있으며, 데이터의 각 항목에 대해 다른 필드 구성을 가질 수 있다

#### 수평 확장성

관계형 데이터베이스에서는 수직적 확장에 의존하는 경우가 많은 반면,

nosql 데이터베이스는 분산된 하드웨어 클러스터를 사용하여 수평적으로 확장하도록 설계되었다

#### 고성능

대규모 데이터 처리에 뛰어난 성능을 발휘한다

대량의 데이터를 빠르게 삽입하고 조회할 수 있는 방법을 제공하여 읽기/쓰기 성능이 중요한 실시간 애플리케이션이나 빅데이터 환경에서 자주 사용된다

## 키-값 nosql vs rdbms

[참고](https://djlee118.tistory.com/95)

#### 패러다임 차이

rdbms: 데이터 구조화(데이터 간 관계를 정의), 데이터 중복 지양, 데이터의 일관성 추구

nosql: 단일 key를 사용하여 데이터 처리, 대량의 데이터 저장 조회, 단순성 추구

#### 설계 차이

rdbms: 도메인/요구사항 분석 -> 엔티티 간 관계 정의 -> 스키마 설계 -> 쿼리

nosql: 도메인/요구사항 분석 -> 조회할 데이터 분석 -> 데이터 저장/조회

#### 데이터 취급 차이

rdbms: 정규화, 엔티티 간의 관계에 따라 각 테이블이 데이터에 대한 부분 정보를 가지고 있고 쿼리를 통해 전체 정보 조회

```sql
SELECT * 
FROM post 
INNER JOIN user ON post.user_id = user.id 
WHERE user.id = "hansanhha" 
```

```text
 table                 table
[user]   ---1:N--->   [post]
  id                    id
                      user_id
                       title
                      content
```

nosql: 값이 아닌 키에 부분 정보가 포함되며, 키를 통해 데이터 조회

```redis
GET users:hansanhha:posts:1
```

```text
       [key]     ----------------->   [value]
users:userid:posts:num                 title
                                      content
```

## 데이터 저장 방식에 따른 nosql db 구분

키-값
- 데이터를 키와 값 쌍으로 저장한다
- redis, aws dynamo db

문서
- 데이터를 json 또는 bson 형식의 문서로 저장한다
- mongdb, couchdb

컬럼 패밀리
- 데이터를 컬럼 단위로 저장한다
- apache cassandra, hbase

그래프
- 데이터를 노드와 엣지의 그래프 형식으로 저장한다
- neo4j, arangodb


## nosql 트랜잭션과 일관성 제약

속성 차이
- 관계형 데이터베이스: acid
- nosql 데이터베이스: cap, base

nosql 데이터베이스에서의 트랜잭션은 관계형 데이터베이스의 트랜잭션과 비교했을 때 여러 노드를 아우르는 트랜잭션을 지원하는 시스템이 적거나 제약이 많다

nosql 설계 목적 자체가 성능과 수평 확장성을 염두에 뒀기 때문에 acid를 완벽히 보장하는 대신 cap 정리와 base 모델을 따른다

### acid vs base

#### 관계형 데이터베이스 acid

관계형 데이터베이스에서 사용하는 전통적인 트랜잭션 속성이다

atomicity (원자성): 트랜잭션 내의 모든 작업이 성공적으로 완료되거나, 하나라도 실패하면 모든 작업이 롤백된다

consistency (일관성): 트랜잭션이 시작되기 전과 완료된 후 데이터가 일관성 있는 상태여야 한다

isolation (고립성): 트랜잭션은 다른 트랜잭션과 독립적으로 실행되어야 한다

durability (지속성): 트랜잭션이 완료된 후 그 결과는 무슨 일이 있더라도 영구적으로 저장되어야 한다 

#### nosql base

basically available (기본적인 가용성): 시스템은 항상 응답을 제공해야 한다 (가끔 데이터의 정확성이 보장되지 않을 수 있다)

soft state (유연한 상태): 시스템의 상태가 시간이 지나면서 변할 수 있으며 일관성 있는 상태를 유지하지 않는 상태에서 동작할 수 있다

eventually consistent (최종 일관성): 모든 데이터 노드가 최종적으로 일관된 상태를 유지하게 된다 -> 일시적으로 일관성 문제가 발생할 수 있지만 시간이 지나면서 모든 노드가 동기화되어 일관성을 맞춰야 한다

### cap (consistency, availability, partition tolerance)

nosql 데이터베이스의 일관성 처리 방식은 cap 정리와 관련이 있다

cap은 분산 시스템에서 일관성(consistency), 가용성(availability), 분할 내구성(partition tolerance)을 동시에 만족할 수 없다는 이론이다

**consistency**: 모든 노드가 동일한 데이터를 동시에 제공한다는 의미다 -> 모든 읽기 연산이 최신 데이터를 반환해야 한다

**availability**: 데이터베이스는 항상 읽기/쓰기 요청에 응답해야 한다 -> 장애가 발생해도 서비스는 계속 제공되어야 한다

**partition tolerance**: 시스템이 네트워크 분할을 겪더라도 서비스를 계속 제공해야 한다 (네트워크 파티션이 발생하면 시스템의 일부 노드는 서로 연결되지 않지만 여전히 데이터를 처리할 수 있어야 한다)

cap 이론에 따르면 분산 데이터에비스는 세 가지 속성 중 두 가지를 우선시해야 하며 나머지 하나는 포기해야 한다
- ca: 데이터의 일관성과 가용성을 보장하지만 네트워크 분할이 발생하면 서비스가 중단될 수 있다 (hbase)
- cp: 네트워크 분할 상황에서도 일관성을 보장하지만 가용성은 제한될 수 있다 (zookeeper)
- ap: 네트워크 분할 상황에서도 가용성을 보장하지만 일관성이 약해질 수 있다 (cassandra, couchbase)

### 일관성 수준

nosql에서 일관성은 즉시 일관성과 최종 일관성으로 구분되며 애플리케이션에서 요구하는 수준에 따라 유연하게 설정할 수 있다

#### 즉시 일관성 (strong consistency)

모든 노드가 동시에 동일한 데이터를 반환한다

데이터를 읽는 순간 항상 최신 상태의 데이터가 반환된다

#### 최종 일관성 (eventual consistency)

데이터의 일관성이 즉시 보장되지 않고, 시간이 지나면서 모든 노드가 동일한 데이터를 보장하게 된다





