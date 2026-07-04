# 🚀 云端部署指南 — Render（国外免费）

## 📌 为什么选 Render？

- ✅ **免费额度**：750 小时/月（足够个人项目 24/7 运行）
- ✅ **支持 Docker**：直接识别 Dockerfile 自动构建
- ✅ **全球 CDN**：国外访问速度快
- ✅ **自动 HTTPS**：免费 SSL 证书
- ✅ **GitHub 联动**：推送代码自动部署

---

## 第一步：将项目推送到 GitHub

Render 通过 GitHub 仓库部署，所以先把代码上传：

```bash
cd emergency-planner
git init
git add .
git commit -m "初始化应急预案生成器"
```

然后在 [github.com](https://github.com) 创建新仓库，按提示推送：
```bash
git remote add origin https://github.com/你的用户名/emergency-planner.git
git branch -M main
git push -u origin main
```

---

## 第二步：在 Render 创建 Web Service

1. 注册/登录 [render.com](https://render.com)（用 GitHub 账号直接登录最方便）
2. 点击右上角 **「New +」→「Web Service」**
3. 授权 Render 访问你的 GitHub，选择 `emergency-planner` 仓库
4. 配置页面填写以下信息：

| 配置项       | 填写内容                                 |
| ------------ | ---------------------------------------- |
| Name         | `emergency-planner`（随意）              |
| Region       | **Singapore**（离国内最近）或 Oregon     |
| Branch       | `main`                                   |
| Runtime      | **Docker**（会自动识别 Dockerfile）       |
| Instance Type| **Free**                                 |

---

## 第三步：设置环境变量（重要！）

在配置页面的 **Environment Variables** 区域，添加以下 5 个变量：

| 变量名               | 值（你的 API Key）                        |
| -------------------- | ----------------------------------------- |
| `PORT`               | `8080`                                    |
| `DEEPSEEK_API_KEY`   | `sk-a66f405ebd9c4fa2b5ef95274bc7571c`    |
| `AMAP_API_KEY`       | `51d5b9e6d0096a9efc0f0271c628f817`       |
| `AMAP_JS_API_KEY`    | `74fa8d67e886775b49b325fb138dcfa4`       |
| `QWEATHER_API_KEY`   | `4852ec3e98c240a0b7adaf4139e3e99e`       |

> ⚠️ **安全提示**：API Key 放在环境变量中不会被提交到 Git 仓库，比写在配置文件里安全得多。

---

## 第四步：配置高德地图 JS API 白名单

部署成功后 Render 会给你一个域名（如 `https://emergency-planner.onrender.com`），需要把这个域名添加到高德 JS API 的白名单：

1. 登录 [高德开放平台控制台](https://console.amap.com/dev/key/app)
2. 找到你的 JS API Key（`74fa8d67e886775b49b325fb138dcfa4`）
3. 在「安全密钥」或「白名单」中添加你的 Render 域名
4. 保存生效（可能需要几分钟）

---

## 第五步：部署

点击 **「Create Web Service」**，Render 会自动：
1. 拉取 GitHub 代码
2. 识别 Dockerfile 进行多阶段构建（Maven 编译 + JRE 镜像）
3. 启动容器并绑定端口
4. 分配一个 `*.onrender.com` 域名

首次部署约需 5-10 分钟（含 Maven 依赖下载），后续推送代码会自动触发部署。

---

## 🆓 免费层注意事项

| 限制           | 影响                                       |
| -------------- | ------------------------------------------ |
| 750 小时/月    | 够用整整一个月（31天 × 24小时 = 744小时）   |
| 15分钟无请求休眠 | 无人访问时会休眠，下次请求需等 30-60秒唤醒 |
| 512 MB RAM     | Dockerfile 已配置 JVM 内存限制 320MB       |
| 0.1 CPU        | 并发不高但够个人使用                        |

> 💡 **防止休眠**：可以用 [cron-job.org](https://cron-job.org) 等免费服务，每隔 10 分钟访问一次你的网站，保持活跃。

---

## 🌐 备选平台

如果 Render 不满足需求，还可以尝试：

| 平台         | 免费额度              | 特点                   |
| ------------ | --------------------- | ---------------------- |
| **Fly.io**   | 3 个共享 VM           | 性能好，需绑信用卡      |
| **Koyeb**    | 1 个 Web Service      | 支持 Docker，延迟较高   |
| **Railway**  | $5 赠金/月            | 界面友好，赠金用完即停   |

以上三个平台部署方式基本相同——只需提供 Dockerfile，无需额外配置。

---

## 🔧 本地验证 Docker 构建（可选）

推送前可以先在本地验证 Docker 镜像是否正常：

```bash
cd emergency-planner

# 构建镜像
docker build -t emergency-planner .

# 本地运行
docker run -p 8080:8080 \
  -e DEEPSEEK_API_KEY=sk-xxx \
  -e AMAP_API_KEY=xxx \
  -e AMAP_JS_API_KEY=xxx \
  -e QWEATHER_API_KEY=xxx \
  emergency-planner

# 浏览器打开 http://localhost:8080
```
