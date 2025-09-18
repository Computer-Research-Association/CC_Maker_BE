-- 개인정보 동의서 테이블 생성
CREATE TABLE privacy_agreements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version VARCHAR(20) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    effective_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- users 테이블에 개인정보 동의 관련 컬럼 추가
ALTER TABLE users 
ADD COLUMN privacy_agreement_version VARCHAR(20),
ADD COLUMN privacy_agreed BOOLEAN DEFAULT FALSE,
ADD COLUMN privacy_agreed_at DATETIME,
ADD COLUMN privacy_agreed_method VARCHAR(50),
ADD COLUMN privacy_agreed_environment VARCHAR(20);

-- 초기 개인정보 동의서 데이터 삽입 (v1.0)
INSERT INTO privacy_agreements (version, content, effective_date, created_at, is_active) VALUES (
    'v1.0',
    '개인정보 수집 및 이용에 대한 안내

1. 수집하는 개인정보 항목
- 필수항목: 이름, 생년월일, 이메일, 비밀번호, 성별
- 선택항목: 없음

2. 개인정보의 수집 및 이용목적
- 회원가입 및 서비스 이용
- 서비스 제공 및 운영
- 고객상담 및 문의응답
- 서비스 개선 및 신규 서비스 개발

3. 개인정보의 보유 및 이용기간
- 회원 탈퇴 시까지 (단, 관련 법령에 따라 보존이 필요한 경우 해당 기간까지)

4. 개인정보의 파기절차 및 방법
- 전자적 파일 형태로 저장된 개인정보는 복구 불가능한 방법으로 영구 삭제
- 종이에 출력된 개인정보는 분쇄기로 분쇄하거나 소각을 통하여 파기

5. 동의 거부권 및 동의 거부에 따른 불이익
- 개인정보 수집 및 이용에 대한 동의를 거부할 수 있습니다.
- 동의를 거부할 경우 회원가입 및 서비스 이용이 제한됩니다.

위 내용에 동의하시면 체크박스를 선택해 주세요.

─────────────────────────────────────────
시행일: 2025.08.20 / 버전: v1.0',
    '2025-08-20',
    NOW(),
    TRUE
);

-- 인덱스 생성
CREATE INDEX idx_privacy_agreements_version ON privacy_agreements(version);
CREATE INDEX idx_privacy_agreements_active ON privacy_agreements(is_active);
CREATE INDEX idx_users_privacy_version ON users(privacy_agreement_version);
CREATE INDEX idx_users_privacy_agreed ON users(privacy_agreed);
