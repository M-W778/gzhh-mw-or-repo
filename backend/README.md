# 后端服务说明

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Security + JWT
- MyBatis + MyBatis-Plus
- MySQL
- Maven

## 目录结构

```text
backend/
├─ src/main/java/com/hospital/registration/
│  ├─ config/
│  ├─ controller/
│  ├─ mapper/
│  ├─ model/
│  ├─ service/
│  └─ exception/
├─ src/main/resources/
│  ├─ application.yml
│  └─ schema.sql
├─ 数据库初始化脚本/
│  ├─ db.sql
│  └─ data.sql
└─ pom.xml
```

## 启动前准备

1. 安装 JDK 17+、Maven 3.8+、MySQL 8+。
2. 执行数据库脚本：
   - `数据库初始化脚本/db.sql`
   - `数据库初始化脚本/data.sql`

## 配置说明

默认配置文件：`src/main/resources/application.yml`

默认数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/or_sys?useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&allowMultiQueries=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
```

请按本地环境调整用户名/密码。

## 本地启动

```bash
cd backend
mvn spring-boot:run
```

或：

```bash
cd backend
mvn clean package -DskipTests
java -jar target/registration-system-0.0.1-SNAPSHOT.jar
```

## 访问地址

- 后端服务：`http://localhost:8080`
- Swagger UI：`http://localhost:8080/swagger-ui/index.html`
- OpenAPI：`http://localhost:8080/v3/api-docs`

## 鉴权说明

需登录接口获取 JWT，受保护接口在请求头携带：

```text
Authorization: Bearer <token>
```

