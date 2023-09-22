
# How it works

## 코어 컴포넌트
[core componentes](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.CoreComponents.html)


다이나모 DB는 위의 3가지 개념을 가지고 있습니다. 아래와 같습니다. 
### table
다른 데이터베이스 시스템과 마찬가지로 데이터를 테이블에 저장합니다. 
테이블은 데이터(item)들의 모음입니다.
table은 0개이상의 item을 보유해야합니다.

### items
RDB와 같이 비교할때, items == records 이라고 볼수 있습니다. 
item은 하나의  record(row, tuple) 과 비슷하다고 볼수있습니다.
item은 고유의 식별자를 가지고 있습니다. (유니크 key)
item은 table에 저장되는데 table은 0개이상의 item을 보유해야합니다.
테이블에 저장할수 있는 항목의 제한이 없습니다. 

### attributes
item은 여러 속성으로 구성될수있습니다. 
다른 데이터 베이스의 필드, 열과 유사합니다. 


```
People

{
    "PersonID": 101,
    "LastName": "Smith",
    "FirstName": "Fred",
    "Phone": "555-4321"
}

{
    "PersonID": 102,
    "LastName": "Jones",
    "FirstName": "Mary",
    "Address": {
                "Street": "123 Main",
                "City": "Anytown",
                "State": "OH",
                "ZIPCode": 12345
    }
}

{
    "PersonID": 103,
    "LastName": "Stephens",
    "FirstName": "Howard",
    "Address": {
                "Street": "123 Main",
                "City": "London",                                    
                "PostalCode": "ER3 5K8"
    },
    "FavoriteColor": "Blue"
}
```

People의 테이블의 예제 특징
- 테이블은 여러 항목으로 구성된다.
- 항목에는 PersonID라는 key와 다른 속성으로 구성된다.
- 다른 속성들은 미리 정의 되지 않아도 된다.(mongDB처럼 document의 느낌을 갖는다.)
- 속성은 [[scalar]] 입니다. 하나의 값만 가질수 있습니다. 

위와 같은 특징이 존재하며, 기본키(PK)를 구성하는 다양한 방법이 존재합니다.



## 기본키

- 테이블 생성시 기본키를 지정 해야합니다. (필수)
- 기본키는 유니크 해야합니다.

### 파티션키

- 하나의 속성으로 구성된 단순 기본키
``` 
{
    "PersonID": 101,
    "LastName": "Smith",
    "FirstName": "Fred",
    "Phone": "555-4321"
}
```
예를 들어 `PersonID` 와 같은 형태입니다.

- 파티션 키 및 정렬 키 (두가지의 속성으로 이루어진 기본키)

``` json
Music
{
	"Artist": "No One You Know",
	"SongTitle": "My Dog Spot",
	"AlbumTitle": "Hey Now",
	"Price": 1.98,
	"Genre": "Country",
	"CriticRating": 8.4
}

{
	"Artist": "No One You Know",
	"SongTitle": "Somewhere Down The Road",
	"AlbumTitle": "Somewhat Famous",
	"Genre": "Country",
	"CriticRating": 8.4,
	"Year": 1984
}
```

Artist , SongTitle 두개를 가지고 기본키를 구성할수 있습니다. 
여기서 기본키의 자세한 구성은 `파티션키 + 정렬키` 로 말할 수 있습니다.

- 파티션 키만 제공한다면 Artist에 대한 모든것을 액세스
- 파티션키 + 정렬키 까지 제공 한다면 Artist , SongTitle 정렬되고 액세스 가능합니다.

## 보조 인덱스

DynamoDB의 각 테이블에는 글로벌 보조 인덱스 20개(기본 할당량)와 로컬 보조 인덱스 5개의 할당량이 있습니다.

보조 인덱스를 사용하면 기본키에 대한 쿼리 이외에도 테이블의 데이터에 쿼리를 할 수 있습니다.

`2종류의 보조 인덱스`
- 글로벌 보조 인덱스 – 테이블의 파티션 키와 정렬 키가 다를 수 있는 인덱스입니다. 
> [!글로벌 보조 인덱스]  위의 Music  참고
> Artist , SongTitle (기존 파티션키 , 정렬키) 
> 하지만 새로운 보조 인덱스로 기존 파티션키 ,정렬키를 놔두고 
> 인덱스를 위한 파티션키  = Genre , 정렬키 = AlbumTitle 을 설정 할 수 있습니다. 
> 이를 통해서 Genre , AlbumTitle 두가지만으로 인덱스가 설정 되고 조회가 가능합니다.

- 로컬 보조 인덱스 – 테이블과 파티션 키는 동일하지만 정렬 키가 다른 인덱스입니다.
> [!로컬 보조 인덱스]  위의 Music  참고
> 특정 하나의 키워드로 조회를 하고 싶을때  ? 
> CriticRating 이라는 속성으로 조회 및 정렬을 해야할때 
> 로컬 보조 인덱스로 CriticRating 을 설정 할수 있습니다. 
> 이렇게 되면 파티션키는 Artist가 되고, CriticRating 가 정렬키가 되어 
> 조회 및 정렬 가능




