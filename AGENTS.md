# AGENTS.md · 教务器材预约系统

> 本文件是 AI 结对编程代理的工作指南：架构、命令、代码约定、已拍板的产品决策与任务清单。
> 改动前先读完本文；与 README / 反思录的历史描述冲突时，以本文的拍板结论为准。
> 每完成一个任务，更新 §6 对应条目状态。
> 需求三件套见 docs/：[PRD](docs/PRD.md)（做什么/给谁/怎么算成功）· [技术方案](docs/技术方案.md)（怎么做）· [任务看板](docs/任务看板.md)（排期与状态）。

---

## 0. 响应纪律（每个会话先读，优先级最高）

1. **改代码前先说计划**：列出要动哪些文件、每处改什么、为什么——我确认后再动手。一次计划只做一件事。
2. **不确定就先问，不要猜**：表结构、已有接口行为、我上一句话的真实意图，任一拿不准就提问，禁止靠猜表结构或臆造接口直接写代码。
3. **只做被要求的事**：不顺手重构、不优化没被点名的代码、不引入新依赖/新文件，除非我明确要求。
4. **破坏性操作先确认**：删文件、删数据、`git push --force`、改数据库、覆盖整文件——必须先列出影响清单等我点头。
5. **每次改动附 diff 说明**：改动后逐文件说明"改了什么、为什么"，让我能复述（这是课程要求：说不出来就等于没做）。
6. **红线即终审**：§7 红线表里标"不做"的需求，即使我口头又提起来，也要先引用红线条目跟我确认例外，再动手。
7. **文案只出自后端**：用户可见的错误文案由后端生成透传，前端不得编造（§4 已定，此处重申因为最常被违反）。

---

## 1. 项目定位

让「谁先约到器材」从群里吵架，变成系统里一条 409。同一器材 + 同一日期 + 同一时段，只有一个人能约成功。

**信任模型（本期红线）**：姓名即身份，无登录无鉴权。姓名存浏览器 localStorage，后端按 `user_name` 精确匹配。任何需要"证明你是你"的需求都属二期（见 §7）。

**资源模型**：单件贵重设备。一个器材 = 一份资源，一时段只能被约走一次，由数据库唯一索引保证。没有"台数"概念，也不打算有（见红线 Q4）。

---

## 2. 架构速览

| 层 | 选型 | 说明 |
|----|------|------|
| 前端 | Vue3 + Element Plus + Vite | `:5173`，`/api` 代理到 `:8080` |
| 后端 | Spring Boot 3 + Spring Data JPA | `:8080`，Controller / Service / Repository 分层 |
| 数据 | H2 文件库 | `jdbc:h2:file:./data/reservation;AUTO_SERVER=TRUE`，落盘重启不丢 |

**数据模型**
- `device(id, name, description)` — 预置三件套：投影仪 / 音响 / 无线话筒
- `reservation(id, device_id, reservation_date, period, user_name, created_at)`
- 唯一索引 `uk_reservation_slot(device_id, reservation_date, period)`

**冲突防护三层**（新增并发类需求必须沿用此思路）：
1. Service 前置查询校验（给出友好文案）
2. 数据库唯一索引兜底并发（`saveAndFlush` + catch `DataIntegrityViolationException`）
3. 全局异常处理器统一转 `Result` 返回（前端只消费 message）

**目录结构**
```
backend/    Spring Boot 后端（源码 + pom.xml）
frontend/   Vue3 前端（源码 + vite.config.js）
data/       H2 数据文件（勿提交业务数据改动）
docs/反思录/李诗滢/   每日反思件（过程档案）
```

---

## 3. 常用命令

```
run-backend.cmd      # 后端 :8080，就绪标志 "Started ReservationApplication"
run-frontend.cmd     # 前端 :5173（首次自动 npm install）
```

等价裸命令（在对应子目录执行）：
```
cd backend  && mvn spring-boot:run
cd frontend && npm run dev
```

健康检查：`curl http://localhost:8080/api/ping` → `{"status":"ok"}`（唯一不走 Result 封装的接口，健康检查惯例，保留）。

H2 控制台（dev）：`http://localhost:8080/h2-console`，JDBC URL 同上，用户 `sa`，空密码。

收工自查：`python 交作业自查.py --day N`（四盏灯全绿）。

**环境陷阱**（Windows，代理必读）：
- 工具链是用户级 zip 安装：JDK / Maven / Node 在 `%USERPROFILE%\.local\opt\`，脚本已配 PATH，不要假设系统全局可用
- PowerShell 执行策略会拦截 npm 等 `.cmd`：优先在 cmd 或脚本内执行
- `run-*.cmd` 必须保持 **GBK + CRLF** 编码，改用 UTF-8 会中文乱码
- H2 开 `AUTO_SERVER` 后 `data.sql` 默认不执行，须 `spring.sql.init.mode: always`（勿删此配置）

---

## 4. 代码约定（强制）

### 后端
- 统一响应 `Result{code, message, data}`：`code=0` 成功；错误时 `code = HTTP 状态码`，message 为可直接展示给用户的中文文案
- **所有用户可见文案由后端生成并透传**，前端不得自行编造错误原因。冲突文案格式：`{器材名}在 {日期} {时段} 已被预约，请更换时段`
- 业务异常抛 `BizException(code, message)`，由 `GlobalExceptionHandler` 统一转换；参数校验失败返回第一条校验错误
- **时间边界只有一个裁判：后端服务器时间**。"待使用/已结束"状态、日期合法性均由后端计算下发，前端只渲染，不得用浏览器本地时间做业务判断
- 入参校验用 jakarta.validation 注解（`@NotNull` / `@NotBlank` / `@FutureOrPresent` / `@Pattern` / `@Size`）挂在 DTO 上

### 前端
- API 调用统一走 `src/api/reservation.js` 的 axios 实例：拦截器解包 `Result.data`，失败只弹出后端 message
- 用户身份 localStorage key 固定为 `reservation-user`，读写只经 `getUser()/setUser()`
- 页面在 `src/views/`，可复用 UI 在 `src/components/`；组件通信 props down / emits up
- 提示统一用 Element Plus（`ElMessage` / `el-alert`）；弹窗内错误需**持久展示**（el-alert），不能只闪 toast

### 通用
- 注释、commit message、文档一律中文（代码标识符英文）
- 每日改动写入 `docs/反思录/李诗滢/dayN.md`，署名 commit → `git tag dayN` → 推送 → 跑自查脚本

---

## 5. 接口契约

Base：`/api`。响应统一 `Result` 封装（`/api/ping` 除外）。

| 方法 | 路径 | 入参 | 出参 | 错误码 | 状态 |
|---|---|---|---|---|---|
| GET | `/api/ping` | 无 | `{"status":"ok"}` | — | ✅ 现状 |
| GET | `/api/devices` | 无 | `Result<data:[{id, name, description}]>` | — | ✅ 现状 |
| POST | `/api/reservations` | Body：`{deviceId:int, date:"YYYY-MM-DD"(今天≤date≤今天+14), period:"上午"\|"下午", user:string(≤50字)}` | `Result<data:{id, deviceId, deviceName, date, period, userName, status, createdAt}>` | 400 参数不合法；404 器材不存在；409 时段冲突 | ✅ 现状（14 天窗口、status、user ≤50 字均已实施） |
| GET | `/api/reservations` | Query：`user`（必填） | `Result<data: ReservationVO[]>`，按 createdAt 倒序，**最多 200 条** | 400 缺参（Result 封装） | 🔶 演进中（缺参封装已落地；LIMIT 200 待实施） |
| GET | `/api/reservations/stats` | Query：`user`（必填） | `Result<data:{"count":long}>` | 400 缺参（Result 封装） | ✅ 现状 |
| DELETE | `/api/reservations/{id}` | Query：`user`（必填） | `Result<data:null>` | 404 记录不存在；403 非本人预约 | ✅ 现状（905cc18） |

`ReservationVO.status` 取值：`"待使用"`（date ≥ 服务器今天）/ `"已结束"`（date < 服务器今天）。

---

## 6. 任务清单（已拍板，按优先级执行）

### P0（主线，先做）

**T1 · 取消预约（仅本人）** — "约错只能改库"是真实痛点 ✅（2026-10-07 重做轮，905cc18）
- 后端：`DELETE /api/reservations/{id}?user=xxx`，校验记录存在（404）与 `user_name` 归属一致（403，文案如「只能取消自己的预约」）
- 前端：历史列表行内加「取消」按钮，二次确认后调用；成功后 toast + 刷新历史与次数
- 验收：① 本人取消成功，列表移除该行、次数 -1；② 换一个姓名取消同一条 → 403，记录不变；③ 取消不存在的 id → 404；④ curl 复测同结果

**T2 · 限制最多提前 14 天** — 两处小改，防脏数据 ✅（2026-10-07 重做轮，79f7304；前端禁选为基线保留，未再改）
- 前端：`disabledDate` 在禁过去日期基础上，同时禁今天 +14 天之后的日期
- 后端：Service 校验 `date > today+14` → `BizException(400, "预约日期最多提前 14 天")`
- 验收：① 选择器中第 15 天及以后不可选；② 直调 API 传超限日期 → 400 文案正确；③ 今天与 +14 天当天均可约

**T3 · 状态改由后端下发** — 边界只允许一个裁判，演示不被时区坑 ✅（1b02677 为基线保留，2026-10-07 重做轮复验通过）
- 后端：`ReservationVO` 加 `status` 字段，按服务器时间计算（见 §5）
- 前端：删除本地 `reservationStatus()` 计算，直接渲染 `row.status`
- 验收：① 改系统时间或用跨时区手法验证口径一致；② 响应 JSON 含 status 字段且取值合法

### P1（打磨期，不阻塞主线）

**T4 · 错误契约统一收紧** ✅（2026-10-07 重做轮，c66cdf4）
- `ReservationRequest.user` 加 `@Size(max=50, message="姓名不能超过 50 字")`（与 DB 列宽一致，超长落 400 而非 500）
- `GlobalExceptionHandler` 补 `MissingServletRequestParameterException` 处理：缺参也返回 `Result(400)`
- `/api/ping` 保留裸 JSON（已注明的例外）
- 验收：① 51 字姓名 → 400 文案正确（原来是 500）；② 不带 user 调列表接口 → 400 且响应体为 Result 结构

**T5 · CSV 导出（纯前端）**
- 历史卡片加「导出 CSV」按钮，导出当前姓名的全量历史；带 UTF-8 BOM 头防 Excel 乱码（静态版已验证过的经验）；文件名如 `我的预约_YYYYMMDD.csv`
- 验收：导出文件用 Excel 打开不乱码、行数与表格一致

**T6 · device 初始化改为仅空表时执行**
- 删掉 `data.sql` 的 `DELETE FROM device`（或整文件），改为 `ApplicationRunner`：`deviceRepository.count() == 0` 才插入三件套——为二期器材管理留活路
- 验收：① 空库启动出三件套；② 手工加第 4 台器材后重启，第 4 台仍在

**T7 · 历史查询加 LIMIT 200 护栏**
- 后端查询最多返回 200 条（最新在前），班内量级够用，分页排二期
- 验收：造 200+ 条数据，接口只返回最新 200 条， newest 在最上

### 部署前必做（演示当天前）

**T8 · profile 拆分 dev / prod**
- `dev`（默认）：保持现状（H2 console 开、CORS 兜底）
- `prod`：`spring.profiles.active=prod` 启动——关 H2 console；前端 `vite build` 产物由 Spring Boot 同源静态托管，**同源后删除 `WebConfig` 的 CORS 全放行配置**
- 验收：prod 模式下 `/h2-console` 不可达，页面经 `:8080` 直接可用

**文案微调（随任意 P0/P1 顺带）**：成功次数标签改为「累计预约成功 N 次」——口径定为累计成功预约数（含已结束），用文案消除歧义，不动逻辑。✅ 已随 905cc18 落地

---

## 7. 红线：不做 / 已知行为（改需求前先读这里）

| # | 事项 | 结论 | 一句话理由 |
|---|------|------|-----------|
| Q1 | CSV 导出 | 做，P1 | 静态版已有、纯前端可实现，见 T5 |
| Q2 | 登录 | **本期不做** | 电梯稿已声明的已知边界；登录是唯一会拉爆范围的需求。二期学号+口令，表结构已留 `user_name` 不用迁移 |
| Q3 | 取消预约 | 做，P0，仅本人 | 见 T1，与现有信任模型自洽 |
| Q4 | 库存数量 | **不做，二期** | 唯一索引模型 = 单件贵重设备定位，正好成立；库存引入并发扣减/超卖，是范围失控的典型入口 |
| Q5 | 节次时段 | **本期保持上午/下午** | period 字符串存储，扩展成本低；二期若扩，把时段做成配置而非硬编码 |
| Q6 | 提前 N 天 | 做，P0，上限 14 天 | 见 T2 |
| Q7 | 一人同时段多器材 | **不限制，已知行为** | 借投影仪+话筒办同一场活动是合理场景；若老师坚持，Service 加一个 exists 查询即可，先不做 |
| Q8 | 成功次数口径 | 累计成功预约数（含已结束） | 实现即口径，改文案不改逻辑 |
| Q9 | 状态裁判 | 后端下发 | 见 T3 |
| Q10 | device 重插 | 仅空表初始化 | 见 T6 |
| Q11 | 全量返回 | 本期接受 + LIMIT 200 护栏 | 见 T7，分页排二期 |
| Q12 | 错误契约 | 统一收紧，ping 除外 | 见 T4 |
| Q13 | console / CORS | 拆 profile，部署前收紧 | 见 T8 |

**二期候选**（本期一律不启动）：登录（学号+口令）、库存数量、节次时段配置化、分页、器材管理界面。

---

## 8. 验收方式

所有功能必须**双通道验收**，缺一不可：
1. **浏览器实测**：按 §6 各任务的验收标准走完整用户路径
2. **curl 复测**：对每个新接口验证成功路径 + 全部错误码路径

完成后走收工四步（写反思件 → 署名 commit → 打 tag 推送 → 自查脚本四盏灯全绿），详见 README。
