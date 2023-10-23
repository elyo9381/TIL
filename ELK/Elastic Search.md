
- 뛰어난 검색엔진 입니다.
- 대규모 분산 시스템 구축

## 1.소개
### 오픈소스

### 실시간 분석
Elasticsearch는 클러스타가 실행되고 있는 동안, 계속적인 데이터가 입력 될수 있습니다.
그리고 이와 동시에 실시간에 가까운 속도로 색인된 데이터 검색, 집계가 가능합니다.

### 전문검색 엔진
루씬은 기본적으로 [[역파일 색인(Inverted file index)]]라는 수조로 데이터를 저장합니다. 

Json 문서 기반으로 내부적으로 역파일 색인 구조로 데이터를 저장합니다.

### 멀티테넌시(Multitenancy)

서로 다른 인덱스들을 별도의 커넥션 없이 하나의 질의로 묶어서 검색하고, 검색 결과들을 하나의 출력으로 도출할 수 있습니다.
이러한 특징을 멀티테넌시 라고 합니다.

## 2. 시작하기

```
➜  elasticsearch-8.10.4 bin/elasticsearch -d

어찌저찌 실행됨

➜  elasticsearch-8.10.4 ps -ef | grep elasticsearch

  501 76384     1   0  1:00PM ttys001    0:55.33 /Users/refine/DEV/elasticsearch-8.10.4/jdk.app/Contents/Home/bin/java -Des.networkaddress.cache ~~ 
  
➜  elasticsearch-8.10.4 kill 76384

```

bin/elasticsearch 
-> 을 통해서 실행

ps -ef | grep elasticsearch
-> 프로세스 실행중임을 확인

kill 76384
-> 프로세스 종료 ( 본인이 실행한 pid 로 종료)