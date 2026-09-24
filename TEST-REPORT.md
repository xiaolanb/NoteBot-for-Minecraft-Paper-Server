# Notebot 测试报告

测试环境：**Leaf 1.21.11-179**（官方 API 下载），Java 21.0.11，Windows 10。
测试日期：2026-09-19 ~ 09-20。全部测试通过 RCON 控制台命令驱动。

## 测试结果总览（v2 音符盒舞台版）

| # | 测试项 | 结果 |
| --- | --- | --- |
| 1 | 插件加载（NMS 反射初始化，服务器 1.21.11） | ✅ |
| 2 | NBS 解析（5 文件，含 26.5 万音符真实大曲） | ✅ |
| 3 | 音符盒舞台自动搭建（16 组 × 25 声调 + 音色基座） | ✅ |
| 4 | 舞台方块数据（note_block 存在且 note=0，基座=草方块） | ✅ Test passed |
| 5 | 16 假人逐个生成（Tab 列表可见） | ✅ |
| 6 | 假人真实点击音符盒演奏（千本樱 12 秒压测零异常） | ✅ |
| 7 | 暂停 / 继续 / 停止 / 完整演奏结束广播 | ✅ |
| 8 | `/notebot assign`（指定/清空/auto/reset） | ✅ |
| 9 | `/notebot tp <玩家>`（舞台+假人整体搬迁） | ✅ |
| 10 | despawn 后舞台清除、地形还原为空气 | ✅ Test passed |
| 11 | keepalive 防踢（假人持续在线不掉线） | ✅ |
| 12 | 全程真实异常计数 | 0 |

## 关键日志证据

```
[Notebot] 音符盒舞台已搭建于 world (-603, 68, -496)，16 组 × 25 音符盒
> notebot spawn → 正在逐个生成 16 个假人（音符盒舞台已搭建在 world）…
> notebot list → 假人 (16/16) … 舞台: 已搭建
> execute if block -615 69 -508 minecraft:note_block → Test passed
> execute if block -615 69 -508 minecraft:note_block[note=0] → Test passed
> execute if block -615 68 -508 minecraft:grass_block → Test passed
> notebot play 千本樱… →（12 秒）notebot stop → 已停止演奏 千本樱2(18轨推荐使用).nbs
> notebot tp Notebot01 → 舞台与 16 个假人正在移动到 Notebot01 附近…（舞台重建于新位置）
> notebot despawn → 已移除 16 个假人，舞台已清除并还原地形
> execute if block -615 69 -508 minecraft:air → Test passed
```

## 排障记录（已修复并回归验证）

1. **`Connection.setupInboundProtocol` NPE**：1.21.11 网络层重构要求非空 channel。
   修复：实现 fakeplayer 同款 `FakeChannel`（netty AbstractChannel）+ 空实现 `FakeChannelPipeline`。
2. **netty 4.2 `Channel.newSucceededFuture()` 无限递归**：默认实现委托回 pipeline。
   修复：pipeline 的 future 工厂直接构造 `DefaultChannelPromise`。
3. **despawn 假人残留**：1.21.11 退出流程由 netty 事件/`Connection.tick()` 驱动（假人均不触发），
   且 `handleDisconnection` 要求 channel 已关闭；netty 4.2 `AbstractChannel.close()` 走 pipeline()。
   修复：FakeChannel 覆盖 close/disconnect 翻转状态 + 手动触发 `handleDisconnection()`
   + `PlayerList.remove` 兜底。
4. **旧版音色 id 顺序错误**（v1 曾把 id 顺序写错）：已按 NBS 规范修正
   （0=harp 1=bass 2=basedrum 3=snare 4=hat 5=guitar 6=flute 7=bell 8=chime
   9=xylophone 10=iron_xylophone 11=cow_bell 12=didgeridoo 13=bit 14=banjo 15=pling），
   升级后需 `/notebot assign reset` 恢复默认映射。

## 需要玩家肉眼/听觉确认的部分

- 音符粒子与音符盒声音（客户端渲染，服务端已验证点击调用与零异常）；
- 假人转头注视音符盒的动画（转头包已广播，点击前注视目标方块）。
