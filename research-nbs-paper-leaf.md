# 调研报告：Leaf 1.21.11 / Paper API / NBS 格式

> 调研环境限制：本沙箱网络无法访问 github.com / api.github.com / raw.githubusercontent.com / web.archive.org（DNS 解析到非公网 IP），也无法从 shell 出网。GitHub 相关事实改由官方站点 API、镜像站与 CDN（jsDelivr 代取 GitHub 原始文件）验证。所有结论均已标注来源 URL。

---

## 任务 1：Leaf（Winds-Studio/Leaf）1.21.11 发行版

**结论：存在，官方下载渠道为 Leaf 官网 API（leafmc.one），不是 GitHub Releases 附件。**

- 官方下载页（按版本）：<https://www.leafmc.one/en/download/1.21.11>
  - 页面标题 "Leaf 1.21.11 · Build #179"，**Released September 12, 2026，Java 21+**，sha256: `5da79782215c1a25edcd7c73b3523b7ecb7f4b86dc8a5846a176ed69bc2cd020`
  - 官方直链（最新稳定版 #179）：
    `https://api.leafmc.one/v2/projects/leaf/versions/1.21.11/builds/179/downloads/leaf-1.21.11-179.jar`
  - 脚本下载：`curl -O https://api.leafmc.one/v2/projects/leaf/versions/1.21.11/builds/179/downloads/leaf-1.21.11-179.jar`
- 完整构建清单（172 个 build，build 2 → 179，含 sha256、channel、commit）：
  <https://api.leafmc.one/v2/projects/leaf/versions/1.21.11/builds>
  - 资产命名规律：`leaf-1.21.11-<build>.jar`（如 `leaf-1.21.11-178.jar`、`leaf-1.21.11-175.jar`）
  - 近期稳定构建：#175 (2026-08-16)、#176 (08-28)、#177 (08-29)、#178 (08-30)、#179 (09-12)
- GitHub Releases 页面（<https://github.com/Winds-Studio/Leaf/releases>）本环境无法直连；经镜像站 [mygit.top](https://mygit.top/repository/499182897) 确认存在两个 tag 为 **`ver-1.21.11`** 的 GitHub Release：
  - 2026-05-13（<https://mygit.top/release/321578033>，详情页被 Cloudflare 拦截）
  - 2026-07-30（<https://mygit.top/release/362354211>，详情页被 Cloudflare 拦截）
  - 镜像数据截至 2026-08-15，之后可能还有新 release（对应 build #175~#179）。
  - ⚠️ 无法在本环境直接枚举 GitHub release 的资产文件名；Leaf 官方 README 也注明 "Download Leaf from our Website or get latest build in GitHub Releases"，官网 API 是权威渠道。需要 GitHub 资产精确列表时请在能访问 GitHub 的网络下核对 release 详情。
- Leaf API 坐标（镜像 README，供插件开发）：
  - 仓库 `https://maven.leafmc.one/snapshots/`，`cn.dreeam.leaf:leaf-api`（镜像示例为 `26.2.local-SNAPSHOT`，1.21.11 对应版本以仓库 metadata 为准）

---

## 任务 2：Paper API Maven 版本

抓取 <https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/maven-metadata.xml>（HTTP 200）。全部 1.21.x 版本：

```
1.21-R0.1-SNAPSHOT
1.21.1-R0.1-SNAPSHOT
1.21.3-R0.1-SNAPSHOT
1.21.4-R0.1-SNAPSHOT
1.21.5-no-moonrise-SNAPSHOT
1.21.5-R0.1-SNAPSHOT
1.21.6-R0.1-SNAPSHOT
1.21.7-R0.1-SNAPSHOT
1.21.8-R0.1-SNAPSHOT
1.21.9-rc1-R0.1-SNAPSHOT
1.21.9-pre2-R0.1-SNAPSHOT
1.21.9-pre3-R0.1-SNAPSHOT
1.21.9-pre4-R0.1-SNAPSHOT
1.21.9-R0.1-SNAPSHOT
1.21.10-R0.1-SNAPSHOT
1.21.11-rc1-R0.1-SNAPSHOT
1.21.11-rc2-R0.1-SNAPSHOT
1.21.11-rc3-R0.1-SNAPSHOT
1.21.11-pre3-R0.1-SNAPSHOT
1.21.11-pre4-R0.1-SNAPSHOT
1.21.11-pre5-R0.1-SNAPSHOT
1.21.11-R0.1-SNAPSHOT   ← ✅ 存在
```

- **`1.21.11-R0.1-SNAPSHOT` 存在**；**最新 1.21.x = `1.21.11-R0.1-SNAPSHOT`**（其后是 26.x 系列，如 `26.3-pre-2.build.0-alpha`，metadata 的 `<latest>` 指向 26.x）。
- 构件直链（按 Paper 仓库惯例）：
  `https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/1.21.11-R0.1-SNAPSHOT/paper-api-1.21.11-R0.1-SNAPSHOT.jar`
- **dev-bundle 同样存在对应版本**：抓取 <https://repo.papermc.io/repository/maven-public/io/papermc/paper/dev-bundle/maven-metadata.xml>（HTTP 200）确认含 `1.21.11-R0.1-SNAPSHOT`。
  ⚠️ 注意 artifactId 路径是 `io/papermc/paper/dev-bundle`（**不是** `paper-dev-bundle`；后者 404）。
  Gradle 坐标：`io.papermc.paper:dev-bundle:1.21.11-R0.1-SNAPSHOT`。

---

## 任务 3：Paper API 1.21.11 的 API 细节

验证基准：Paper 官方版本化 javadoc（`paper-api 1.21.11-R0.1-SNAPSHOT`），主页 <https://jd.papermc.io/paper/1.21.11/>。

### 3.1 16 个音符盒音色 → org.bukkit.Sound 常量（1.21.11 全部存在、均未弃用）

来源：[Sound 常量列表（Spigot-API javadoc）](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html) + [NoteBlockAPI InstrumentUtils（按 Bukkit Sound 枚举名读取，名称自 1.13 扁平化后未变）](https://cdn.jsdelivr.net/gh/koca2000/NoteBlockAPI@master/src/main/java/com/xxmicloxx/NoteBlockAPI/utils/InstrumentUtils.java)。1.21.11 的 deprecated-list 中没有任何 NOTE_BLOCK 条目（见 [deprecated-list](https://jd.papermc.io/paper/1.21.11/deprecated-list.html)）。

| NBS 名 | org.bukkit.Sound 常量 |
|---|---|
| harp | `BLOCK_NOTE_BLOCK_HARP` |
| basedrum | `BLOCK_NOTE_BLOCK_BASEDRUM` |
| snare | `BLOCK_NOTE_BLOCK_SNARE` |
| hat | `BLOCK_NOTE_BLOCK_HAT` |
| bass | `BLOCK_NOTE_BLOCK_BASS` |
| flute | `BLOCK_NOTE_BLOCK_FLUTE` |
| bell | `BLOCK_NOTE_BLOCK_BELL` |
| guitar | `BLOCK_NOTE_BLOCK_GUITAR` |
| chime | `BLOCK_NOTE_BLOCK_CHIME` |
| xylophone | `BLOCK_NOTE_BLOCK_XYLOPHONE` |
| iron_xylophone | `BLOCK_NOTE_BLOCK_IRON_XYLOPHONE` |
| cow_bell | `BLOCK_NOTE_BLOCK_COW_BELL` |
| didgeridoo | `BLOCK_NOTE_BLOCK_DIDGERIDOO` |
| bit | `BLOCK_NOTE_BLOCK_BIT` |
| banjo | `BLOCK_NOTE_BLOCK_BANJO` |
| pling | `BLOCK_NOTE_BLOCK_PLING` |

- ⚠️ `BLOCK_NOTE_BLOCK_TRUMPET` 等 4 个小号变体是 **MC 26.1+ 才加入**的（NoteBlockAPI 的版本门控为 server ≥ 26.1；1.21.11 的 `org.bukkit.Instrument` 枚举中没有 TRUMPET），**1.21.11 不存在**，勿在 1.21.11 编译中使用。
- 1.21.11 的 `org.bukkit.Instrument`（<https://jd.papermc.io/paper/1.21.11/org/bukkit/Instrument.html>）＝16 个经典音色（PIANO/BASS_DRUM/SNARE_DRUM/STICKS/BASS_GUITAR/FLUTE/BELL/GUITAR/CHIME/XYLOPHONE/IRON_XYLOPHONE/COW_BELL/DIDGERIDOO/BIT/BANJO/PLING）+ 头颅音色（ZOMBIE/SKELETON/CREEPER/DRAGON/WITHER_SKELETON/PIGLIN/CUSTOM_HEAD）；提供 `getSound()`、`getByType(byte)`。

### 3.2 playSound 方法签名（1.21.11，全部非弃用）

- **`org.bukkit.SoundSource` 在 Paper API 中不存在**——1.21.11 javadoc 的 `org/bukkit/SoundSource.html` 与 `class-use/SoundSource.html` 均 404（连 26.3 javadoc 同样 404）。用户问题中的 "SoundSource" 概念只对应 Adventure 的 `net.kyori.adventure.sound.Sound.Source`：1.21.11 的 `SoundCategory` 实现 `Sound.Source.Provider`（见 [SoundCategory javadoc](https://jd.papermc.io/paper/1.21.11/org/bukkit/SoundCategory.html)）。
- **1.21.11 的 deprecated-list 中没有任何 playSound 条目**（grep 验证）→ Sound 参数版**未弃用**，可直接用。
- 方法声明在 `org.bukkit.World` 上（1.21.11 的 `RegionAccessor` 上**没有** playSound）。签名集合（12 个重载，与 [Spigot-API World javadoc](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/World.html) 列出的集合一致，Paper 1.21.11 同款）：

```java
void playSound(Location location, Sound sound, float volume, float pitch)
void playSound(Location location, String sound, float volume, float pitch)          // 自定义资源包音效
void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch)
void playSound(Location location, String sound, SoundCategory category, float volume, float pitch)
void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch, long seed)
void playSound(Location location, String sound, SoundCategory category, float volume, float pitch, long seed)
void playSound(Entity entity, Sound sound, float volume, float pitch)
void playSound(Entity entity, String sound, float volume, float pitch)
void playSound(Entity entity, Sound sound, SoundCategory category, float volume, float pitch)
void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch)
void playSound(Entity entity, Sound sound, SoundCategory category, float volume, float pitch, long seed)
void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch, long seed)
```

- 即：音符盒播放推荐 `world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_HARP, SoundCategory.RECORDS, volume, pitch)`；音量 0.0–1.0，pitch 浮点倍率（0.5–2.0 常规）。
- `SoundCategory`（1.21.11）11 个值：MASTER, MUSIC, RECORDS, WEATHER, BLOCKS, HOSTILE, NEUTRAL, PLAYERS, AMBIENT, VOICE, UI。

### 3.3 Java 版本

- **Java 21**。PaperMC 官方公告 [Paper & Velocity 1.20.6](https://papermc.io/news/paper-velocity-1-20-6/)："Minecraft 1.20.6 requires you to run Java 21"，1.21.11 沿用；Leaf 1.21.11 下载页同样标注 "Java 21+"。
- 另：1.21.11 javadoc 的 java.base 链接均为 `javase/21`（如 World javadoc 中 Consumer 链接），进一步佐证。

---

## 任务 4：NBS 文件格式（供解析器实现）

来源（多实现交叉验证）：
- 官方 wiki（OpenNBS GitBook，可访问但本环境抓取被截断，拿到完整“版本沿革”部分）：<https://opennbs.gitbook.io/open-note-block-studio/nbs-format>
- **NoteBlockAPI NBSDecoder.java（参考解析器，全文验证）**：<https://cdn.jsdelivr.net/gh/koca2000/NoteBlockAPI@master/src/main/java/com/xxmicloxx/NoteBlockAPI/utils/NBSDecoder.java>
- omninbs/luanbs（独立 Lua 实现，字段表逐字节声明）：<https://cdn.jsdelivr.net/gh/omninbs/luanbs@master/src/fields.lua> 、parser：<https://cdn.jsdelivr.net/gh/omninbs/luanbs@master/src/parser.lua>
- nbs-rs 0.1.1 文档（字段语义）：<https://docs.rs/nbs-rs/0.1.1/nbs/header/struct.Header.html> 、Note：<https://docs.rs/nbs-rs/0.1.1/nbs/noteblocks/note/struct.Note.html> 、Layer：<https://docs.rs/nbs-rs/0.1.1/nbs/noteblocks/layer/struct.Layer.html>

通用规则：**所有数据为 little-endian、有符号**；字符串 = **4 字节 int32 长度 + 原始字节**（旧版 wiki 曾用 2 字节长度，现代格式与全部参考解析器均为 int32 长度；各版本统一 4 字节，不存在“版本差异”）。文件由 4 部分构成：Header、Note Blocks、Layers、Custom Instruments（后两者可选/尾部）。

新格式判定：**文件头 2 字节（short）== 0** → 新版格式；**!= 0** → 旧格式且该 short 就是歌曲长度。旧格式差异：无版本号字节、无 vanilla 音色数字节（vanilla 数按 9/10/16 假设）、无 v3+ 歌曲长度回填；层只有 name+volume；音符只有 tick/layer/instrument/key；（原版规范中旧格式在 midi 文件名后有 loop 三字段，但 NoteBlockAPI 等解析器对旧格式不读 loop 字段——解析器实现时按 NBSDecoder 行为即可）。

### 4.1 Header（新版格式，字段顺序 = 字节顺序）

| 偏移 | 大小 | 字段 | 说明 |
|---|---|---|---|
| 0 | short (2) | 格式标记 | 恒为 0（新版）；非 0 = 旧格式歌曲长度 |
| 2 | byte | NBS 版本 | 1–5 |
| 3 | byte | vanilla 音色数量 | 自定义音色 id 起点 |
| 4 | short (2) | 歌曲长度 | **仅 v3+**（⚠️ 是 2 字节 short，不是 4 字节；v1/v2 不存，需读完音符段取最大 tick） |
| 6 | short (2) | 层数量 | |
| 8 | string (int32+bytes) | 歌曲名 | |
| — | string | 作者 | |
| — | string | 原作者 | |
| — | string | 歌曲描述 | |
| — | short (2) | tempo | **单位：每秒 tick 数 × 100**（如 1000 = 10 TPS；NoteBlockAPI 读后 /100f） |
| — | byte | 自动保存开关 | 0/1（v4 起程序已不使用但字段保留） |
| — | byte | 自动保存间隔 | 分钟，1–60 |
| — | byte | 拍号 | x/4 中的 x，2–8，默认 4 |
| — | int (4) | 项目累计分钟 | |
| — | int (4) | 左键次数 | |
| — | int (4) | 右键次数 | |
| — | int (4) | 添加音符次数 | |
| — | int (4) | 删除音符次数 | |
| — | string | 导入文件名 | 来自 .mid/.schematic |
| — | byte | loop 开关 | **仅 v4+**，0/1 |
| — | byte | 最大循环次数 | **仅 v4+**，0 = 无限 |
| — | short (2) | loop 起始 tick | **仅 v4+** |

### 4.2 音符段（Note ticks）

编码为**累积差值（delta）**：

```
循环 {
  short tickJump      // 相对上一 tick 的差值（首个从 -1 起累加）；== 0 表示音符段结束
  当前tick内循环 {
    short layerJump   // 相对上一层的差值（每个 tick 从 -1 重新起算）；== 0 表示该 tick 音符结束
    byte  instrument  // 音色 id（≥vanilla 数为自定义音色）
    byte  key         // 音高键，33–57 为标准两八度范围（全量 0–87，0=A0、87=C8）
    [v4+] byte  velocity  // 力度/音量 0–100（%），与层音量相乘
    [v4+] byte  panning   // 立体声位置 0–200：0=右2格，100=居中，200=左2格
                         //（注意与 MC 内部 panning 约定相反；NoteBlockAPI 用 200-值 转换）
    [v4+] short pitch     // 微调音分（cents）：0=无微调，±100=1 半音，NBS 限 ±1200
  }
}
```

- v1/v2 音符无 velocity/panning/pitch（默认 100/100/0）。
- ⚠️ tick -1 语义：官方 wiki 完整表格本环境未能全文抓取；三个参考解析器（NoteBlockAPI、luanbs、nbs-rs）均按“有符号 delta 累加、0 终止”实现，无 -1 特判。若遇到旧文档提到 tick=-1“重复上一 tick”，以参考解析器行为为准（负 delta 即回退 tick）。

### 4.3 层段（layerCount 条）

| 大小 | 字段 | 说明 |
|---|---|---|
| string | name | 层名 |
| byte | lock | **仅 v4+**，0/1 |
| byte | volume | 0–100（%），默认 100 |
| byte | panning | **仅 v2+**，同音符 panning 约定（0 右 / 100 中 / 200 左） |

### 4.4 自定义音色段（v5+，文件末尾）

- byte count（v5 最多 240，此前最多 18；无自定义音色时为 0），后接 count 条：
  - string 名称、string 声音文件名（v5 起可含 `/` 子路径）、byte 音高键、byte 是否按压按键(0/1)。
- v5 与 v4 字段结构完全相同（官方：v5 文件可按 v4 读取）。

### 4.5 16 个 vanilla 音色 id → 音符盒音色

来源：[NoteBlockAPI InstrumentUtils](https://cdn.jsdelivr.net/gh/koca2000/NoteBlockAPI@master/src/main/java/com/xxmicloxx/NoteBlockAPI/utils/InstrumentUtils.java)（0–15 与 OpenNBS 文件内 id 顺序一致）：

| id | NBS/OpenNBS 名 | 资源路径 (minecraft:) | Bukkit Sound 常量 |
|---|---|---|---|
| 0 | Harp / Piano | block.note_block.harp | BLOCK_NOTE_BLOCK_HARP |
| 1 | Double Bass | block.note_block.bass | BLOCK_NOTE_BLOCK_BASS |
| 2 | Bass Drum | block.note_block.basedrum | BLOCK_NOTE_BLOCK_BASEDRUM |
| 3 | Snare Drum | block.note_block.snare | BLOCK_NOTE_BLOCK_SNARE |
| 4 | Click / Hat / Sticks | block.note_block.hat | BLOCK_NOTE_BLOCK_HAT |
| 5 | Guitar | block.note_block.guitar | BLOCK_NOTE_BLOCK_GUITAR |
| 6 | Flute | block.note_block.flute | BLOCK_NOTE_BLOCK_FLUTE |
| 7 | Bell | block.note_block.bell | BLOCK_NOTE_BLOCK_BELL |
| 8 | Chime | block.note_block.chime | BLOCK_NOTE_BLOCK_CHIME |
| 9 | Xylophone | block.note_block.xylophone | BLOCK_NOTE_BLOCK_XYLOPHONE |
| 10 | Iron Xylophone | block.note_block.iron_xylophone | BLOCK_NOTE_BLOCK_IRON_XYLOPHONE |
| 11 | Cow Bell | block.note_block.cow_bell | BLOCK_NOTE_BLOCK_COW_BELL |
| 12 | Didgeridoo | block.note_block.didgeridoo | BLOCK_NOTE_BLOCK_DIDGERIDOO |
| 13 | Bit | block.note_block.bit | BLOCK_NOTE_BLOCK_BIT |
| 14 | Banjo | block.note_block.banjo | BLOCK_NOTE_BLOCK_BANJO |
| 15 | Pling | block.note_block.pling | BLOCK_NOTE_BLOCK_PLING |
| 16–19 | Trumpet 系列（26.1+，1.21.11 无） | block.note_block.trumpet… | — |

### 4.6 NBS key ↔ MC 音高 / pitch 计算

- **MC 音符盒 note 值 = NBS key − 33**（key 33 = 音符盒音高 0 = F#3；key 45 = 音高 12 = F#4；key 57 = 音高 24 = F#5）。Bukkit `org.bukkit.Note` 的音高编号与之相同（F#=0 … F# 两个八度 = 24，即 `Note.Tone` 范围 0xC=12 个音、两个八度）。
- **pitch 浮点公式 `2^((key-45)/12)` 是准确的**：MC 音符盒声音素材以 F#4（key 45）为基准录制，pitch=1.0 对应 key 45；key 33 → 2^(−1) = 0.5；key 57 → 2^(+1) = 2.0。
- 含 v4+ 微调音分 p 时：`pitch = 2^((key - 45)/12 + p/1200)`。
- 越界处理参考（NoteBlockAPI warpNameOutOfRange）：key<33 用音效名后缀 `_-1`（key<9 用 `_-2`），key>57 用 `_1`（>81 用 `_2`）——仅在需要超出两八度时把 key 折算回 33–57 区间并叠加 pitch 倍率。
