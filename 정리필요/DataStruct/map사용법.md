## map 사용법

* map

```
ios_base::sync_with_stdio(false);
ifstream cin;
cin.open("input.txt");

map<char,int> ch;
map<char,int>::iterator it;
char a[100];
cin >> a;

for(int  i = 0; a[i]!='\0'; i++){
    ch[a[i]]++;
}

---------------------------------

char a[100];
int n;
cin >> n;
for(int i = 1; i<=n; i++){
    cin >> a;
    ch[a]++;
}
```