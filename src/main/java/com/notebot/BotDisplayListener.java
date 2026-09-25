package com.notebot;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.notebot.bot.FakeBot;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.List;
import java.util.UUID;

/**
 * 假人显示控制：
 * 1) Tab 玩家列表显示（display.tablist）
 * 2) 服务器列表 MOTD 玩家样本（display.motd，兼容 ProxyOnlineLinker 的 Redis 模式：
 *    KEEP 时完全不改动列表，只可能按 motd-count-bots 调整人数）
 * 3) 假人加入/退出提示（display.join-quit-message，兼容 CustomJoinMessages 等接管提示的插件）
 */
public final class BotDisplayListener implements Listener {

    private final NotebotPlugin plugin;
    private boolean motdWarned;

    public BotDisplayListener(NotebotPlugin plugin) {
        this.plugin = plugin;
    }

    /** 新玩家进入后会收到完整的玩家列表，因此需要延迟重新隐藏假人 */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (this.plugin.config().tablistShow()) {
            return;
        }
        Bukkit.getScheduler().runTaskLater((org.bukkit.plugin.Plugin)this.plugin, this::hideBots, 20L);
    }

    /** 假人加入提示：AUTO=交给服务端/其它插件；ON=本插件发送；OFF=不显示 */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBotJoin(PlayerJoinEvent event) {
        if (!this.plugin.fakePlayers().isBotName(event.getPlayer().getName())) {
            return;
        }
        Config.JoinMessageMode mode = this.plugin.config().joinMessageMode();
        if (mode == Config.JoinMessageMode.AUTO) {
            return;
        }
        event.joinMessage((Component)null);
        event.setJoinMessage(null);
        if (mode == Config.JoinMessageMode.ON) {
            Bukkit.broadcastMessage(this.plugin.lang().fmt("bot-join", "&e{0} &7加入了游戏", event.getPlayer().getName()));
        }
    }

    /** 假人退出提示，规则同上 */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBotQuit(PlayerQuitEvent event) {
        if (!this.plugin.fakePlayers().isBotName(event.getPlayer().getName())) {
            return;
        }
        Config.JoinMessageMode mode = this.plugin.config().joinMessageMode();
        if (mode == Config.JoinMessageMode.AUTO) {
            return;
        }
        event.quitMessage((Component)null);
        event.setQuitMessage(null);
        if (mode == Config.JoinMessageMode.ON) {
            Bukkit.broadcastMessage(this.plugin.lang().fmt("bot-quit", "&e{0} &7离开了游戏", event.getPlayer().getName()));
        }
    }

    /** MOTD 玩家样本（假人也是真实在线玩家，默认会出现在样本里） */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(ServerListPingEvent event) {
        if (!(event instanceof PaperServerListPingEvent)) {
            return;
        }
        PaperServerListPingEvent ping = (PaperServerListPingEvent)event;
        Config.MotdMode mode = this.plugin.config().motdMode();
        boolean count = this.plugin.config().motdCountBots();
        List<FakeBot> online = this.plugin.fakePlayers().onlineBots();
        if (mode == Config.MotdMode.KEEP && count) {
            return;
        }
        try {
            List<PaperServerListPingEvent.ListedPlayerInfo> listed = ping.getListedPlayers();
            if (mode == Config.MotdMode.HIDDEN) {
                List<String> names = this.plugin.fakePlayers().allNames();
                listed.removeIf(info -> names.contains(info.name()));
                if (listed.isEmpty()) {
                    for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                        if (!names.contains(player.getName())) {
                            listed.add(new PaperServerListPingEvent.ListedPlayerInfo(player.getName(), player.getUniqueId()));
                        }
                    }
                }
            } else if (mode == Config.MotdMode.BOTS_ONLY) {
                listed.clear();
                for (FakeBot bot : online) {
                    listed.add(new PaperServerListPingEvent.ListedPlayerInfo(bot.name(), bot.uuid()));
                }
            }
        }
        catch (Throwable t) {
            if (!this.motdWarned) {
                this.motdWarned = true;
                this.plugin.getLogger().warning("\u4fee\u6539 MOTD \u73a9\u5bb6\u5217\u8868\u5931\u8d25\uff08\u5f53\u524d\u670d\u52a1\u7aef\u4e0d\u652f\u6301\uff09: " + t);
            }
        }
        if (!count && !online.isEmpty()) {
            ping.setNumPlayers(Math.max(0, ping.getNumPlayers() - online.size()));
        } else if (count && mode == Config.MotdMode.BOTS_ONLY) {
            ping.setNumPlayers(online.size());
        }
    }

    private void hideBots() {
        List<UUID> uuids = this.plugin.fakePlayers().onlineUuids();
        if (!uuids.isEmpty()) {
            Nms.INSTANCE.hideFromTablist(uuids);
        }
    }
}
