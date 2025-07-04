CREATE TABLE IF NOT EXISTS mbti_score (
    from_mbti VARCHAR(4) NOT NULL,
    to_mbti VARCHAR(4) NOT NULL,
    score INT NOT NULL,
    PRIMARY KEY (from_mbti, to_mbti)
);
