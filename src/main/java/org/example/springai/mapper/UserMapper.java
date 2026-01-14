package org.example.springai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springai.model.User;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 插入用户
     * @param user 用户信息
     * @return 影响行数
     */
    int insertUser(User user);
    
    /**
     * 根据用户ID删除用户
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteUserById(@Param("userId") String userId);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 影响行数
     */
    int updateUser(User user);
    
    /**
     * 根据用户ID查询用户
     * @param userId 用户ID
     * @return 用户信息
     */
    User selectUserById(@Param("userId") String userId);
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User selectUserByUsername(@Param("username") String username);
    
    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> selectAllUsers();
    
    /**
     * 根据状态查询用户
     * @param status 状态
     * @return 用户列表
     */
    List<User> selectUsersByStatus(@Param("status") String status);
} 