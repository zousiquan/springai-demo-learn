-- 创建数据库
CREATE DATABASE IF NOT EXISTS springai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE springai;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据
INSERT INTO users (user_id, username, password, status) VALUES
('1', 'admin', 'admin123', 'ACTIVE'),
('2', 'user1', 'user123', 'ACTIVE'),
('3', 'user2', 'user123', 'INACTIVE')
ON DUPLICATE KEY UPDATE username=username; 