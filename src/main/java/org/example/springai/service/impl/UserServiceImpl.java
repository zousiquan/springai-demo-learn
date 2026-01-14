package org.example.springai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springai.mapper.UserMapper;
import org.example.springai.model.User;
import org.example.springai.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public boolean createUser(User user) {
        try {
            // 生成用户ID
            if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
                user.setUserId(UUID.randomUUID().toString().replace("-", ""));
            }
            
            // 检查用户名是否已存在
            User existingUser = userMapper.selectUserByUsername(user.getUsername());
            if (existingUser != null) {
                log.warn("用户名已存在: {}", user.getUsername());
                return false;
            }
            
            // 设置默认状态
            if (user.getStatus() == null || user.getStatus().trim().isEmpty()) {
                user.setStatus("ACTIVE");
            }
            
            int result = userMapper.insertUser(user);
            log.info("创建用户成功: {}", user.getUserId());
            return result > 0;
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteUser(String userId) {
        try {
            // 检查用户是否存在
            User existingUser = userMapper.selectUserById(userId);
            if (existingUser == null) {
                log.warn("用户不存在: {}", userId);
                return false;
            }
            
            int result = userMapper.deleteUserById(userId);
            log.info("删除用户成功: {}", userId);
            return result > 0;
        } catch (Exception e) {
            log.error("删除用户失败: {}", userId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateUser(User user) {
        try {
            // 检查用户是否存在
            User existingUser = userMapper.selectUserById(user.getUserId());
            if (existingUser == null) {
                log.warn("用户不存在: {}", user.getUserId());
                return false;
            }
            
            // 检查用户名是否被其他用户使用
            User userWithSameUsername = userMapper.selectUserByUsername(user.getUsername());
            if (userWithSameUsername != null && !userWithSameUsername.getUserId().equals(user.getUserId())) {
                log.warn("用户名已被其他用户使用: {}", user.getUsername());
                return false;
            }
            
            int result = userMapper.updateUser(user);
            log.info("更新用户成功: {}", user.getUserId());
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户失败: {}", user.getUserId(), e);
            return false;
        }
    }
    
    @Override
    public User getUserById(String userId) {
        try {
            return userMapper.selectUserById(userId);
        } catch (Exception e) {
            log.error("查询用户失败: {}", userId, e);
            return null;
        }
    }
    
    @Override
    public User getUserByUsername(String username) {
        try {
            return userMapper.selectUserByUsername(username);
        } catch (Exception e) {
            log.error("根据用户名查询用户失败: {}", username, e);
            return null;
        }
    }
    
    @Override
    public List<User> getAllUsers() {
        try {
            return userMapper.selectAllUsers();
        } catch (Exception e) {
            log.error("查询所有用户失败", e);
            return List.of();
        }
    }
    
    @Override
    public List<User> getUsersByStatus(String status) {
        try {
            return userMapper.selectUsersByStatus(status);
        } catch (Exception e) {
            log.error("根据状态查询用户失败: {}", status, e);
            return List.of();
        }
    }
} 