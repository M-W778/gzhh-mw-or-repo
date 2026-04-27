# 在线预约挂号系统 API 文档

> 更新时间：2026-04-27  
> 基准来源：`controller` 实现 + `WebSecurityConfig` + 运行中的 Swagger（`http://localhost:8080/v3/api-docs`）

## 1. 基础信息

- 服务地址：`http://localhost:8080`
- API 基础前缀：`/api`
- Swagger UI：`http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- 认证方式：`Authorization: Bearer <JWT>`
- Content-Type：`application/json`
- 统一返回结构：`BaseResponse<T>`

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

## 2. 鉴权与权限规则

| 标记 | 说明 |
| --- | --- |
| `PUBLIC` | 无需登录即可访问 |
| `PATIENT` | 需 `ROLE_PATIENT` |
| `DOCTOR` | 需 `ROLE_DOCTOR` |
| `ADMIN` | 需 `ROLE_ADMIN` |

权限要点（来自 `WebSecurityConfig`）：

- `/api/user/login`、`/api/user/register`、`/api/user/has-username`：公开。
- `/api/admin/**`：管理员。
- `/api/patient/**`、`/api/appointment/**`：患者。
- `/api/doctor/profile`、`/api/doctor/statistics`、`/api/doctor/appointments/**`：医生。
- `GET /api/department/**`、`GET /api/doctor/**`、`GET /api/schedule/**`、`GET /api/notice/**`、`GET /api/statistic/today`：公开。

## 3. 完整接口列表（共 47 个）

### 3.1 用户与认证（4）

| 方法 | 路径 | 权限 | 关键参数 | 说明 |
| --- | --- | --- | --- | --- |
| POST | `/api/user/register` | PUBLIC | body: `username/password/checkPassword/phone/email` | 用户注册（默认 PATIENT） |
| POST | `/api/user/login` | PUBLIC | body: `username/password` | 用户登录，返回 JWT |
| GET | `/api/user/has-username` | PUBLIC | query: `username` | 检查用户名是否存在 |
| POST | `/api/admin/user/create` | ADMIN | body: `username/password/phone/email/role/doctorId?` | 管理员创建 ADMIN/DOCTOR 账号 |

### 3.2 科室（3）

| 方法 | 路径 | 权限 | 关键参数 | 说明 |
| --- | --- | --- | --- | --- |
| GET | `/api/department` | PUBLIC | 无 | 查询启用科室列表 |
| GET | `/api/department/{id}` | PUBLIC | path: `id` | 科室详情 |
| GET | `/api/department/search` | PUBLIC | query: `keyword?`、`deptName?` | 按关键字搜索科室 |

### 3.3 医生公共查询（5）

| 方法 | 路径 | 权限 | 关键参数 | 说明 |
| --- | --- | --- | --- | --- |
| GET | `/api/doctor` | PUBLIC | 无 | 查询启用医生列表 |
| GET | `/api/doctor/{id}` | PUBLIC | path: `id` | 医生基础信息 |
| GET | `/api/doctor/department/{departmentId}` | PUBLIC | path: `departmentId` | 指定科室医生列表 |
| GET | `/api/doctor/search` | PUBLIC | query: `keyword?`、`doctorName?` | 按关键字搜索医生 |
| GET | `/api/doctor/detail/{id}` | PUBLIC | path: `id` | 医生详情（含 `titleText`、`department`） |

### 3.4 排班（8）

| 方法 | 路径 | 权限 | 关键参数 | 说明 |
| --- | --- | --- | --- | --- |
| GET | `/api/schedule/{doctorId}` | PUBLIC | path: `doctorId` | 医生未来 7 天排班 |
| GET | `/api/schedule/future/{doctorId}` | PUBLIC | path: `doctorId` | 医生未来全部排班 |
| GET | `/api/schedule/detail/{scheduleId}` | PUBLIC | path: `scheduleId` | 排班详情 |
| POST | `/api/admin/schedule` | ADMIN | body: `doctorId/workDate/workPeriod/totalNum` | 管理端创建排班 |
| POST | `/api/admin/schedule/{scheduleId}/decrease` | ADMIN | path: `scheduleId` | 余号减 1 |
| POST | `/api/admin/schedule/{scheduleId}/increase` | ADMIN | path: `scheduleId` | 余号加 1 |
| PUT | `/api/admin/schedule/{scheduleId}` | ADMIN | path: `scheduleId` + body: `totalNum/status` | 更新排班总号源/状态 |
| DELETE | `/api/admin/schedule/{scheduleId}` | ADMIN | path: `scheduleId` | 删除排班 |

### 3.5 就诊人（7）

| 方法 | 路径 | 权限 | 关键参数 | 说明 |
| --- | --- | --- | --- | --- |
| GET | `/api/patient/query` | PATIENT | 无 | 当前用户就诊人列表 |
| GET | `/api/patient/query/ids` | PATIENT | query: `ids`（可多值） | 批量查询就诊人 |
| GET | `/api/patient/{id}` | PATIENT | path: `id` | 就诊人详情（含归属校验） |
| POST | `/api/patient/save` | PATIENT | body: `PatientReqDTO` | 新增就诊人 |
| PUT | `/api/patient/update/{id}` | PATIENT | path: `id` + body: `PatientReqDTO` | 更新就诊人 |
| DELETE | `/api/patient/delete/{id}` | PATIENT | path: `id`（兼容 query: `id`） | 删除就诊人 |
| GET | `/api/patient/default` | PATIENT | 无 | 查询默认就诊人 |

### 3.6 患者预约（6）

| 方法 | 路径 | 权限 | 关键参数 | 说明 |
| --- | --- | --- | --- | --- |
| GET | `/api/appointment` | PATIENT | 无 | 我的预约列表 |
| GET | `/api/appointment/{id}` | PATIENT | path: `id` | 预约详情（含归属校验） |
| POST | `/api/appointment` | PATIENT | body: `doctorId/scheduleId/patientId/remarks?` | 提交预约 |
| POST | `/api/appointment/{id}/cancel` | PATIENT | path: `id` + body: `reason` | 取消预约 |
| GET | `/api/appointment/{id}/can-cancel` | PATIENT | path: `id` | 查询是否可取消 |
| POST | `/api/appointment/{id}/notify` | PATIENT | path: `id` | 标记通知已发送 |

### 3.7 医生工作台（6）

| 方法 | 路径 | 权限 | 关键参数 | 说明 |
| --- | --- | --- | --- | --- |
| GET | `/api/doctor/profile` | DOCTOR | 无 | 当前医生档案 |
| GET | `/api/doctor/appointments` | DOCTOR | 无 | 医生全部预约 |
| GET | `/api/doctor/appointments/today` | DOCTOR | 无 | 医生今日预约 |
| GET | `/api/doctor/appointments/status/{status}` | DOCTOR | path: `status` | 按状态查询预约 |
| GET | `/api/doctor/statistics` | DOCTOR | 无 | 医生预约统计 |
| POST | `/api/doctor/appointments/{id}/complete` | DOCTOR | path: `id` | 预约置为已完成 |

### 3.8 公告（6）

| 方法 | 路径 | 权限 | 关键参数 | 说明 |
| --- | --- | --- | --- | --- |
| GET | `/api/notice/page` | PUBLIC | query: `page=1`、`size=6` | 前台公告分页 |
| GET | `/api/admin/notice/page` | ADMIN | query: `page=1`、`size=10` | 管理端公告分页 |
| GET | `/api/admin/notice/{id}` | ADMIN | path: `id` | 公告详情 |
| POST | `/api/admin/notice` | ADMIN | body: `AdminNoticeSaveReqDTO` | 新建公告 |
| PUT | `/api/admin/notice/{id}` | ADMIN | path: `id` + body: `AdminNoticeSaveReqDTO` | 更新公告 |
| DELETE | `/api/admin/notice/{id}` | ADMIN | path: `id` | 删除公告 |

### 3.9 统计（2）

| 方法 | 路径 | 权限 | 关键参数 | 说明 |
| --- | --- | --- | --- | --- |
| GET | `/api/statistic/today` | PUBLIC | 无 | 今日预约统计（公开） |
| GET | `/api/statistic/appointments` | ADMIN | 无 | 管理端预约统计 |

## 4. 请求/响应示例（必选接口）

> 以下示例字段按当前代码结构整理，示例值为演示用途。

### 4.1 登录

`POST /api/user/login`

请求：

```json
{
  "username": "patient01",
  "password": "123456"
}
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "userId": "12",
    "id": "12",
    "username": "patient01",
    "realName": "patient01",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.xxx",
    "token": "eyJhbGciOiJIUzI1NiJ9.xxx",
    "phone": "13800000000",
    "roles": ["ROLE_PATIENT"]
  }
}
```

### 4.2 科室列表

`GET /api/department`

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "id": 1,
      "name": "内科",
      "description": "内科门诊",
      "iconUrl": null,
      "sortOrder": 1,
      "createTime": "2026-04-20T10:00:00",
      "updateTime": "2026-04-20T10:00:00",
      "delFlag": 0
    }
  ]
}
```

### 4.3 医生排班查询

`GET /api/schedule/3`

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "id": 101,
      "doctorId": 3,
      "workDate": "2026-04-28",
      "workPeriod": "MORNING",
      "workPeriodText": "上午",
      "totalNum": 20,
      "remainingNum": 7,
      "status": "AVAILABLE",
      "statusText": "可预约"
    }
  ]
}
```

### 4.4 提交预约

`POST /api/appointment`

请求头：

```text
Authorization: Bearer <token>
```

请求：

```json
{
  "doctorId": 3,
  "scheduleId": 101,
  "patientId": 18,
  "remarks": "复诊挂号"
}
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 9001,
    "appointmentNo": "AP20260427A1B2C3D4",
    "patientName": "张三",
    "patientIdCard": "310101199001010011",
    "doctorName": "李医生",
    "doctorTitle": "CHIEF",
    "departmentName": "内科",
    "appointmentDate": "2026-04-28",
    "timeSlot": "MORNING",
    "timeSlotText": "上午",
    "status": "PENDING",
    "statusText": "待就诊",
    "queueNumber": 5,
    "remarks": "复诊挂号",
    "createdAt": "2026-04-27T20:15:30",
    "cancelledAt": null,
    "cancelReason": null,
    "canCancel": true,
    "notificationSent": false,
    "notices": [
      "Please arrive 15 minutes before your appointment time.",
      "Bring your ID card and related medical records.",
      "If you need to cancel, please do it within 30 minutes after booking."
    ],
    "successNotice": "appointment created successfully"
  }
}
```

### 4.5 预约记录查询

`GET /api/appointment`

请求头：

```text
Authorization: Bearer <token>
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "id": 9001,
      "appointmentNo": "AP20260427A1B2C3D4",
      "patientName": "张三",
      "patientIdCard": "310101199001010011",
      "doctorName": "李医生",
      "doctorTitle": "CHIEF",
      "departmentName": "内科",
      "appointmentDate": "2026-04-28",
      "timeSlot": "MORNING",
      "timeSlotText": "上午",
      "status": "PENDING",
      "statusText": "待就诊",
      "queueNumber": 5,
      "remarks": "复诊挂号",
      "createdAt": "2026-04-27T20:15:30",
      "cancelledAt": null,
      "cancelReason": null,
      "canCancel": true,
      "notificationSent": false,
      "notices": [
        "Please arrive 15 minutes before your appointment time.",
        "Bring your ID card and related medical records.",
        "If you need to cancel, please do it within 30 minutes after booking."
      ]
    }
  ]
}
```

## 5. 状态码说明

### 5.1 业务码（`BaseResponse.code`）

| code | 含义 | 对应枚举 |
| --- | --- | --- |
| `0` | 成功 | `SUCCESS` |
| `40000` | 参数错误 | `PARAMS_ERROR` |
| `40100` | 未登录/认证失败 | `NOT_LOGIN_ERROR` |
| `40101` | 无权限 | `NO_AUTH_ERROR` |
| `40300` | 禁止访问 | `FORBIDDEN_ERROR` |
| `40400` | 资源不存在 | `NOT_FOUND_ERROR` |
| `50000` | 系统错误 | `SYSTEM_ERROR` |
| `50001` | 业务操作失败 | `OPERATION_ERROR` |

### 5.2 HTTP 状态码（当前项目实际行为）

| HTTP 状态 | 场景 |
| --- | --- |
| `200` | 接口正常返回；以及大部分业务异常（通过 `code != 0` 表达） |
| `403` | 未携带/无效 token 访问受保护接口，或角色权限不足（Spring Security 拦截） |
| `500` | 容器级未处理异常（理论上较少） |

说明：

- 例如 `GET /api/schedule/0` 会返回 HTTP `200`，响应体 `{"code":40000,...}`。
- 例如未登录访问 `GET /api/appointment` 会返回 HTTP `403`。

## 6. 错误响应示例

业务错误示例（号源不足）：

```json
{
  "code": 50001,
  "message": "no remaining quota",
  "data": null
}
```
