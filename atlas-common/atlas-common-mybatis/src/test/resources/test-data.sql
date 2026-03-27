-- Test data for DataScopeInterceptor bug condition test

-- Insert test users
INSERT INTO test_user (user_id, username, dept_id, deleted) VALUES (1, 'admin', 1, 0);
INSERT INTO test_user (user_id, username, dept_id, deleted) VALUES (112, 'testuser', 5, 0);
INSERT INTO test_user (user_id, username, dept_id, deleted) VALUES (113, 'otheruser', 6, 0);
INSERT INTO test_user (user_id, username, dept_id, deleted) VALUES (114, 'anotheruser', 7, 0);
