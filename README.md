# 🚨 个性化应急预案生成器 v2.0

> AI × 实时地图 × 天气 — 精准到时间地点的应急行动指南

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

---

## 📖 项目简介

在紧急情况下，每一秒都至关重要。个性化应急预案生成器结合 **DeepSeek 大模型**、**高德地图实时数据**和**天气信息**，根据你输入的场景、位置、周边设施，自动生成一份分步骤、可操作的应急行动预案。

无论是宿舍火灾、高层地震、城市内涝还是野外迷路——输入场景，AI 会告诉你**第一步该做什么、往哪跑、附近哪有避难所/医院/消防站、千万别做什么**。

---

## ✨ 核心功能

- 🗺️ **交互式地图选点** — 点击地图或搜索地址，精确定位事发位置
- 🌤️ **实时天气 + 灾害预警** — 接入高德天气 API，预案自动考虑天气因素
- 🏥 **周边安全设施搜索** — 自动搜索 3 公里内的避难所、医院、消防站、派出所
- 🤖 **DeepSeek AI 深度分析** — 模拟 20 年救援经验的应急专家，生成结构化预案
- 📋 **分阶段行动指南** — 第一时间行动（前 30 秒） → 分阶段步骤 → 禁止事项 → 事后处理
- 📞 **紧急联系电话** — 根据场景智能推荐相关机构电话
- 📱 **响应式设计** — 手机/平板/桌面全适配

---

## 🖼️ 截图（占位，可选替换）

<!-- 可以替换为你的实际截图 -->
<!-- ![screenshot](docs/screenshot.png) -->

---

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| **后端框架** | Spring Boot 3.5.0 (Java 17) |
| **构建工具** | Maven |
| **AI 引擎** | DeepSeek API (`deepseek-chat`) |
| **地图服务** | 高德地图 Web API + JS API 2.0 |
| **天气数据** | 高德天气 API / 和风天气 API |
| **HTTP 客户端** | OkHttp 4.12 |
| **前端** | 原生 HTML5 + CSS3 + JavaScript（零框架依赖） |
| **容器化** | Docker 多阶段构建 |

---

## 🚀 快速开始

### 前置条件

- JDK 17+
- Maven 3.6+
- 以下 API Key（均为免费注册获取）：
  - [DeepSeek API Key](https://platform.deepseek.com)
  - [高德开放平台 Key](https://console.amap.com)（Web服务 + JS API 各一个）
  - [和风天气 API Key](https://console.qweather.com)（可选）

### 1. 克隆项目

```bash
git clone https://github.com/你的用户名/emergency-planner.git
cd emergency-planner
```

### 2. 配置 API Key

编辑 `src/main/resources/application.yml`，填入你的 API Key：

```yaml
deepseek:
  api-key: sk-xxxxxxxx    # DeepSeek API Key

amap:
  api-key: xxxxxxxx       # 高德 Web 服务 Key
  js-api-key: xxxxxxxx    # 高德 JS API Key

qweather:
  api-key: xxxxxxxx       # 和风天气 Key（可选）
```

> 或者设置环境变量 `DEEPSEEK_API_KEY`、`AMAP_API_KEY`、`AMAP_JS_API_KEY`、`QWEATHER_API_KEY`，程序会自动读取。

### 3. 启动应用

```bash
# 方式一：Maven Wrapper（推荐）
./mvnw spring-boot:run

# 方式二：IDEA 直接运行
# 打开 EmergencyPlannerApplication.java → 右键 Run

# 方式三：Docker
docker build -t emergency-planner .
docker run -p 8080:8080 emergency-planner
```

### 4. 打开浏览器

```
http://localhost:8080
```

---

## ☁️ 云端部署

项目已内置 `Dockerfile`，可一键部署到任意支持 Docker 的平台。

### Render（推荐，国外免费）

1. 推送代码到 GitHub
2. 在 [render.com](https://render.com) 创建 Web Service，选择仓库
3. 添加环境变量（`DEEPSEEK_API_KEY`、`AMAP_API_KEY`、`AMAP_JS_API_KEY`、`QWEATHER_API_KEY`）
4. Region 选 **Singapore**，点击 Deploy

详细步骤见 [`DEPLOY.md`](DEPLOY.md)。

### 其他平台

| 平台 | 说明 |
|------|------|
| **Fly.io** | `fly launch` 自动识别 Dockerfile |
| **Railway** | 连接 GitHub 仓库即可 |
| **Koyeb** | 原生支持 Docker 部署 |

---

## 📁 项目结构

```
emergency-planner/
├── src/main/java/com/emergency/
│   ├── EmergencyPlannerApplication.java  # 启动入口
│   ├── controller/
│   │   ├── PlanController.java           # 预案生成 API
│   │   ├── MapController.java            # 地图相关 API
│   │   └── WeatherController.java        # 天气查询 API
│   ├── service/
│   │   ├── DeepSeekService.java          # DeepSeek AI 调用
│   │   ├── AmapService.java              # 高德地图 API 代理
│   │   └── QWeatherService.java          # 和风天气 API 代理
│   └── model/
│       ├── PlanRequest.java              # 请求模型
│       ├── PlanResponse.java             # 预案结果模型
│       ├── GeoInfo.java                  # 地理信息模型
│       └── WeatherInfo.java              # 天气信息模型
├── src/main/resources/
│   ├── application.yml                   # 应用配置
│   └── static/index.html                 # 前端页面（完整 SPA）
├── Dockerfile                            # Docker 多阶段构建
├── DEPLOY.md                             # 部署指南
├── pom.xml                               # Maven 配置
└── .gitignore
```

---

## 🔌 API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/generate-plan` | 生成应急预案（核心接口） |
| `GET` | `/api/map/geocode?address=xxx` | 地址 → 坐标 |
| `GET` | `/api/map/regeocode?lng=xxx&lat=xxx` | 坐标 → 地址 |
| `GET` | `/api/map/nearby?lng=xxx&lat=xxx` | 周边安全设施搜索 |
| `GET` | `/api/map/city` | IP 定位获取城市 |
| `GET` | `/api/weather/now?city=xxx` | 实时天气 + 预警 |
| `GET` | `/api/config` | 获取前端 JS API Key |
| `GET` | `/api/health` | 健康检查 |

---

## ⚠️ 免责声明

**本工具生成的预案仅供参考和学习用途。** 真实紧急情况中，请以现场实际情况和专业人员指导为准。开发者不对因使用本工具产生的任何后果承担责任。

---

## 📝 License

MIT License — 可自由使用、修改和分发。

---

## 🙏 致谢

- [DeepSeek](https://deepseek.com) — 强大的 AI 大模型
- [高德开放平台](https://console.amap.com) — 地图与天气 API
- [和风天气](https://www.qweather.com) — 气象数据服务
- [Spring Boot](https://spring.io) — Java 生态基石
