# 用户管理系统 CRUD 功能

## 项目结构

```
src/main/java/org/example/springai/
├── model/
│   └── User.java                    # 用户实体类
├── mapper/
│   └── UserMapper.java              # MyBatis Mapper接口
├── service/
│   ├── UserService.java             # 服务接口
│   └── impl/
│       └── UserServiceImpl.java     # 服务实现类
└── controller/
    └── UserController.java          # REST控制器

src/main/resources/
├── mapper/
│   └── UserMapper.xml               # MyBatis XML映射文件
├── sql/
│   └── init.sql                     # 数据库初始化脚本
└── application.properties           # 应用配置文件
```

## 功能特性

### 1. 用户实体类 (User.java)
- 使用Lombok注解简化代码
- 包含用户ID、用户名、密码、状态字段
- 支持自动生成getter/setter方法

### 2. 数据访问层 (UserMapper)
- 基于MyBatis框架
- 支持增删改查操作
- 包含用户名唯一性检查

### 3. 业务逻辑层 (UserService)
- 事务管理
- 业务规则验证
- 异常处理
- 日志记录

### 4. 控制器层 (UserController)
- RESTful API设计
- 统一响应格式
- 跨域支持
- 异常处理

## API接口

### 1. 创建用户
```
POST /api/users
Content-Type: application/json

{
    "username": "newuser",
    "password": "password123",
    "status": "ACTIVE"
}
```

### 2. 查询所有用户
```
GET /api/users
```

### 3. 根据用户ID查询用户
```
GET /api/users/{userId}
```

### 4. 根据用户名查询用户
```
GET /api/users/username/{username}
```

### 5. 根据状态查询用户
```
GET /api/users/status/{status}
```

### 6. 更新用户
```
PUT /api/users/{userId}
Content-Type: application/json

{
    "username": "updateduser",
    "password": "newpassword",
    "status": "ACTIVE"
}
```

### 7. 删除用户
```
DELETE /api/users/{userId}
```

## 响应格式

所有API都返回统一的JSON格式：

```json
{
    "success": true,
    "message": "操作成功",
    "data": {
        // 具体数据
    }
}
```

## 数据库配置

### 1. 创建数据库
执行 `src/main/resources/sql/init.sql` 脚本创建数据库和表。

### 2. 配置数据库连接
在 `application.properties` 中修改数据库连接信息：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/springai
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## 启动项目

1. 确保MySQL服务已启动
2. 执行数据库初始化脚本
3. 修改数据库连接配置
4. 运行Spring Boot应用

```bash
mvn spring-boot:run
```

## 测试示例

### 创建用户
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpass",
    "status": "ACTIVE"
  }'
```

### 查询所有用户
```bash
curl http://localhost:8080/api/users
```

### 更新用户
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "updateduser",
    "password": "newpass",
    "status": "ACTIVE"
  }'
```

### 删除用户
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

## 注意事项

1. 用户名具有唯一性约束
2. 用户ID会自动生成（如果未提供）
3. 默认状态为"ACTIVE"
4. 所有操作都有完整的异常处理和日志记录
5. 支持事务管理，确保数据一致性 