# 前端服务说明

## 技术栈

- Vue 3 + TypeScript
- Vue Router + Pinia
- Element Plus
- Axios
- Vite

## 目录结构

```text
frontend/
├─ src/
│  ├─ api/
│  ├─ router/
│  ├─ stores/
│  ├─ views/
│  ├─ App.vue
│  └─ main.ts
├─ package.json
└─ vite.config.ts
```

## 环境要求

- Node.js 18+
- Yarn 1.22+（或 npm）

## 安装与启动

```bash
cd frontend
yarn install
yarn serve
```

说明：

- `serve` 脚本等价于 Vite 开发模式（`vite`）。
- 也可使用 `yarn dev`。

## 打包

```bash
cd frontend
yarn build
```

打包产物目录：`frontend/dist`

## 默认访问

- 前端地址：`http://localhost:5173`
- 代理目标：`http://localhost:8080`（见 `vite.config.ts`）

## 主要路由

| 路径 | 页面 |
| --- | --- |
| `/` | 首页 |
| `/login` | 登录 |
| `/register` | 注册 |
| `/departments` | 科室列表 |
| `/doctors/:departmentId` | 科室医生列表 |
| `/doctor/:id` | 医生详情 |
| `/appointment/:doctorId` | 预约挂号 |
| `/appointments` | 我的预约 |
| `/patients` | 就诊人管理 |
| `/profile` | 个人中心 |
| `/doctor/portal` | 医生工作台 |
| `/admin/schedules` | 管理员排班管理 |
| `/admin/statistics` | 管理员统计看板 |

