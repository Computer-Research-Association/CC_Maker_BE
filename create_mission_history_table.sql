-- mission_history 테이블 생성
CREATE TABLE mission_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sub_group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    mission_template_id BIGINT NOT NULL,
    completed_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (sub_group_id) REFERENCES sub_group(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (mission_template_id) REFERENCES mission_template(id)
);

-- 인덱스 추가 (성능 향상을 위해)
CREATE INDEX idx_mission_history_user_id ON mission_history(user_id);
CREATE INDEX idx_mission_history_sub_group_id ON mission_history(sub_group_id);
CREATE INDEX idx_mission_history_completed_at ON mission_history(completed_at);
