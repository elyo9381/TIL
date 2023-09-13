
# TIW( today i work )

- maas 예약관리 - 예약내역 리스트 수정
    - CarTourSearchDTO에 이용일 미지정 여부  필드 추가
    
      @ApiModelProperty("이용일 미지정 여부")var unSpecifiedUsedtYn:String? = null
    
    - AdminCarTourMapper 쿼리수정
    

### RestTemplate 이란

- 스프링에서 제공하는 HTTP통신 기능을 쉽게 사용할 수 있게 설계되어 있는 템플릿
- HTTP 서버와의 통신을 단순화하고 RESTful원칙을 지킴
- 동기 방식으로 처리되며, 비동기 방식으로는 webClient가 이 있음
- RestTemplate 클래스 REST서비스를 호출하도록 설계되어 HTTP 프로토콜의 메소드에 맞게 여러 메소드를 제공

### 제공 메서드

![Untitled](Untitled.png)

- Object가 붙은 메서드는 Object를 반환합니다.
- Entity가 붙으면 Entity를 반환합니다.

spring은 restTemplate  빈으로 등록해주지 않고  RestTemplateBuilder.build()를 빈으로 등록해줍니다. 

이것은 블럭킹 콜입니다. 

webClinet는 논블럭킹 비동기 콜이 가능합니다. 

webClient또한 restTemplate와 동일합니다.  WebClient.Builder를 빈으로 등록 가능합니다. 

webClientBuild.baseUrl("~~").build();

### RestTemplate 사용방법

- postforEntity메소드 url, request, responseType등을 파라미터로 지정하여 작성합니다.
- exchange메소드는 Object 및 response를 반환하는 암시적 메소드입니다.
- exchange는  url, httpMathod, request, responseType등을 파라미터로 받습니다.
- 헤더 정보를 넣기 위해서는 HttpHeaders()를 사용합니다. UrlBuilder는 이제 사용하지 않습니다.

### open Feign

- REST Call을 추상화 한 spring colud netflix 라이브러리
- Load balanced  지원

사용방법

- 호출하려는 HTTP EndPoint에 대한 interface를 생성
- @FeignClient 선언