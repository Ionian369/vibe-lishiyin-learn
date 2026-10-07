# vibe-lishiyin

> **你是谁**：李诗滢
> **讨论组号**：第 9 组
> **这门课要练成什么**：AI 结对独立交付——从一个想法到一个能跑的全栈小系统，每一行 AI 改动自己都能讲清楚。

## 项目：教务器材预约系统（Day1 · 实验一）

让「谁先约到器材」从群里吵架，变成系统里一条 409。同一器材 + 同一日期 + 同一时段，只有一个人能约成功，第二次提交当场拒绝并提示具体冲突原因。

### 核心功能（恰好三个）

1. **浏览**器材卡片（投影仪 / 音响 / 无线话筒）与用途说明
2. **提交**预约并被系统裁定：选日期 + 时段（上午/下午）；同一器材同一时段重复提交返回 409，弹窗持久展示「投影仪在 2026-10-08 上午 已被预约，请更换时段」
3. **查询**我的预约历史：姓名一存即出列表（器材 / 日期 / 时段 / 待使用或已结束 / 提交时间），成功次数实时累计

### 技术栈

| 层 | 选型 | 说明 |
|----|------|------|
| 前端 | Vue3 + Element Plus + Vite | 组件化，`/api` 代理穿透后端 |
| 后端 | SpringBoot 3 + Spring Data JPA | Controller / Service / Repository 分层 |
| 数据 | H2 文件库 | `UNIQUE(device_id, date, period)` 唯一索引兜底并发冲突 |

冲突防护三层：Service 前置查询校验 → 数据库唯一索引 → 全局异常处理器统一返回 409 与原因文案。

### 启动（各一条命令）

```
run-backend.cmd     # 后端 :8080，就绪标志 Started ReservationApplication
run-frontend.cmd    # 前端 :5173，浏览器打开 http://localhost:5173
```

健康检查：`http://localhost:8080/api/ping` 返回 `{"status":"ok"}`。

### 目录结构

```
backend/                 SpringBoot 后端（源码 + pom.xml）
frontend/                Vue3 前端（源码 + vite.config.js）
docs/反思录/李诗滢/      每日反思件（过程档案）
想法电梯稿.md            T3 一页电梯稿（开题）
pitch.html               C1 挑战：电梯稿可访问单页站
交作业自查.py            收工自查脚本（四盏灯）
```

### 每日收工四步

1. 写反思件 `docs/反思录/李诗滢/dayN.md` 并署名 commit
2. `git tag dayN && git push origin dayN`
3. Gitee 仓库发「第 N 天完工报告」issue
4. `python 交作业自查.py --day N` 四盏灯全绿再下班
