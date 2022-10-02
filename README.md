# ocistorage
Oracle Cloud의 Object Storage 활용을 위한 API를 제공하는 솔루션 프로젝트


## 기술 스택
+ Java 1.8
+ Spring Boot 2.7.0
+ Spring Webflux
+ OCI Java Sdk 2.8.1


## 필수
모든 API에는 **ACL을 위한 아이디와 비밀번호 정보가 필수**입니다.

API 사용을 위한 아이디 발급 혹은 정보를 알고 싶다면 뿡빵뿡뿡

## 사용 예시
com.poozim.web.example 패키지 내에 Java 예시 소스코드가 있습니다.

예시코드는 계속해서 업데이트 중

## API 명세

### 버킷 생성

#### Request
 + URL : /storage/bucket
 + Method : POST
 + Type : application/json
 + Header

|Name|Type|Description|Require|
|---|---|---|---|
|ACL-ID|String|아이디|Y|
|ACL-PASSWD|String|비밀번호|Y|

<br/>

 + Body

|Name|Type|Description|Require|
|---|---|---|---|
|bucketName|String|버킷명|Y|

<br/>

#### Response

 + Body
 
|Name|Type|Description|Nullable|
|---|---|---|---|
|status|Integer|응답코드|N|
|success|Boolean|성공여부|N|
|data|String|버킷명|Y|
|msg|String|응답메세지|Y|


