# 🚨 个性化应急预案生成器 v2.0
    2
    3 > AI × 实时地图 × 天气 — 精准到时间地点的应急行动指南
    4
    5 [![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk)](https://openjdk.org/projects/jdk/17/)
    6 [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-6DB33F?logo=springboot)](https://spring.io/pro
      jects/spring-boot)
    7 [![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker)](https://www.docker.com/)
    8 [![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
    9
   10 ---
   11
   12 ## 📖 项目简介
   13
   14 在紧急情况下，每一秒都至关重要。个性化应急预案生成器结合 **DeepSeek 大模型**、**高德地图实时数据**和**天气信息*
      *，根据你输入的场景、位置、周边设施，自动生成一份分步骤、可操作的应急行动预案。
   15
   16 无论是宿舍火灾、高层地震、城市内涝还是野外迷路——输入场景，AI 会告诉你**第一步该做什么、往哪跑、附近哪有避难所/
      医院/消防站、千万别做什么**。
   17
   18 ---
   19
   20 ## ✨ 核心功能
   21
   22 - 🗺️ **交互式地图选点** — 点击地图或搜索地址，精确定位事发位置
   23 - 🌤️ **实时天气 + 灾害预警** — 接入高德天气 API，预案自动考虑天气因素
   24 - 🏥 **周边安全设施搜索** — 自动搜索 3 公里内的避难所、医院、消防站、派出所
   25 - 🤖 **DeepSeek AI 深度分析** — 模拟 20 年救援经验的应急专家，生成结构化预案
   26 - 📋 **分阶段行动指南** — 第一时间行动（前 30 秒） → 分阶段步骤 → 禁止事项 → 事后处理
   27 - 📞 **紧急联系电话** — 根据场景智能推荐相关机构电话
   28 - 📱 **响应式设计** — 手机/平板/桌面全适配
   29
   30 ---
   31
   32 ## 🖼️ 截图（占位，可选替换）
   33
   34 <!-- 可以替换为你的实际截图 -->
   35 <!-- ![screenshot](docs/screenshot.png) -->
   36
   37 ---
   38
   39 ## 🛠️ 技术栈
   40
   41 | 层级 | 技术 |
   42 |------|------|
   43 | **后端框架** | Spring Boot 3.5.0 (Java 17) |
   44 | **构建工具** | Maven |
   45 | **AI 引擎** | DeepSeek API (`deepseek-chat`) |
   46 | **地图服务** | 高德地图 Web API + JS API 2.0 |
   47 | **天气数据** | 高德天气 API / 和风天气 API |
   48 | **HTTP 客户端** | OkHttp 4.12 |
   49 | **前端** | 原生 HTML5 + CSS3 + JavaScript（零框架依赖） |
   50 | **容器化** | Docker 多阶段构建 |
   51
   52 ---
   53
   54 ## 🚀 快速开始
   55
   56 ### 前置条件
   57
   58 - JDK 17+
   59 - Maven 3.6+
   60 - 以下 API Key（均为免费注册获取）：
   61   - [DeepSeek API Key](https://platform.deepseek.com)
   62   - [高德开放平台 Key](https://console.amap.com)（Web服务 + JS API 各一个）
   63   - [和风天气 API Key](https://console.qweather.com)（可选）
   64
   65 ### 1. 克隆项目
   66
   67 ```bash
   68 git clone https://github.com/你的用户名/emergency-planner.git
   69 cd emergency-planner
   70 ```
   71
   72 ### 2. 配置 API Key
   73
   74 编辑 `src/main/resources/application.yml`，填入你的 API Key：
   75
   76 ```yaml
   77 deepseek:
   78   api-key: sk-xxxxxxxx    # DeepSeek API Key
   79
   80 amap:
   81   api-key: xxxxxxxx       # 高德 Web 服务 Key
   82   js-api-key: xxxxxxxx    # 高德 JS API Key
   83
   84 qweather:
   85   api-key: xxxxxxxx       # 和风天气 Key（可选）
   86 ```
   87
   88 > 或者设置环境变量 `DEEPSEEK_API_KEY`、`AMAP_API_KEY`、`AMAP_JS_API_KEY`、`QWEATHER_API_KEY`，程序会自动读取。
   89
   90 ### 3. 启动应用
   91
   92 ```bash
   93 # 方式一：Maven Wrapper（推荐）
   94 ./mvnw spring-boot:run
   95
   96 # 方式二：IDEA 直接运行
   97 # 打开 EmergencyPlannerApplication.java → 右键 Run
   98
   99 # 方式三：Docker
  100 docker build -t emergency-planner .
  101 docker run -p 8080:8080 emergency-planner
  102 ```
  103
  104 ### 4. 打开浏览器
  105
  106 ```
  107 http://localhost:8080
  108 ```
  109
  110 ---
  111
  112 ## ☁️ 云端部署
  113
  114 项目已内置 `Dockerfile`，可一键部署到任意支持 Docker 的平台。
  115
  116 ### Render（推荐，国外免费）
  117
  118 1. 推送代码到 GitHub
  119 2. 在 [render.com](https://render.com) 创建 Web Service，选择仓库
  120 3. 添加环境变量（`DEEPSEEK_API_KEY`、`AMAP_API_KEY`、`AMAP_JS_API_KEY`、`QWEATHER_API_KEY`）
  121 4. Region 选 **Singapore**，点击 Deploy
  122
  123 详细步骤见 [`DEPLOY.md`](DEPLOY.md)。
  124
  125 ### 其他平台
  126
  127 | 平台 | 说明 |
  128 |------|------|
  129 | **Fly.io** | `fly launch` 自动识别 Dockerfile |
  130 | **Railway** | 连接 GitHub 仓库即可 |
  131 | **Koyeb** | 原生支持 Docker 部署 |
  132
  133 ---
  134
  135 ## 📁 项目结构
  136
  137 ```
  138 emergency-planner/
  139 ├── src/main/java/com/emergency/
  140 │   ├── EmergencyPlannerApplication.java  # 启动入口
  141 │   ├── controller/
  142 │   │   ├── PlanController.java           # 预案生成 API
  143 │   │   ├── MapController.java            # 地图相关 API
  144 │   │   └── WeatherController.java        # 天气查询 API
  145 │   ├── service/
  146 │   │   ├── DeepSeekService.java          # DeepSeek AI 调用
  147 │   │   ├── AmapService.java              # 高德地图 API 代理
  148 │   │   └── QWeatherService.java          # 和风天气 API 代理
  149 │   └── model/
  150 │       ├── PlanRequest.java              # 请求模型
  151 │       ├── PlanResponse.java             # 预案结果模型
  152 │       ├── GeoInfo.java                  # 地理信息模型
  153 │       └── WeatherInfo.java              # 天气信息模型
  154 ├── src/main/resources/
  155 │   ├── application.yml                   # 应用配置
  156 │   └── static/index.html                 # 前端页面（完整 SPA）
  157 ├── Dockerfile                            # Docker 多阶段构建
  158 ├── DEPLOY.md                             # 部署指南
  159 ├── pom.xml                               # Maven 配置
  160 └── .gitignore
  161 ```
  162
  163 ---
  164
  165 ## 🔌 API 接口
  166
  167 | 方法 | 路径 | 说明 |
  168 |------|------|------|
  169 | `POST` | `/api/generate-plan` | 生成应急预案（核心接口） |
  170 | `GET` | `/api/map/geocode?address=xxx` | 地址 → 坐标 |
  171 | `GET` | `/api/map/regeocode?lng=xxx&lat=xxx` | 坐标 → 地址 |
  172 | `GET` | `/api/map/nearby?lng=xxx&lat=xxx` | 周边安全设施搜索 |
  173 | `GET` | `/api/map/city` | IP 定位获取城市 |
  174 | `GET` | `/api/weather/now?city=xxx` | 实时天气 + 预警 |
  175 | `GET` | `/api/config` | 获取前端 JS API Key |
  176 | `GET` | `/api/health` | 健康检查 |
  175 | `GET` | `/api/config` | 获取前端 JS API Key |
  176 | `GET` | `/api/health` | 健康检查 |
