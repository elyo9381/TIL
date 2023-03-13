nl : 파일 내용을 라인을 넣어서 출력한다.

ex) nl /etc/passwd

cat /etc/passwd | nl 
위와 같은 결과가 나온다. 

nl명령어는 공백을 출력하지않는다. 

이럴때 유명하게 사용할수있는 옵션은 
nl -ba test.c
공백을 이쁘게 출력할수있게 해준다. 

