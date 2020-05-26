## basic linux

ls : 현재 디렉토리의 파일 목록을 출력하는 명령어

pwd : 현재 위치하고 있는 디렉터리 출력하는 명렁어

mkdir : 새로 생성할 디렉터리 

cd : 이동할 디렉터리의 경로명

rm : 파일 혹은 디렉터리를 삭제할때 사용된다. 

* --help 와 man

    mkdir --help 

    man ls 

    help 와 man 의 차이는 man은 전용페이지로 이동해서 상세한 메뉴얼을 보여준다. help는 기존페이지에서 간단하게 보여준다.


cp filename filename2 // 파일복사

mv filename filename2 // 파일 이동 및 파일 이름 변경

* 파일다운로드
  - wget 주소 : CLI 환경에서 다운로드 할수있는것 


* IO Redirection - output
IO Redirection : input/output 방향을 바꾼다.

ls -l > result.txt // > 은 리다이렉션의 의미이고 ls -l의 정보가 result.txt에 담기게 된다. (output으로)

rename2.txt파일 존재하지 않을때
rm -rf rename2.txt 1> result.txt 2> error.log
 1> result.txt // 결과를 result.txt 리다이렉트 
 2> error.log // 결과를 error.log redirect

* IO Redirection - input
cat 은 입력을 기다리고 입력을 그대로 출력해준다. 

cat hello.txt
아구먼트로 인자를 받은것이고

cat < hello.txt
stdin으로 입력을 한것이다. 

* IO Redirection - append
ls -al >> result.txt // 뒤에 append 한다 매번 실행할따마다
