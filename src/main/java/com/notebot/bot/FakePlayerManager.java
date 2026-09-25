/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.notebot.bot;

import com.notebot.Config;
import com.notebot.Nms;
import com.notebot.NotebotPlugin;
import com.notebot.bot.FakeBot;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class FakePlayerManager {
    private final NotebotPlugin plugin;
    private final Map<String, FakeBot> bots = new LinkedHashMap<String, FakeBot>();
    private BukkitTask spawnTask;
    private boolean spawning;

    public FakePlayerManager(NotebotPlugin plugin) {
        this.plugin = plugin;
    }

    public int count() {
        return this.bots.size();
    }

    public boolean isSpawning() {
        return this.spawning;
    }

    public List<String> names() {
        ArrayList<String> list = new ArrayList<String>(this.bots.keySet());
        Collections.sort(list);
        return list;
    }

    public FakeBot get(String name) {
        return this.bots.get(name);
    }

    public boolean isBot(String name) {
        return this.bots.containsKey(name);
    }

    /** 在线假人列表 */
    public List<FakeBot> onlineBots() {
        return new ArrayList<FakeBot>(this.bots.values());
    }

    /** 在线假人的 UUID 列表（用于 Tab 列表隐藏） */
    public List<UUID> onlineUuids() {
        ArrayList<UUID> list = new ArrayList<UUID>();
        for (FakeBot bot : this.bots.values()) {
            UUID uuid = bot.uuid();
            if (uuid != null) {
                list.add(uuid);
            }
        }
        return list;
    }

    /** 配置里的全部假人名字（含当前不在线的），用于识别假人的加入/退出提示 */
    public List<String> allNames() {
        Config config = this.plugin.config();
        ArrayList<String> list = new ArrayList<String>(config.botCount());
        for (int index = 0; index < config.botCount(); ++index) {
            list.add(config.botName(index));
        }
        return list;
    }

    public boolean isBotName(String name) {
        return name != null && this.allNames().contains(name);
    }

    /** 假人的 Tab 列表显示说明（用于提示消息） */
    public String tabState() {
        return this.plugin.config().tablistShow()
                ? this.plugin.lang().get("spawn-tab-shown", "\u5df2\u663e\u793a\u5728 Tab \u5217\u8868")
                : this.plugin.lang().get("spawn-tab-hidden", "\u5df2\u4ece Tab \u5217\u8868\u9690\u85cf");
    }

    public String spawnNext(Location anchor) {
        World world;
        Location base;
        if (anchor != null && anchor.getWorld() != null) {
            world = anchor.getWorld();
            base = anchor.clone();
        } else if (!Bukkit.getWorlds().isEmpty()) {
            world = (World)Bukkit.getWorlds().get(0);
            base = world.getSpawnLocation();
        } else {
            return null;
        }
        if (this.plugin.config().stageEnabled() && !this.plugin.stage().isBuilt()) {
            this.plugin.stage().build(base);
        }
        Config config = this.plugin.config();
        for (int index = 0; index < config.botCount(); ++index) {
            String name = config.botName(index);
            if (this.bots.containsKey(name)) continue;
            this.spawnOne(name, index, base);
            return this.bots.containsKey(name) ? name : null;
        }
        return null;
    }

    public void teleportTo(Location target) {
        if (target == null || target.getWorld() == null) {
            return;
        }
        if (this.plugin.config().stageEnabled()) {
            this.plugin.stage().clear();
            this.plugin.stage().build(target);
        } else {
            this.plugin.stage().clear();
        }
        for (FakeBot bot : this.bots.values()) {
            Player player = bot.player();
            Location pos = this.plugin.stage().botPosition(bot.index());
            if (pos == null) {
                pos = target.clone();
            }
            if (player == null || !player.isOnline()) continue;
            player.teleport(pos);
            bot.resetView();
        }
        this.rescanIfManual();
    }

    public boolean remove(String name) {
        FakeBot bot = this.bots.remove(name);
        if (bot == null) {
            return false;
        }
        try {
            Nms.INSTANCE.removeFakePlayer(bot.player());
        }
        catch (Exception ex) {
            this.plugin.getLogger().warning("\u79fb\u9664\u5047\u4eba\u5f02\u5e38 " + name + ": " + ex.getMessage());
        }
        this.rescanIfManual();
        return true;
    }

    public boolean teleportOne(String name, Location target) {
        FakeBot bot = this.bots.get(name);
        if (bot == null || bot.player() == null || !bot.player().isOnline() || target == null) {
            return false;
        }
        bot.player().teleport(target.clone());
        bot.resetView();
        this.rescanIfManual();
        return true;
    }

    public String rescanIfManual() {
        if (this.plugin.config().stageEnabled()) {
            return "";
        }
        return this.plugin.stage().rescan(this.onlineBots());
    }

    public int spawnAll(Location anchor) {
        Location base;
        if (anchor != null && anchor.getWorld() != null) {
            World world = anchor.getWorld();
            base = anchor.clone();
        } else if (!Bukkit.getWorlds().isEmpty()) {
            World world = (World)Bukkit.getWorlds().get(0);
            base = world.getSpawnLocation();
        } else {
            return 0;
        }
        if (this.plugin.config().stageEnabled() && !this.plugin.stage().isBuilt()) {
            this.plugin.stage().build(base);
        }
        Config config = this.plugin.config();
        int count = config.botCount();
        int[] next = new int[]{0};
        if (this.spawnTask != null) {
            this.spawnTask.cancel();
        }
        this.spawning = true;
        this.spawnTask = null;
        this.plugin.getServer().getScheduler().runTaskTimer((Plugin)this.plugin, task -> {
            this.spawnTask = task;
            if (!this.spawning) {
                task.cancel();
                this.spawnTask = null;
                return;
            }
            if (next[0] >= count) {
                task.cancel();
                this.spawnTask = null;
                this.spawning = false;
                this.rescanIfManual();
                this.plugin.broadcast(this.plugin.lang().fmt("spawn-all-done", "\u5047\u4eba\u751f\u6210\u5b8c\u6210\uff1a\u5171 {0} \u4e2a\uff08{1}\uff09", count, this.tabState()));
                return;
            }
            int n = next[0];
            next[0] = n + 1;
            int index = n;
            String name = config.botName(index);
            if (!this.bots.containsKey(name)) {
                this.spawnOne(name, index, base);
            }
        }, 1L, config.spawnIntervalTicks());
        return count;
    }

    private void spawnOne(String name, int index, Location anchor) {
        try {
            Player player;
            World world = anchor.getWorld();
            Location pos = this.plugin.stage().botPosition(index);
            if (pos == null) {
                pos = new Location(world, anchor.getX(), Math.floor(anchor.getY()), anchor.getZ());
            }
            float yaw = 0.0f;
            Location block = this.plugin.stage().blockOf(index, 12);
            if (block != null) {
                yaw = pos.clone().setDirection(block.toVector().subtract(pos.toVector())).getYaw();
            }
            if ((player = Nms.INSTANCE.spawnFakePlayer(name, FakePlayerManager.uuidOf(name), world, pos.getX(), pos.getY(), pos.getZ(), yaw, 0.0f)) == null) {
                this.plugin.getLogger().warning("\u5047\u4eba\u751f\u6210\u5931\u8d25: " + name);
                return;
            }
            player.setCollidable(this.plugin.config().botPushable());
            player.setGravity(this.plugin.config().botGravity());
            player.setInvulnerable(this.plugin.config().invulnerable());
            this.applyReach(player);
            this.bots.put(name, new FakeBot(name, index, player));
            if (!this.plugin.config().tablistShow()) {
                // display.tablist=false：立即从所有客户端 Tab 列表移除该假人
                Nms.INSTANCE.hideFromTablist(Collections.singletonList(player.getUniqueId()));
            }
            this.rescanIfManual();
        }
        catch (Exception ex) {
            this.plugin.getLogger().warning("\u5047\u4eba\u751f\u6210\u5f02\u5e38: " + name + " - " + ex.getMessage());
        }
    }

    public int despawnAll() {
        if (this.spawnTask != null) {
            this.spawnTask.cancel();
            this.spawnTask = null;
        }
        this.spawning = false;
        int removed = this.bots.size();
        for (FakeBot bot : new ArrayList<FakeBot>(this.bots.values())) {
            try {
                Nms.INSTANCE.removeFakePlayer(bot.player());
            }
            catch (Exception ex) {
                this.plugin.getLogger().warning("\u79fb\u9664\u5047\u4eba\u5f02\u5e38 " + bot.name() + ": " + ex.getMessage());
            }
        }
        this.bots.clear();
        if (this.plugin.config().stageEnabled()) {
            this.plugin.stage().clear();
        }
        return removed;
    }

    public void maintain() {
        for (FakeBot bot : new ArrayList<FakeBot>(this.bots.values())) {
            Player player = bot.player();
            if (player == null || !player.isOnline()) {
                this.bots.remove(bot.name());
                continue;
            }
            Nms.INSTANCE.maintainConnection(player);
            // 配置热更新：重力/推挤与手的交互距离（/notebot reload 后对在线假人生效）
            player.setCollidable(this.plugin.config().botPushable());
            player.setGravity(this.plugin.config().botGravity());
            this.applyReach(player);
        }
    }

    /** 设置假人手的交互距离（原版使用 BLOCK_INTERACTION_RANGE 属性做点击距离检查） */
    private void applyReach(Player player) {
        try {
            org.bukkit.attribute.AttributeInstance attribute = player.getAttribute(
                    org.bukkit.attribute.Attribute.BLOCK_INTERACTION_RANGE);
            if (attribute == null) {
                return;
            }
            double want = this.plugin.config().botReach();
            if (Math.abs(attribute.getBaseValue() - want) > 0.01) {
                attribute.setBaseValue(want);
            }
        }
        catch (Throwable throwable) {
            // 忽略：属性不可用时保持原版行为
        }
    }

    private static UUID uuidOf(String name) {
        return UUID.nameUUIDFromBytes(("Notebot:" + name).getBytes(StandardCharsets.UTF_8));
    }
}

