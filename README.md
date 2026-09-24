# Notebot

⚠️该插件由深度求索生成

适用于 **Leaf / Paper 1.21.11** 的 Minecraft 插件：
**16 个假人分担 16 种音符盒音色**（每种音色 25 个声调 F#3~F#5），
自动演奏 **NBS（Note Block Studio）** 音乐。假人生成方式参照
[tanyaofei/minecraft-fakeplayer](https://github.com/tanyaofei/minecraft-fakeplayer)，
演奏方式与彗星客户端（Comet Client）的 Notebot 一致：**假人转头注视并真实右键点击音符盒**，
声音与音符粒子全部由原版方块产生。

## 特性

- ✅ 假人是**真正的 ServerPlayer**：加入 PlayerList、显示在 Tab 列表、可被 `/tp` `/ban` 等命令作用
- ✅ 自动搭建音符盒舞台：16 组 **5×5 音符盒平台**（25 个方块 = 25 个声调，基座决定音色），
  **假人站在正中央音符盒上**；搭台时强制刷新方块音色状态（不会全变竖琴）；移除时还原地形
- ✅ 演奏时假人**转头注视目标音符盒并真实右键点击**（挥臂动画）
- ✅ 手动模式（不搭台）：扫描玩家自建音符盒（`AROUND` 周围半径 / `BELOW` 只认脚底）
- ✅ **自动配对担当**：生成/扫描时按脚底音符盒方块状态的 instrument 值自动改派音色，
  GUI 分配优先（一个假人可以担当多个音色）
- ✅ **显示配置**：Tab 列表显隐、MOTD 玩家样本（兼容 ProxyOnlineLinker Redis 模式）、
  加入/退出提示（兼容 CustomJoinMessages）
- ✅ **物理可配**：`gravity`（是否摔落）、`pushable`（是否被生物推动）、`reach`（手的交互距离）
- ✅ 箱子 GUI（音色分配二级菜单、选曲即播、播放控制）
- ✅ **messages.yml 语言文件**：全部文案可改，支持 `&` 颜色码与 `&#RRGGBB` hex 颜色
- ✅ NBS v0~v5 解析（26 万音符大曲目实测通过）、keepalive 防踢、越界音高处理

## 环境要求

- 服务端：Leaf / Paper **1.21.11**
- Java 21+
- 构建：Maven 3.9+（JDK 21）

## 构建

```bash
mvn clean package
# 产物: target/Notebot-1.0.0.jar
```

## 安装

1. 把 `Notebot-1.0.0.jar` 放入 `plugins/`；
2. 把 `.nbs` 曲目放入 `plugins/Notebot/songs/`；
3. 启动服务器（`/notebot reload` 可刷新曲目库）。

## 快速上手

1. `/notebot spawn all` —— 生成 16 个假人（默认会在指令执行者位置搭建 16 组 5×5 音符盒舞台，假人各站一组平台）
2. `/notebot play <曲目>` —— 开始演奏（可用 Tab 补全曲名）
3. `/notebot gui` —— 图形化控制台（分配音色、选曲、播放控制）

## 命令

| 命令 | 说明 |
| --- | --- |
| `/notebot spawn` | 生成**一个**假人（生成在指令执行者位置） |
| `/notebot spawn all` | 逐个生成全部假人 |
| `/notebot despawn [假人名]` | 移除全部假人 / 移除指定假人 |
| `/notebot play <曲目>` | 播放 .nbs（假人不足自动补足，可关闭） |
| `/notebot stop` / `pause` / `resume` | 停止 / 暂停 / 继续 |
| `/notebot tphere [假人名]` | 全部假人传送到你身边；带假人名 = 仅该假人（仅玩家） |
| `/notebot rescan` | 重新扫描身边音符盒（手动模式） |
| `/notebot stage <build\|clear>` | 手动搭建 / 清除舞台 |
| `/notebot gui` / `list` / `reload` | 控制台 GUI / 状态 / 重载 |

`tphere` 与 `despawn` 支持 Tab 补全（补全在线假人名单）。

## 箱子 GUI（`/notebot gui`）

- 前 16 格 = 16 种音色，点击进入**二级分配菜单**（选假人 / 无 / 自动）；
- `♪ 选择曲目`：分页曲目列表，**点击曲目立即播放**；
- `▶ 播放`、`⏸ 暂停`、`⏹ 停止`、`🔁 循环`、`+ 生成一个假人`、`- 移除全部假人`。

## 演奏模式

### 自动舞台（`stage.auto-build: true`，默认）

生成/传送假人时自动搭建 16 组 5×5 音符盒平台（25 个方块 = 25 个声调，
基座决定音色并强制写入方块音色状态），**假人站在正中央那个音符盒上**；
移除假人时自动清除并还原地形。

### 手动模式（`stage.auto-build: false`）

不改动地形，使用你自行摆放的音符盒（音色按基座方块识别、声调按方块状态识别）：

- `bot.scan-mode: AROUND`：扫描每个假人身边 `bot.scan-radius` 格内的音符盒；
- `bot.scan-mode: BELOW`：只检测每个假人**脚下**的音符盒，一个音符盒即可
  承担该音色全部 25 个声调（点击前自动预置音高）。

扫描发生在假人生成/传送/每次演奏开始/定期重扫（`playback.auto-rescan-ticks`），
也可用 `/notebot rescan` 手动触发，结果会以消息反馈（如
"检测到 25 个音符盒：竖琴 HARP×25"）。

### 自动配对担当（`bot.auto-assign`，默认 `true`）

生成假人/扫描时，自动读取**脚底音符盒方块状态里的 instrument 值**
（右键发声实际使用的音色），把该音色改派给站在这个音符盒上的假人。
只填补“无分配 / 原担当假人已离线”的音色，**不会抢在线假人的已有分配**
（GUI 分配优先，因此一个假人可以担当多个音色）；设为 `false` 则完全由 GUI 决定。

> 一个假人担当多个音色时，这些音色的音符盒必须在它手的交互距离
> （`bot.reach`）以内，否则点不到。需要点更远的平台就把 `bot.reach` 和
> `bot.scan-radius` 一起调大（如 32）。

## 发声模式（`playback.sound-mode`）

| 值 | 行为 |
| --- | --- |
| `AUTO`（默认） | 有音符盒就真实点击（原版声音+粒子）；无则按 `sound-fallback` 模拟发声 |
| `CLICK` | 只真实点击，没有可用音符盒就跳过 |
| `SIMULATE` | 始终模拟发声（假人位置播放原版音符盒声音+挥臂，不点击方块） |

`playback.round-out-of-range`：越界音高（NBS key < 33 或 > 57）
`true` = 就近取整到 0-24；`false` = 跳过该音符。

## 假人物理（`bot` 段）

```yaml
bot:
  # 假人是否受重力（像真实客户端一样摔落）；false = 浮空不动
  gravity: true
  # 假人是否可被其他生物推动（实体碰撞）；false = 其他生物无法推动它
  pushable: true
  # 假人手的交互距离（格）：原版生存 4.5 / 创造 5.0。
  # 调大（如 32）可让一个假人点击更远的音符盒；手动模式下还需把
  # bot.scan-radius 调到同样大，远方的音符盒才会被扫描登记。
  reach: 4.5
```

三项都支持 `/notebot reload` 后对**在线假人**即时生效。

## 显示配置（`display` 段）

假人是真实在线玩家，默认会出现在 Tab 列表、MOTD 玩家样本，并产生加入/退出提示。

```yaml
display:
  # 假人是否显示在 Tab 玩家列表（false = 隐藏，假人仍在线并正常演奏）
  tablist: true
  # MOTD 玩家样本：KEEP = 不修改（假人显示真名）；BOTS_ONLY = 只显示假人；
  # HIDDEN = 不显示假人（保留真实玩家与其他插件写入的内容）
  motd: KEEP
  # MOTD 在线人数是否把假人算进去（false = 从人数中减去在线假人）
  motd-count-bots: false
  # 加入/退出提示："AUTO" = 交给服务端/其他插件；"ON" = 用 messages.yml 的
  # bot-join / bot-quit 发送；"OFF" = 不显示。也可写成 true/false。
  join-quit-message: "AUTO"
```

### 兼容 ProxyOnlineLinker 的 Redis 模式

该模式下 MOTD 的玩家列表/人数由代理端从 Redis 下发。保持 `motd: KEEP` 并把
`motd-count-bots` 设为 `false` 即可：本插件**不会覆盖**代理端或其他插件写入的
列表内容，只在不改动列表时按开关调整人数。

### 兼容 CustomJoinMessages

`join-quit-message: "AUTO"`（默认）时不插手加入/退出提示——假人走的是真实
PlayerJoinEvent/PlayerQuitEvent，CustomJoinMessages 会照常格式化假人的提示。
`ON` 时本插件改用 messages.yml 的 `bot-join` / `bot-quit` 发送；`OFF` 时完全静默。

## 完整配置参考 `config.yml`

```yaml
prefix: "§b[Notebot]§r "

bot:
  name-prefix: "Notebot"        # 假人名字（Notebot01~16）
  count: 16
  gamemode: CREATIVE            # 创造模式点击距离更远
  invulnerable: true
  spawn-interval-ticks: 1       # spawn all 时逐个生成的间隔（tick）
  auto-assign: true             # 按脚底音符盒 instrument 值自动配对担当（不抢在线分配）
  gravity: true                 # 假人是否受重力（摔落）
  pushable: true                # 假人是否可被其他生物推动
  reach: 4.5                    # 假人手的交互距离（格）
  scan-radius: 6                # 手动模式扫描半径（格）
  scan-mode: AROUND             # AROUND / BELOW

stage:
  auto-build: true              # 自动搭建/清除音符盒舞台
  platform-spacing: 8           # 自动舞台 4×4 槽位间距（格）

playback:
  sound-radius: 48              # 转头包的广播半径（格）
  auto-spawn: true              # 演奏前自动补足假人
  loop: false
  auto-rescan-ticks: 100        # 手动模式演奏期重扫间隔（0=关闭）
  sound-mode: AUTO              # AUTO / CLICK / SIMULATE
  sound-fallback: true          # AUTO 模式无音符盒时模拟发声
  round-out-of-range: true      # 越界音高就近取整

gui:
  title: "§8☰ Notebot 控制台"
  song-title: "§8♪ 选择曲目"

display:
  tablist: true
  motd: KEEP
  motd-count-bots: false
  join-quit-message: "AUTO"
```

## 语言文件 `messages.yml`

插件首次启动自动生成，包含全部命令/广播/提示文案（`{0}` `{1}` 为占位符）。
支持：

- `&` 传统颜色码：`&a` `&b` `&l` …；
- `&#RRGGBB` hex 颜色，如 `&#FFAA00金色文字`。

修改后 `/notebot reload` 生效。

## 兼容性说明

- 仅保证 Leaf/Paper **1.21.11**（NMS 反射按该版本签名逐一验证）；
- 假人是真实玩家实体：默认无敌，`pushable`/`gravity` 控制其物理行为，
  会触发登录类插件的加入事件；
- NBS 自定义音色（id ≥ 16，依赖资源包声音）自动跳过。

## 参考项目

- [tanyaofei/minecraft-fakeplayer](https://github.com/tanyaofei/minecraft-fakeplayer) — 假人生成方式
- [Comet Client Notebot](https://github.com/cubk/Comet) — 假人点击音符盒的演奏方式
- [OpenNoteBlockStudio](https://github.com/OpenNBS/NoteBlockStudio) — NBS 文件格式
