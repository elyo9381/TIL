## Shell

    hardware : 기계들을 동작시켜서 원하는 결과를 얻는것. 

    kernel : 물리적인 기계를 직접적으로 제어하는 운영체제에서 가장중요시 되는 코어가 되는 부분

    shell : 사용자가 입력한 명령이 shell에게 입력하고 커널이 이해할수있는 방식으로 해석및 전달해준다.
          : 커널을 직접 제어하기 어려움으로 쉘에 해당하는 프로그램이 명령어를 해석해서 커널에게 전달해주는 것

    쉘과 커널을 분리하여 얻은 장점 : 사용자의 입장에서는 쉘을 선택해서 취향에 맞게 커널을 제어할수있기 때문으로 추정

    기본적인 bash shell이 있다. 

    zsh : 다양한 명령어를 사용자가 편하게 사용하기 위해서 만들어진 쉘

# shell Script

      /bin : 기본 프로그램이 위치하는 프로그램집합소

      쉘스크립트는 여러가지 명령어를 자동으로 실행되게 하는 스크립트이댜.
      쉘에서 실행되는 스크립트이다. 
      쉘에서 실행되는 것이고 사용자가 각본을 짜서 실행하는것이다. 

      #! /bin/bash   // 이 밑에 작성되는 코드들이 bin/bash를 통해서 해석되라는 약속이다.
      if ! [ -d bak ]; then  //  현재 디렉터리에 bak가 존재한다면
            mkdir bak        //  그렇지 않는다면 없다면 생성해라 
      fi                      // 조건문 종료를 알림
      cp *.log bak

# directory struct

      bin : User Binaries(유저가 사용하기 편하기 위한 파일 bash..)

      sbin : System Binaries(루트가 필요한 실행파일)
      
      etc : configuration files(컴퓨터& 프로그램 동작 설정파일)

      dev : Device Files()

      proc : process Information

      var : Variable Files(변할수있는 파일들이 들어가있다. [ex) log,network,] )

      tmp : Temporary files(임시파일들을 저장하고 컴퓨터가 꺼지면 사라진다 파일들이 )

      home : Home Directories (사용자들의디렉터리 사용자의 파일들이 저장되어있는 디렉터리)

      usr : 

      boot : boot loader files

      lib : System libraries

      opt : optional add-on applications

      mnt : Mount directory
      
      media : Removable media Devies

      srv : servics Data


* 파일 찾는 법 
  - location
    location *.log의 모든 파일을 찾아준다. 
      
      특징 디렉터리를 뒤지지 않는다. 파일의 정보를 가지는 데이터베이스를 뒤져본다. locate가 확인하는 DB는 mlocate라는 곳이다. 

  - find
    find 경우는 직접 파일을 뒤져봅니다. 다양한 사용법이 존재한다. 
    find는 여려가지 옵션을 줄수있다. 
    / << 루트에서부터
    . << 현재파일에서부터 

    ex) find / -name *.log

    whereis 명령어가 어디있는지 알려주는명령어
    ex) whereis ls

    $PATH : ':' 을 통해서 구분이 되어있다
           명령어가 path를 참조하고 이를 확인하고 실행한다. 