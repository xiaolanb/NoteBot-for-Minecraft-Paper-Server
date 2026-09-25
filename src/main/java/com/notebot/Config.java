/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.configuration.file.FileConfiguration
 */
package com.notebot;

import com.notebot.NotebotPlugin;
import java.util.Locale;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;

public final class Config {
    private final NotebotPlugin plugin;

    public Config(NotebotPlugin plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration c() {
        return this.plugin.getConfig();
    }

    public String prefix() {
        return this.c().getString("prefix", "\u00a7b[Notebot]\u00a7r ");
    }

    public int botCount() {
        return Math.max(1, Math.min(16, this.c().getInt("bot.count", 16)));
    }

    public String namePrefix() {
        return this.c().getString("bot.name-prefix", "Notebot");
    }

    public GameMode gamemode() {
        try {
            return GameMode.valueOf((String)this.c().getString("bot.gamemode", "CREATIVE"));
        }
        catch (Exception ex) {
            return GameMode.CREATIVE;
        }
    }

    public boolean invulnerable() {
        return this.c().getBoolean("bot.invulnerable", true);
    }

    public long spawnIntervalTicks() {
        return Math.max(1L, this.c().getLong("bot.spawn-interval-ticks", 1L));
    }

    public int scanRadius() {
        return Math.max(3, Math.min(32, this.c().getInt("bot.scan-radius", 6)));
    }

    /**
     * 生成假人/扫描时，自动按脚底音符盒方块状态的 instrument 值配对担当：
     * 该音色改派给站在这个音符盒上的假人（覆盖 GUI 里的手动分配）。
     */
    public boolean autoAssign() {
        return this.c().getBoolean("bot.auto-assign", true);
    }

    /** 假人是否受重力（像真实客户端一样摔落）；false = 浮空不动 */
    public boolean botGravity() {
        return this.c().getBoolean("bot.gravity", true);
    }

    /** 假人是否可被其他生物推动（实体碰撞） */
    public boolean botPushable() {
        return this.c().getBoolean("bot.pushable", true);
    }

    /** 假人手的交互距离（格）：原版生存 4.5 / 创造 5.0；调大可点更远的音符盒 */
    public double botReach() {
        return Math.max(3.0, Math.min(64.0, this.c().getDouble("bot.reach", 4.5)));
    }

    public ScanMode scanMode() {
        try {
            return ScanMode.valueOf(this.c().getString("bot.scan-mode", "AROUND").toUpperCase(Locale.ROOT));
        }
        catch (Exception e) {
            return ScanMode.AROUND;
        }
    }

    public int platformSpacing() {
        return Math.max(7, Math.min(16, this.c().getInt("stage.platform-spacing", 8)));
    }

    public long autoRescanTicks() {
        return Math.max(0L, this.c().getLong("playback.auto-rescan-ticks", 100L));
    }

    public float soundRadius() {
        return (float)this.c().getDouble("playback.sound-radius", 48.0);
    }

    public boolean autoSpawn() {
        return this.c().getBoolean("playback.auto-spawn", true);
    }

    public boolean loop() {
        return this.c().getBoolean("playback.loop", false);
    }

    public boolean stageEnabled() {
        return this.c().getBoolean("stage.auto-build", true);
    }

    public boolean soundFallback() {
        return this.c().getBoolean("playback.sound-fallback", true);
    }

    public SoundMode soundMode() {
        try {
            return SoundMode.valueOf(this.c().getString("playback.sound-mode", "AUTO").toUpperCase(Locale.ROOT));
        }
        catch (Exception e) {
            return SoundMode.AUTO;
        }
    }

    public boolean roundOutOfRange() {
        return this.c().getBoolean("playback.round-out-of-range", true);
    }

    public String guiTitle() {
        return this.c().getString("gui.title", "\u00a78\u2630 Notebot \u63a7\u5236\u53f0");
    }

    public String guiSongTitle() {
        return this.c().getString("gui.song-title", "\u00a78\u266a \u9009\u62e9\u66f2\u76ee");
    }

    public String botName(int index) {
        return String.format("%s%02d", this.namePrefix(), index + 1);
    }

    // ==================== 显示（Tab 列表 / MOTD / 加入提示） ====================

    /** 假人是否显示在 Tab 玩家列表 */
    public boolean tablistShow() {
        return this.c().getBoolean("display.tablist", true);
    }

    /** MOTD 玩家列表模式（兼容 ProxyOnlineLinker 等代理插件：KEEP 时不改动他人写入的列表） */
    public MotdMode motdMode() {
        try {
            return MotdMode.valueOf(this.c().getString("display.motd", "KEEP").toUpperCase(Locale.ROOT));
        }
        catch (Exception e) {
            return MotdMode.KEEP;
        }
    }

    /** MOTD 在线人数是否把假人算进去 */
    public boolean motdCountBots() {
        return this.c().getBoolean("display.motd-count-bots", false);
    }

    /**
     * 假人加入/退出提示模式。
     * 注意：YAML 里裸写的 ON/OFF 会被解析成布尔值，因此读取原始值并同时
     * 接受 true/false 写法（true=ON，false=OFF）。
     */
    public JoinMessageMode joinMessageMode() {
        Object raw = this.c().get("display.join-quit-message");
        if (raw == null) {
            return JoinMessageMode.AUTO;
        }
        if (raw instanceof Boolean) {
            return (Boolean)raw ? JoinMessageMode.ON : JoinMessageMode.OFF;
        }
        String text = String.valueOf(raw).trim().toUpperCase(Locale.ROOT);
        switch (text) {
            case "ON":
            case "TRUE": {
                return JoinMessageMode.ON;
            }
            case "OFF":
            case "FALSE": {
                return JoinMessageMode.OFF;
            }
        }
        return JoinMessageMode.AUTO;
    }

    public static enum ScanMode {
        AROUND,
        BELOW;

    }

    public static enum SoundMode {
        AUTO,
        CLICK,
        SIMULATE;

    }

    public static enum MotdMode {
        KEEP,
        BOTS_ONLY,
        HIDDEN;

    }

    public static enum JoinMessageMode {
        AUTO,
        ON,
        OFF;

    }
}

