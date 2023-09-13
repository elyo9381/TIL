## 배수의합

    자연수 N이 입력되면 1부터 n까지의 수중 m의 배수합을 출력하는 프로그램을 작성하세요.

    ~~~
    #include <iostream>
    usgin namespace std;

    int main(){
        int n,m, sum = 0;

        for(int i =  0; i<=n; i++)
        {
            if(i % m == 0)
            {
                sum += i;

            }
        }
        
        cout << sum << endl;
    }
    ~~~