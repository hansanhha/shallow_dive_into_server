* [OAuth 2.0](#oauth-2.0)
* [Roles](#roles)
* [Abstract Protocol Flow](#abstract-protocol-flow)
* [Authorization Grant](#authorization-grant)
    * [Basic Authorizaiton Grant](#basic-authorization-grant)
    * [Addtional Authorizaiton Grant](#additional-authorization-grant)
* [Tokens](#tokens)
* [Client Types](#client-types)
* [Client Profiles](#client-profiles)
    * [Client Password](#client-password)
* [Protocol Endpoints](#protocol-endpoints)
    * [Authorization Server Endpoints](#authorization-server-endpoints)
    * [Client Endpoint](#client-endpoint) 

[참고](#참고)

## OAuth 2.0

애플리케이션 사용자에 대한 인증, 인가를 제 3자에게 위임할 수 있도록 하는 사양

산업 표준 인증(authorization) 프로토콜

웹 애플리케이션, 데스크탑 애플리케이션, 모바일 및 휴대용 장치에 대해 특정 인증 흐름을 제공함

이를 통해 client 개발자가 간단하게 인증 기능을 구현하도록 도와줌

애플리케이션이 third-party 애플리케이션에 resource 제공하기 위해 resource owner의 credentials를 공유하지 않는 대신 별도의 crendentials을 발급하여 접근할 수 있도록 하는 메커니즘을 가짐

## Roles

### resource owner

protected source에 접근할 수 있는 entity

resource owner가 사람일 경우 end-user라고 칭함

### resource server

protected source를 호스팅하는 server

access token을 사용하여 resource 접근 요청을 받고 응답함

### client

resource owner를 대신하여 권한을 가지고 protected source를 요청하는 애플리케이션

여기서 말하는 client는 특정 수단이 아닌 서버, 데스크탑, 모바일 등 protected source를 요청하는 애플리케이션 자체를 말함

### authorization server

resource owner가 인증을 마치면 client에게 access token을 발급하는 server

resource server와 authorization server는 동일한 서버이거나 별도로 관리할 수 있음

단일 authorization server가 여러 resource server에서 받는 access token을 발급할 수 있음

## Abstract Protocol Flow

![protocol flow](imgs/protocol-flow.png)

* A. [client -> resource owner] authorization request 전송
    * client가 resource owner에게 authroziation request 요청을 보냄
    * authorization server를 통하거나 resource owner에게 직접 요청을 보낸다
    * resource owner - authentication

* B. [authorization server -> client] authorization grant 수신
    * resource owner가 인증을 마치면 client는 authroziation server로부터 authorization grant를 받음 - resource owner의 authorization을 나타내는 credentials(자격 증명)
    * RFC 6749 명세에 따르면 authrozation grant의 유형(type)은 4가지이고, 추가적으로 확장 타입을 지원함
    * client가 authroziation request를 보내는 방법과 authorization server에서 지원하는 유형에 따라 grant 타입이 결정됨(보안 수준)

* C. [client -> authorization server] access token 요청
    * client의 authorization grant를 통해 client가 authorization server에 access token을 요청함

* D. [authorization server -> client] access token 발급
    * authroization server는 client를 인증하고 authrozation grant를 검증함
    * 유효하다면 acccess token 발급

* E. [client -> resource server] protected source 요청
    * access token을 통해 client에서 resource server에 protected source를 요청함

* F. [resource server -> client] protected source 응답
    * resource server는 access token을 검증하고, 유효하다면 요청에 응답함

## Authroization Grant

client가 authorization server에게 access token을 얻기 위해 사용되는 수단

resource owner가 authorization server에 인증을 마치면 authroziation server가 client에게 응답해주는 것으로 resource owner의 권한을 나타내는 자격증명(credentials)임

## Basic Authorization Grant

rfc 6749에서 정의한 기본 grant type은 4가지임

보안 수준 및 client 애플리케이션 유형 등에 따라 선택하면 됨

### Authorization Code

![authorization code flow](imgs/authorization-code-flow.png)

* A
    * resource owner의 user agent를 authroization endpoint로 전달(direct)
    * client identifier, requested scope, local state, redirection uri 포함
* B
    * resource owner 인증
    * resource owner의 client access request 허용/거부 설정
* C
    * resource owner의 user agent를 client의 redirection uri로 redirect
    * authorization code 및 local state 포함
* D
    * authroization server의 token endpoint로 access token 요청
    * access token을 발급하기 전에 client 자체도 인증함
    * redirection uri, authorization code 포함
* E
    * client가 전달한 authorization code를 검증하고 redirection uri가 step C에서 사용된 redirection uri과 일치하는지 확인
    * 유효하다면 access token 발급, 추가적으로 refresh token도 발급할 수 있음(선택사항)

#### authorization request field

* request_type (required)
    * value "code" 지정
* client_id (required)
    * client identifier
* redirect_uri (optional)
    * client 등록할 때 설정한 걸 사용할 수도 있음
* scope (optional)
    * access request scope
* state (recommended)
    * authorization request와 callback 사이에서 상태를 유지하기 위한 용도
    * authorization server가 redirect할 때 request에 담은 state를 포함해서 응답함
    * CSRF(cross-site request forgery) 방지 목적

**요청 예시**

```
    GET /authorize?response_type=code&client_id=s6BhdRkqt3&state=xyz
        &redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb HTTP/1.1
    Host: server.example.com
```

#### authorizatoin response field

* code (required)
    * authorization code
* state (required)
    * client에서 state를 보낸 경우

#### error

[error](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1) 참고

#### Access token request 

* grant type (required)
* code (required)
* redirect uri (required)
* client_id (required)

**요청 예시**

```
     POST /token HTTP/1.1
     Host: server.example.com
     Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
     Content-Type: application/x-www-form-urlencoded

     grant_type=authorization_code&code=SplxlOBeZQQYbYS6WxSbIA
     &redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb
```

#### Acess token response

**응답 예시**

```
     HTTP/1.1 200 OK
     Content-Type: application/json;charset=UTF-8
     Cache-Control: no-store
     Pragma: no-cache

     {
       "access_token":"2YotnFZFEjr1zCsicMWpAA",
       "token_type":"example",
       "expires_in":3600,
       "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
       "example_parameter":"example_value"
     }
```


#### 특징

access token이 백단에서 이뤄짐(client - authorization, resource server)

browser 등에 token 또는 resource owner에 대한 데이터 노출이 없음

또한 resource server에서 client에 대한 인증을 진행함

### Implicit

**flow**

```
1. client -> resource owner        : 인증 요청(+ 접근 허용 동의)
2. authorization server ->  client : access token 발급(client redirect uri 활용, client 인증 X)
3. 이후 동일
```
**특징**

authorization server에서 client에 대한 인증을 진행하지 않음

중간과정(authroziation code)없이 resource owner의 redirect uri을 통해 access token이 전달되므로, user agent에 접근할 수 있는 다른 애플리케이션에 노출될 수 있음

### Resource Owner Password Credentials (Password grant)

**flow**

```
1. resource owner -> client       : crendentials(username, password) 전달
2. client -> authorization server : access token 요청(resource owner crendentials 전달)
3. authorization server -> client : crendentials 검증 후 access token 발급
4. 이후 동일
```

**특징**

client에게 직접적으로 end-user의 crendentials를 공유함


### Client Credentials

**flow**

```
1. client -> authorization server : client credentials 전달
2. authorization server -> client : access token 발급
3. client -> resource server      : protected resource 접근
```

**특징**

resource owner와 관련없는 데이터에 접근할 때 사용되는 방식

## Additional Authorization Grant

확장 authorization grant 타입

### Proof Key for Code Exchange (PCKE)

**flow**

```
1. client -> authorization server : authroization 요청 (code_challenge, code_challenge_method 전달)
2. authorization server -> client : authorization code 발급 (code_challenge, code_challenge_method 저장)
3. client -> authorization server : access token 요청 (code_verifier 전달)
4. authorization server -> client : access token 발급 (code_challenge, code_challenge_method를 통해 code_verifier 검증)
```

code_challenge : 임의 문자열 A를 sha256으로 해시한 값

code_verifier : 임의 문자열 A

**특징**

HAMC 방식을 사용하여 access token 발급 과정을 거침

공격자는 authorization code를 가로채도 code_verfier가 없으면 access token을 발급할 수 없음

다만 authorization server가 PKCE 방식을 지원해야 하며, authorization code를 사용해야 client를 보호할 수 있음

### Device Code

**flow**

```
1. client -> authroization server : code 요청 (client id 전달)
2. authorization server -> client : device code, user code, verfication url (user code 입력하는 곳) 전달
3. client -> resource owner       : 브라우저를 통해 verfication url 표시
4. resource owner                 : user code 입력 (+ 접근 권한 허용)
5. client -> authorozation server : resource owner가 user code를 통해 인증을 완료했는지 확인
6. authrozation server -> client  : device code, client id를 검증한 후 client에게 access token 전달
```

**특징**

client가 제한된 입력 장치(IoT 장치, 게임 콘솔 등)일 때 사용하는 방식

별도의 장치를 통해 인증을 진행함

## Tokens

### Credentials

resource owner's credentials - username, password

client's credentials - authorization grant(access token 발급 용도), access token(protected resource 접근 용도), refresh token(access token 재발급 용도)

### Access Token

resource server가 호스팅하고 있는 protected resource에 접근하기 위해 사용되는 Credentials

발급 대상인 client에 대한 권한(access duration/scope 등) 정보를 표현함

resource server의 보안 요구사항 또는 회사 규약에 따라 token의 속성, 방법(method) 등을 정의함

### Refresh Token

access token 재발급을 위해 사용되며 authorization server의 재량에 따라 구현되는 Credentials

오직 재발급을 위해 authorization server에 보낼 때만 사용된다

사용 목적
* 현재 access token 만료 또는 invalid일 때
* 새로운 scope를 가진 access token이 필요할 때

**flow**

![refresh-token](imgs/refresh-token.png)

기존과 거의 동일하며 refresh token을 통해 authorization server로부터 새로운 access token을 발급받는 과정이 추가됨

H. Authorization server에서 선택적으로 새로운 refresh token을 발급할 수도 있음

## Client Types

OAuth 2.0은 client를 type과 profile로 세분화해서 나눔

authorization server는 client를 식별하기 위해 client credential을 발급함

client credential을 어떻게 보관하는지에 따라 두 가지로 나눔

### Confidential

발급된 client credentials를 노출시키지 않고 내부에서 안전하게 보관하는 유형임 

### Public

credential을 안전하게 보관할 수 없는 유형임

mobile phone, desktop app, javascript app(browser)의 경우 애플리케이션 안에 임베드함

따라서 크랙되어 비밀번호가 노출될 가능성이 있음

### Client Profiles

각 Profile은 confidential 또는 Public이 될 수 있음

#### Web Application

웹 서버에서 실행되는 application으로 confidential 타입임

resource owner, browser 등에서 credentials에 접근할 수 없음

#### User-agent-based Application

web server로부터 client code를 다운받고, user agent에서 실행되는 application으로 public 타입임

resource owner가 protocol data 및 credential에 접근할 수 있음

#### Native Application

resource owner의 device에 설치되서 실행되는 application으로 public 타입임

user-agent application과 마찬가지로 resource owner가 protocol data 및 client credential에 접근할 수 있음

동적으로 발급되는 credentials(access/refresh token)의 허용 가능한 수준의 보호를 받음

## Client identifier

client id와 client password는 authorization server에 client를 등록할 때 생성됨

client id는 비밀이 아니며 resource owner에게 노출됨

## Client Password

authorization server - client authentication은 HTTP-Basic authentication scheme을 사용함

application/x-www-form-urlencoded(Base64) 사용

authorization server는 HTTP-Basic을 무조건 지원해야 하며 TLS 상에서 통신이 이뤄져야 함

```
Authorization: Basic Y2xpZW50OnNlY3JldA== (client id, password 인코딩)
```

---

request body로 인증을 진행하는 방법을 지원할 수도 있음(기타 방법도 정의 가능)

이 때 parameter 명은 client_id, client_secret으로 설정해야 함

다만 권장되지 않는 방법이며, HTTP Basic을 사용할 수 없는 client의 경우에만 제한적으로 사용하는 것이 좋음

# Protocol Endpoints

## Authorziation Server Endpoints

client - authorization server 간 authorization process를 거칠 때 client가 이용하는 authorization server의 엔드포인트임

### Authorization endpoint

client가 resource owner을 통해 authorization grant를 얻을 때 이용하는 엔드포인트

client가 원하는 authroziation grant type을 "response_type" 필드를 통해 전달한다

authroization code일 경우 : code

implicit일 경우 : token

### Token endpoint

client authorization grant나 refresh token을 통해 access token을 얻을 때 이용하는 엔드포인트

grant type에 따라 엔드포인트가 변경될 수  있다

## Client Endpoint 

**Redirection endpoint**

authorization server가 client에게 authorization credentials 응답할 때 이용하는 엔드포인트

cliden에서 HTML 자체를 응답하는 경우 credential 보호 차원에서 해당 html 문서에 third-party script를 포함하지 않아야 한다

## Client Authentication 

client - authorization server 간 authentication process에서 사용되는 client credential가 탈취될 경우 변경하거나 비활성화할 수 있어야 됨

best practice는 주기적으로 credential을 순환 변경해주는 것임

## 참고

[RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749#section-1.1)

[bytebytego - oauth flow](https://blog.bytebytego.com/i/135955829/oauth-explained-with-simple-terms)

[frontegg - grant types](https://frontegg.com/blog/oauth-grant-types)

[modern oauth guide](https://fusionauth.io/articles/oauth/modern-guide-to-oauth)
