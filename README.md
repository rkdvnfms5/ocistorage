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

### 1. 버킷 생성

#### Request
 + URL : /storage/bucket
 + Method : POST
 + Type : application/json
 + Header

|Name|Type|Description|Require|Default|
|---|---|---|---|---|
|ACL-ID|String|아이디|Y||
|ACL-PASSWD|String|비밀번호|Y||

<br/>

 + Body

|Name|Type|Description|Require|Default|Ex|
|---|---|---|---|---|---|
|bucketName|String|버킷명|Y|||

<br/>

#### Response

 + Body
 
|Name|Type|Description|Nullable|
|---|---|---|---|
|status|Integer|응답코드|N|
|success|Boolean|성공여부|N|
|data|String|버킷명|Y|
|msg|String|응답메세지|Y|

<br/>

### 2. 사전인증 생성

#### Request
 + URL : /storage/preauth
 + Method : POST
 + Type : application/json
 + Header

|Name|Type|Description|Require|Default|
|---|---|---|---|---|
|ACL-ID|String|아이디|Y||
|ACL-PASSWD|String|비밀번호|Y||
|X-BUCKET|String|버킷명|Y||

<br/>

 + Body

|Name|Type|Description|Require|Default|Ex|
|---|---|---|---|---|---|
|expireDate|String|유효기간|N|5 years|YYYY-MM-DD|

<br/>

#### Response

 + Body
 
|Name|Type|Description|Nullable|
|---|---|---|---|
|status|Integer|응답코드|N|
|success|Boolean|성공여부|N|
|data|String|사전인증값|Y|
|msg|String|응답메세지|Y|

<br/>

### 3. 오브젝트 생성

#### Request
 + URL : /storage/object
 + Method : POST
 + Type : multipart/form-data
 + Header

|Name|Type|Description|Require|Default|
|---|---|---|---|---|
|ACL-ID|String|아이디|Y||
|ACL-PASSWD|String|비밀번호|Y||
|X-BUCKET|String|버킷명|Y||

<br/>

 + Body

|Name|Type|Description|Require|Default|Ex|
|---|---|---|---|---|---|
|files|File|업로드할 파일|Y||파일명에 확장자 필수|

<br/>

#### Response

 + Body
 
|Name|Type|Description|Nullable|
|---|---|---|---|
|status|Integer|응답코드|N|
|success|Boolean|성공여부|N|
|data|String[]|업로드 성공한 파일명|Y|
|msg|String|응답메세지|Y|
