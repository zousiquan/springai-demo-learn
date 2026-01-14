package org.example.springai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springai.model.User;
import org.example.springai.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = userService.createUser(user);
            if (success) {
                response.put("success", true);
                response.put("message", "用户创建成功");
                response.put("data", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户创建失败，可能用户名已存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("创建用户异常", e);
            response.put("success", false);
            response.put("message", "创建用户时发生错误");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = userService.deleteUser(userId);
            if (success) {
                response.put("success", true);
                response.put("message", "用户删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户删除失败，可能用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("删除用户异常: {}", userId, e);
            response.put("success", false);
            response.put("message", "删除用户时发生错误");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String userId, @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            user.setUserId(userId);
            boolean success = userService.updateUser(user);
            if (success) {
                response.put("success", true);
                response.put("message", "用户更新成功");
                response.put("data", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户更新失败，可能用户不存在或用户名冲突");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("更新用户异常: {}", userId, e);
            response.put("success", false);
            response.put("message", "更新用户时发生错误");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据用户ID查询用户
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                response.put("success", true);
                response.put("data", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("查询用户异常: {}", userId, e);
            response.put("success", false);
            response.put("message", "查询用户时发生错误");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserByUsername(username);
            if (user != null) {
                response.put("success", true);
                response.put("data", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据用户名查询用户异常: {}", username, e);
            response.put("success", false);
            response.put("message", "查询用户时发生错误");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 查询所有用户
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<User> users = userService.getAllUsers();
            response.put("success", true);
            response.put("data", users);
            response.put("total", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询所有用户异常", e);
            response.put("success", false);
            response.put("message", "查询用户时发生错误");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据状态查询用户
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getUsersByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<User> users = userService.getUsersByStatus(status);
            response.put("success", true);
            response.put("data", users);
            response.put("total", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("根据状态查询用户异常: {}", status, e);
            response.put("success", false);
            response.put("message", "查询用户时发生错误");
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 