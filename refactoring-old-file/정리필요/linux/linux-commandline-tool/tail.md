tail은 문서의 뒷부분을 보여주는 툴이다.

-c : --bytes [+]num 

-n : --lines [+]num 옵션은 원하는 라인만 출력한다.

-f : --follow 어떤 파일의 추가되는 내용대기,추가되는 내용은 append하여 출력

-F : 파일이 지웠졌다가 다시 생겨도 follow를 한다. 


tail 명령어의 활용

tail -F /var/log/auth.log
를 통해서 
리눅스 컴퓨터에 들어오는 로그를 실시간 모니터링 할수있다.