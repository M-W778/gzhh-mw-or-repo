# 后端服务说明

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Security + JWT
- MyBatis + MyBatis-Plus
- MySQL

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
│  └─ application.yml
├─ 数据库初始化脚本/
│  ├─ db.sql
│  └─ data.sql
└─ pom.xml
```

说明：`src/main/resources` 不再维护运行期 SQL，数据库统一以 `数据库初始化脚本/db.sql` 为准。

## 启动前准备

1. 安装 JDK 17+、MySQL 8+。
2. 执行数据库脚本：
   - `数据库初始化脚本/db.sql`
   - `数据库初始化脚本/data.sql`
3. 按本地环境修改 `src/main/resources/application.yml` 数据库连接。

## 本地启动

```bash
cd backend
mvn spring-boot:run
```

## 接口文档

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`

