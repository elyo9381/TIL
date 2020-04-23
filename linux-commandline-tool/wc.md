wc : line/word/byte count 출력

-ㅣ  :라인수만 출력

wc -l /etc/passwd | awk '{print $1}'
wc -l /etc/passwd | cut -d ' ' -f 1
                    //cut -d : delimiter(구분자)
                    //    -f : fields
example

    wc FILENAME
    wc -l FILENAME
    cat FILENAME | wc -l


    ~~~

    wc /etc/passwd // execute
    
    19 24 926 /etc/passwd <== 결과
    // 첫번째 라인의수, 두번째 word의갯수 , 세번째 byte 각각의 count 로 구성되어 있다.
    ~~~

