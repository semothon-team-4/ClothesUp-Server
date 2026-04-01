# Clothes Up API 명세서

## 공통 규칙

### Base URL
```
https://api.clothesup.com/api/v1
```

### 인증
- JWT Bearer Token 방식
- 인증 필요 API는 Header에 `Authorization: Bearer {token}` 포함
- 인증 필요 API에는 모두 @AuthenticationPrincipal 사용

### 공통 Response 형식
```json
{
  "message": "status code: description",
  "data": {}
}
```

### 공통 상태 코드
| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 400 | 잘못된 요청 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 500 | 서버 오류 |

---

## 1. Auth (인증)

### 1-1. 회원가입
- **POST** `/auth/register`
- **Auth** 불필요

**Request Body**
```json
{
  "email": "string",
  "password": "string",
  "nickname": "string"
}
```

**Response 201**
```json
{
  "message": "201: 회원가입 성공",
  "data": {
    "id": "long",
    "email": "string",
    "nickname": "string",
    "createdAt": "datetime"
  }
}
```

---

### 1-2. 로그인
- **POST** `/auth/login`
- **Auth** 불필요

**Request Body**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response 200**
```json
{
  "message": "200: 로그인 성공",
  "data": {
    "accessToken": "string",
    "tokenType": "Bearer"
  }
}
```

---

### 1-3. 로그아웃
- **POST** `/auth/logout`
- **Auth** 필요

**Response 200**
```json
{
  "message": "200: 로그아웃 성공",
  "data": {}
}
```

---

## 2. User (사용자)

### 2-1. 내 프로필 조회
- **GET** `/users/me`
- **Auth** 필요

**Response 200**
```json
{
  "message": "200: 프로필 조회 성공",
  "data": {
    "id": "long",
    "email": "string",
    "nickname": "string",
    "profileImage": "string(url)",
    "createdAt": "datetime"
  }
}
```

---

### 2-2. 내 프로필 수정
- **PUT** `/users/me`
- **Auth** 필요
- **Content-Type** `multipart/form-data`

**Request Body**
```
nickname: string (optional)
profileImage: file (optional)
```

**Response 200**
```json
{
  "message": "200: 프로필 수정 성공",
  "data": {
    "id": "long",
    "email": "string",
    "nickname": "string",
    "profileImage": "string(url)"
  }
}
```

---

## 3. Shop (세탁소)

### 3-1. 주변 세탁소 목록 조회
- **GET** `/shops`
- **Auth** 필요

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| lat | double | Y | 위도 |
| lng | double | Y | 경도 |
| radius | int | N | 반경 (미터, 기본값 1000) |

**Response 200**
```json
{
  "message": "200: 세탁소 목록 조회 성공",
  "data": [
    {
      "id": "long",
      "placeId": "string",
      "name": "string",
      "address": "string",
      "lat": "double",
      "lng": "double"
    }
  ]
}
```

---

### 3-2. 세탁소 상세 조회
- **GET** `/shops/{shopId}`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| shopId | long | 세탁소 ID |

**Response 200**
```json
{
  "message": "200: 세탁소 상세 조회 성공",
  "data": {
    "id": "long",
    "placeId": "string",
    "name": "string",
    "address": "string",
    "lat": "double",
    "lng": "double",
    "prices": [
      {
        "id": "long",
        "category": "string",
        "price": "int",
        "priceGrade": "string"
      }
    ],
    "createdAt": "datetime"
  }
}
```

---

### 3-3. 세탁소 등록
- **POST** `/shops`
- **Auth** 필요

**Request Body**
```json
{
  "placeId": "string",
  "name": "string",
  "address": "string",
  "lat": "double",
  "lng": "double"
}
```

**Response 201**
```json
{
  "message": "201: 세탁소 등록 성공",
  "data": {
    "id": "long",
    "name": "string",
    "address": "string",
    "lat": "double",
    "lng": "double",
    "createdAt": "datetime"
  }
}
```

---

### 3-4. 세탁소 가격 목록 조회
- **GET** `/shops/{shopId}/prices`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| shopId | long | 세탁소 ID |

**Response 200**
```json
{
  "message": "200: 가격 목록 조회 성공",
  "data": [
    {
      "id": "long",
      "category": "string",
      "price": "int",
      "priceGrade": "string"
    }
  ]
}
```

---

### 3-5. 세탁소 가격 등록
- **POST** `/shops/{shopId}/prices`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| shopId | long | 세탁소 ID |

**Request Body**
```json
{
  "prices": [
    {
      "category": "string",
      "price": "int",
      "priceGrade": "string"
    }
  ]
}
```

**Response 201**
```json
{
  "message": "201: 가격 등록 성공",
  "data": [
    {
      "id": "long",
      "category": "string",
      "price": "int",
      "priceGrade": "string"
    }
  ]
}
```

---

## 4. Receipt (영수증)

### 4-1. 영수증 업로드
- **POST** `/receipts`
- **Auth** 필요
- **Content-Type** `multipart/form-data`

**Request Body**
```
shopId: long
image: file
```

**Response 201**
```json
{
  "message": "201: 영수증 업로드 성공",
  "data": {
    "id": "long",
    "shopId": "long",
    "imageUrl": "string(url)",
    "createdAt": "datetime"
  }
}
```

---

### 4-2. 영수증 상세 조회
- **GET** `/receipts/{receiptId}`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| receiptId | long | 영수증 ID |

**Response 200**
```json
{
  "message": "200: 영수증 조회 성공",
  "data": {
    "id": "long",
    "shopId": "long",
    "imageUrl": "string(url)",
    "createdAt": "datetime"
  }
}
```

---

## 5. Review (리뷰)

### 5-1. 세탁소 리뷰 목록 조회
- **GET** `/shops/{shopId}/reviews`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| shopId | long | 세탁소 ID |

**Response 200**
```json
{
  "message": "200: 리뷰 목록 조회 성공",
  "data": [
    {
      "id": "long",
      "userId": "long",
      "nickname": "string",
      "profileImage": "string(url)",
      "rating": "int",
      "content": "string",
      "images": ["string(url)"],
      "createdAt": "datetime"
    }
  ]
}
```

---

### 5-2. 리뷰 작성
- **POST** `/shops/{shopId}/reviews`
- **Auth** 필요
- **Content-Type** `multipart/form-data`

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| shopId | long | 세탁소 ID |

**Request Body**
```
receiptId: long
rating: int (1~5)
content: string
images: file[] (optional)
```

**Response 201**
```json
{
  "message": "201: 리뷰 작성 성공",
  "data": {
    "id": "long",
    "shopId": "long",
    "receiptId": "long",
    "rating": "int",
    "content": "string",
    "images": ["string(url)"],
    "createdAt": "datetime"
  }
}
```

---

### 5-3. 리뷰 삭제
- **DELETE** `/reviews/{reviewId}`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| reviewId | long | 리뷰 ID |

**Response 200**
```json
{
  "message": "200: 리뷰 삭제 성공",
  "data": {}
}
```

---

## 6. Analysis (옷 분석)

### 6-1. 옷 분석 요청
- **POST** `/analyses`
- **Auth** 필요
- **Content-Type** `multipart/form-data`

**Request Body**
```
name: string
category: string
image: file
```

**Response 201**
```json
{
  "message": "201: 분석 요청 성공",
  "data": {
    "id": "long",
    "name": "string",
    "category": "string",
    "imageUrl": "string(url)",
    "careLabel": {
      "id": "long",
      "labels": [
        {
          "id": "long",
          "name": "string",
          "imageUrl": "string(url)"
        }
      ]
    },
    "condition": {
      "id": "long",
      "grade": "string(A|B|C|D)",
      "stainLevel": "int",
      "damageLevel": "int",
      "recommendation": "string"
    },
    "createdAt": "datetime"
  }
}
```

---

### 6-2. 내 옷장 목록 조회
- **GET** `/analyses`
- **Auth** 필요

**Response 200**
```json
{
  "message": "200: 옷장 목록 조회 성공",
  "data": [
    {
      "id": "long",
      "name": "string",
      "category": "string",
      "imageUrl": "string(url)",
      "createdAt": "datetime"
    }
  ]
}
```

---

### 6-3. 분석 결과 상세 조회
- **GET** `/analyses/{analysisId}`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| analysisId | long | 분석 ID |

**Response 200**
```json
{
  "message": "200: 분석 결과 조회 성공",
  "data": {
    "id": "long",
    "name": "string",
    "category": "string",
    "imageUrl": "string(url)",
    "careLabel": {
      "id": "long",
      "labels": [
        {
          "id": "long",
          "name": "string",
          "imageUrl": "string(url)"
        }
      ]
    },
    "condition": {
      "id": "long",
      "grade": "string(A|B|C|D)",
      "stainLevel": "int",
      "damageLevel": "int",
      "recommendation": "string"
    },
    "createdAt": "datetime"
  }
}
```

---

### 6-4. 분석 항목 삭제
- **DELETE** `/analyses/{analysisId}`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| analysisId | long | 분석 ID |

**Response 200**
```json
{
  "message": "200: 분석 항목 삭제 성공",
  "data": {}
}
```

---

## 7. Post (커뮤니티)

### 7-1. 게시글 목록 조회
- **GET** `/posts`
- **Auth** 필요

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| page | int | N | 페이지 번호 (기본값 0) |
| size | int | N | 페이지 크기 (기본값 20) |

**Response 200**
```json
{
  "message": "200: 게시글 목록 조회 성공",
  "data": {
    "content": [
      {
        "id": "long",
        "userId": "long",
        "nickname": "string",
        "profileImage": "string(url)",
        "title": "string",
        "content": "string",
        "analysisId": "long",
        "likeCount": "int",
        "commentCount": "int",
        "isPublic": "boolean",
        "createdAt": "datetime"
      }
    ],
    "totalElements": "int",
    "totalPages": "int",
    "currentPage": "int"
  }
}
```

---

### 7-2. 게시글 상세 조회
- **GET** `/posts/{postId}`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| postId | long | 게시글 ID |

**Response 200**
```json
{
  "message": "200: 게시글 조회 성공",
  "data": {
    "id": "long",
    "userId": "long",
    "nickname": "string",
    "profileImage": "string(url)",
    "title": "string",
    "content": "string",
    "analysisId": "long",
    "isPublic": "boolean",
    "likeCount": "int",
    "isLiked": "boolean",
    "createdAt": "datetime"
  }
}
```

---

### 7-3. 게시글 작성
- **POST** `/posts`
- **Auth** 필요

**Request Body**
```json
{
  "analysisId": "long",
  "title": "string",
  "content": "string",
  "isPublic": "boolean"
}
```

**Response 201**
```json
{
  "message": "201: 게시글 작성 성공",
  "data": {
    "id": "long",
    "title": "string",
    "content": "string",
    "isPublic": "boolean",
    "createdAt": "datetime"
  }
}
```

---

### 7-4. 게시글 삭제
- **DELETE** `/posts/{postId}`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| postId | long | 게시글 ID |

**Response 200**
```json
{
  "message": "200: 게시글 삭제 성공",
  "data": {}
}
```

---

### 7-5. 게시글 좋아요
- **POST** `/posts/{postId}/likes`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| postId | long | 게시글 ID |

**Response 200**
```json
{
  "message": "200: 좋아요 성공",
  "data": {}
}
```

---

### 7-6. 게시글 좋아요 취소
- **DELETE** `/posts/{postId}/likes`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| postId | long | 게시글 ID |

**Response 200**
```json
{
  "message": "200: 좋아요 취소 성공",
  "data": {}
}
```

---

## 8. Comment (댓글)

### 8-1. 댓글 목록 조회
- **GET** `/posts/{postId}/comments`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| postId | long | 게시글 ID |

**Response 200**
```json
{
  "message": "200: 댓글 목록 조회 성공",
  "data": [
    {
      "id": "long",
      "userId": "long",
      "nickname": "string",
      "profileImage": "string(url)",
      "content": "string",
      "createdAt": "datetime"
    }
  ]
}
```

---

### 8-2. 댓글 작성
- **POST** `/posts/{postId}/comments`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| postId | long | 게시글 ID |

**Request Body**
```json
{
  "content": "string"
}
```

**Response 201**
```json
{
  "message": "201: 댓글 작성 성공",
  "data": {
    "id": "long",
    "userId": "long",
    "nickname": "string",
    "content": "string",
    "createdAt": "datetime"
  }
}
```

---

### 8-3. 댓글 삭제
- **DELETE** `/comments/{commentId}`
- **Auth** 필요

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| commentId | long | 댓글 ID |

**Response 200**
```json
{
  "message": "200: 댓글 삭제 성공",
  "data": {}
}
```