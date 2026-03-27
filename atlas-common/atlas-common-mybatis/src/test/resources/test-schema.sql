-- Test schema for DataScopeInterceptor bug condition test

DROP TABLE IF EXISTS test_user;

CREATE TABLE test_user (
    user_id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    dept_id BIGINT,
    deleted INT DEFAULT 0
);
