[컨테이너](#컨테이너)
- [컨테이너 확인](#컨테이너-확인)
- [컨테이너 실행](#컨테이너-실행)

[이미지](#이미지)

## 컨테이너

### 컨테이너 확인

#### 실행 중인 모든 컨테이너에 대한 정보 확인
- `docker container ls`

#### 모든 컨테이너에 대한 정보 확인
- `docker container ls -a`

#### 실행 중인 컨테이너 상세 정보 확인
- `docker container inspect <container_name>`

#### 컨테이너의 표준 출력 로그 확인
- `docker container logs <container_name> or <container_id>`

#### 실행 중인 컨테이너 상태 확인 (CPU, 메모리, 네트워크 디스크 사용량)
- `docker container stats <container_id>`

### 컨테이너 실행

#### 도커 실행
- `docker container run <container name>`

#### 인터렉티브 모드 실행
- `docker container run --interactive --tty <container name>`

#### 백그라운드 실행
- `docker container run --detach --publish out-port:in-port <container name>`
- `docker container run --detach --publish 8080:80 hello/world`

--detach: 컨테이너를 백그라운드에서 실행하며 컨테이너 ID를 출력함

--publish: 컨테이너의 포트를 호스트 컴퓨터에 공개함

## 컨테이너 중단/제거

#### 컨테이너 제거
- `docker container rm -f `

#### 실행 중인 모든 컨테이너 제거
- `docker container rm -f $(docker container ls -a -q)`
- 호스트 컴퓨터에 존재하는 실행 중인 모든 컨테이너 목록을 아무런 확인 절차없이 제거함

## 이미지

#### 이미지 빌드
- `docker image build -t <image_name>[:<tag>] <file_path>`

#### 이미지 빌드 과정 확인
- `docker image history <image_name> or <image_id>`

#### 이미지 참조 부여
- `docker image tag <image_name> [<docker registry>/]<group or docker_username>/<repository_name>[:<tag>]`
- `docker image tag simple-java-project hansanhha/simple-java-project:0.1.0`

#### 이미지 푸시
- `docker image push [<docker registry>/]<group or docker_username>/<repository_name>[:<tag>]`
- `docker image push hansanhha/simple-java-project:0.1.0`