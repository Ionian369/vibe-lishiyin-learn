# Day 1 反思 · 李诗滢（2026-10-07）

## 反思区（今天指挥 AI 干成了什么 / 坑在哪）

上午用一句话生成静态版器材预约页（T1：三张莫兰蒂卡片、弹窗选日期时段、重复预约当场拒绝，另追加 CSV 导出），下午指挥 AI 把它升级成 Vue3 + SpringBoot + H2 的全栈系统，又追加了「我的预约历史 + 成功次数」。全程零手写代码，靠"提需求 → 实测验收 → 让 AI 解释 diff"循环推进。

坑有四个，按代价排序：① 本机没有 JDK / Maven / Node，工具链装了半天，GitHub 源下载 JDK 还中断过，换镜像才解决——环境先行比写代码更要紧；② H2 开 AUTO_SERVER 后 `data.sql` 不执行，器材列表一直是空的，要配 `spring.sql.init.mode: always`；③ Windows 细节：PowerShell 执行策略拦截 npm、`run-*.cmd` 中文乱码必须 GBK + CRLF；④ 「需求里每个词都会被当真」——说"恰好三个功能、莫兰蒂粉紫"，AI 就真的严格照办，措辞即需求。

## 反思表格（实验手册格式）

| 我让 AI 做了什么 | 我怎么验收的 | 返工次数与原因 |
|---|---|---|
| T1 静态版：器材预约单页（卡片/弹窗/重复拒绝）+ 自选功能 CSV 导出 | 本地服务器浏览器实测：预约成功 → 计数变化 → 重复被拒 → 导出 Excel 打开不乱码 | 0 次，一次通过 |
| 全栈骨架：SpringBoot 后端 + Vue3 前端 + H2（各一条命令启动） | curl `/api/ping` 返回 ok；前端页显示后端连接状态标签 | 2 次：JDK 下载中断（换镜像）；npm 被 PowerShell 执行策略拦截 |
| 预约与三层冲突校验（Service 判重 + 唯一索引 + 全局异常） | 浏览器同一器材同一时段连提两次，第二次 409 红字提示到具体时段；curl 复测同结果 | 1 次：data.sql 不生效（H2 AUTO_SERVER 需 `sql.init.mode: always`） |
| 自选功能（全栈版）：我的预约历史 + 成功次数 | curl `GET /api/reservations?user=李诗滢`；页面保存姓名后表格与计数联动刷新 | 1 次：cmd 启动脚本中文乱码，改 GBK + CRLF 编码 |
| 收工四步（反思件 / 署名 commit / day1 tag / 自查） | `python 交作业自查.py --day 1` 四盏灯全绿 | — |

## T1 验收：追加自选功能改了哪些文件（AI 改动前后差异）

以全栈版自选功能「我的预约历史 + 成功次数」为例（上午静态版 CSV 导出的 diff 已在当日上午对话中复述过）：

后端三处：
1. `ReservationController.java`：新增 `GET /api/reservations?user=`（历史列表）与 `GET /api/reservations/stats?user=`（成功次数）两个接口
2. `ReservationService.java`：新增 `listByUser()`（按提交时间倒序、联查器材名）与 `countByUser()`
3. `ReservationRepository.java`：新增 `findByUserNameOrderByCreatedAtDesc` 与 `countByUserName` 两个查询方法

前端两处：
4. `api/reservation.js`：新增 `fetchMyReservations` / `fetchStats` 两个接口封装
5. `views/ReservationView.vue`：新增「我的预约历史」卡片（器材/日期/时段/状态/提交时间五列表格 + 空态提示），保存姓名、预约成功时联动刷新

改动前后差异：改前预约提交完"约没约上全靠记"；改后姓名一存，历史与成功次数实时可见，状态自动区分「待使用 / 已结束」——功能从"能约"延伸到"可查可对账"。

## 对话档案

### 档案一：T1 静态版（上午）

- **我的指令**：新建空文件夹 vibe-lab1，做一个教务器材预约页面：三张器材卡片，点「预约」选日期和时段提交，同一器材同一时段重复预约要被拒绝，样式简洁，莫兰蒂粉紫、清新古典。
- **AI 的关键动作**：单文件 HTML/CSS/JS；已约满时段禁用并红字提示；数据存 localStorage；本地服务器浏览器实测全链路。随后按 T1 要求追加 CSV 导出（BOM 头防 Excel 乱码），diff 三处可复述。
- **结果**：五步浏览器实测全绿。

### 档案二：全栈骨架（下午）

- **我的指令**：把项目升级为前后端分离：Vue3 + Element Plus 前端、SpringBoot 后端、H2 数据库，两条命令分别启动，先不做业务功能。
- **AI 的关键动作**：装 JDK / Maven / Node 工具链（配阿里云 Maven 镜像）；生成后端骨架（启动类、ping 健康检查、H2 配置）；前端 Vite + Vue3 + Element Plus + axios，`/api` 代理穿透；写 run-backend.cmd / run-frontend.cmd。
- **结果**：curl 健康检查通过，前端页能显示后端连接状态。返工 2 次（JDK 下载中断、npm 执行策略拦截）。

### 档案三：预约与冲突校验实测

- **我的指令**：实现预约提交与冲突拒绝，同一器材同一时段重复预约必须被拒绝，并告诉我为什么。
- **AI 的关键动作**：三层防护（Service 前置查询 → 数据库 `UNIQUE(device_id, date, period)` 唯一索引兜底并发 → 全局异常处理器返回 409 + 具体原因文案）；前端弹窗内用 el-alert 持久展示冲突原因而非只闪一下 toast。修复 data.sql 不生效问题（AUTO_SERVER 模式需 `sql.init.mode: always`）。
- **结果**：浏览器两次提交同一时段，第二次 409 红字「投影仪在 …… 上午 已被预约，请更换时段」，curl 复测一致。

### 档案四：我的预约历史（全栈版自选功能）

- **我的指令**：加「我的预约历史」：填姓名保存后能看到自己的预约列表和成功次数，预约成功后自动刷新。
- **AI 的关键动作**：见上方「T1 验收」——后端两个接口 + 前端表格联动，diff 五处文件可复述。顺手修复了启动脚本中文乱码（GBK + CRLF）。
- **结果**：curl 与浏览器双通道实测通过。

### 档案五：收工四步

- **我的指令**：完成每日收工四步；仓库里旧的静态版内容清空，换成今天的全栈版。
- **AI 的关键动作**：重写 README（三行署名 + 项目说明）与英文版；按 T3 结构写《想法电梯稿.md》并把 pitch.html 单页站内容对齐；补本反思件；署名 commit → 推送 → 重新打 day1 tag → 跑自查脚本。
- **结果**：见本次 commit、day1 tag 与自查输出。
