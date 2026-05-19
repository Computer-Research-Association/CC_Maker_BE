# CC_Maker BE

> 캠퍼스 미팅/소개팅 매칭 플랫폼의 백엔드 서버

## 프로젝트 소개

CC_Maker는 그룹 내 남녀 멤버를 **MBTI 궁합 + 설문 유사도** 기반으로 자동 매칭해주는 소개팅 플랫폼입니다.  
팀 단위로 운영되며, 초대 코드로 팀에 참여한 멤버들이 설문을 완료하면 알고리즘이 최적의 소그룹을 구성하고, 매칭된 그룹에게 함께 수행할 미션을 부여합니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.3 |
| Security | Spring Security + JWT (Access/Refresh Token) |
| Database | MySQL 8 |
| ORM | Spring Data JPA |
| Infra | Docker, Docker Compose |
| Build | Gradle |

## 주요 기능

### 1. 인증 (Auth)
- 이메일/비밀번호 로그인, JWT 발급 (HttpOnly 쿠키 + 응답 바디 이중 지원)
- Access Token / Refresh Token 분리 운용, 자동 갱신
- 개인정보 동의 처리

### 2. 팀 관리 (Team)
- 팀 생성 및 초대 코드 발급/입력으로 팀 참여
- 팀별 역할(LEADER/MEMBER) 구분
- 설문 완료 여부 추적

### 3. 설문 (Question/Answer)
- 팀 관리자가 팀별 질문 생성/수정/삭제
- 멤버가 각 질문에 점수(1~5점)로 답변
- MBTI 입력 (설문 제출 시 함께 저장)

### 4. 매칭 알고리즘 (Matching)
- **MBTI 궁합 점수 (50%) + 설문 유사도 (50%)** 가중치 합산
- 이성 간 1:1 후보 쌍을 점수 순 정렬 → 그리디 방식으로 최적 매칭
- 잉여 인원(성비 불균형) 자동 처리:
  - 남성 잔여: 2인씩 남남 그룹 구성, 홀수 1명은 커플 그룹에 편입
  - 여성 잔여: 커플 그룹에 유사도 기반으로 편입 (최대 4인)
- 매칭 결과로 SubGroup 생성, 멤버 조회 가능

### 5. 미션 (Mission)
- 매칭된 소그룹에 미션 자동 부여
- 미션 완료 처리, 새로고침(재배정) 기능
- 스코어보드로 그룹별 점수 랭킹 제공

## 프로젝트 구조

```
src/main/java/com/ccapp/ccgo/
├── auth/          # 로그인, JWT 발급/갱신/로그아웃
├── user/          # 회원가입, 정보 수정, 개인정보 동의
├── team/          # 팀 생성, 팀원 관리, 설문 상태
├── invitecode/    # 초대 코드 발급 및 팀 참여
├── question/      # 설문 질문/답변 CRUD
├── matching/      # MBTI + 유사도 기반 매칭 알고리즘
├── mission/       # 미션 배정, 완료, 스코어보드
└── common/        # 보안 설정, 예외 처리, 공통 Enum
```

## API 엔드포인트 요약

| 모듈 | 경로 | 설명 |
|------|------|------|
| Auth | `POST /api/auth/login` | 로그인 |
| Auth | `POST /api/auth/refresh` | 토큰 갱신 |
| Auth | `POST /api/auth/logout` | 로그아웃 |
| User | `POST /api/users/signup` | 회원가입 |
| Team | `GET /api/team/mine` | 내 팀 목록 |
| Team | `GET /api/team/{teamId}/survey-status` | 설문 완료 여부 |
| InviteCode | `POST /api/invite-code` | 초대 코드 생성 |
| InviteCode | `POST /api/invite-code/join` | 초대 코드로 팀 참여 |
| Matching | `POST /api/matching/{teamId}` | 매칭 실행 |
| Matching | `POST /api/matching/answers` | 설문 답변 제출 |
| Mission | `POST /api/missions/assign/subgroup/{id}` | 미션 부여 |
| Mission | `POST /api/missions/complete` | 미션 완료 |
| Scoreboard | `GET /api/scoreboard/{teamId}` | 점수 랭킹 조회 |

## 실행 방법

### 환경변수 설정

프로젝트 루트에 `.env` 파일을 생성합니다.

```env
DB_USER=your_db_user
DB_PASS=your_db_password
JWT_SECRET=your_jwt_secret
```

### Docker Compose로 실행

```bash
docker-compose up -d
```

앱 서버: `http://localhost:8080`  
MySQL: `localhost:3306` (DB명: `ccmake`)

### 로컬 실행 (MySQL 별도 구동 시)

```bash
./gradlew bootRun
```

## 매칭 알고리즘 상세

1. 팀 내 남성/여성 멤버를 분리
2. 모든 이성 조합에 대해 점수 계산:
   - **MBTI 점수**: `MBTIschema.sql` 기반의 궁합 테이블에서 조회 (양방향 합산)
   - **유사도 점수**: 질문별 답변 차이 `5 - |점수A - 점수B|` 평균
   - **최종 점수** = MBTI × 0.5 + 유사도 × 0.5
3. 점수 내림차순 정렬 후 그리디 매칭 → SubGroup 생성
4. 잉여 인원은 규칙에 따라 기존 그룹에 편입 (최대 4인)
