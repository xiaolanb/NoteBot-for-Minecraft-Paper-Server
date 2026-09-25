/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 */
package com.notebot.gui;

import com.notebot.Assignments;
import com.notebot.NotebotPlugin;
import com.notebot.bot.FakePlayerManager;
import com.notebot.gui.AssignGui;
import com.notebot.gui.NotebotGui;
import com.notebot.gui.SongSelectGui;
import com.notebot.playback.NbsInstrument;
import com.notebot.playback.PlaybackManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MainGui
implements NotebotGui {
    private final NotebotPlugin plugin;
    private final Inventory inventory;

    public MainGui(NotebotPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)54, (String)plugin.config().guiTitle());
        this.rebuild();
    }

    private void rebuild() {
        this.inventory.clear();
        Assignments assignments = this.plugin.assignments();
        FakePlayerManager bots = this.plugin.fakePlayers();
        PlaybackManager playback = this.plugin.playback();
        for (NbsInstrument instrument : NbsInstrument.values()) {
            String bot = assignments.botFor(instrument.id());
            this.inventory.setItem(instrument.id(), NotebotGui.item(instrument.icon(), "\u00a7e" + instrument.display(), "\u00a77\u62c5\u5f53\u5047\u4eba: " + (String)(bot == null ? "\u00a78\u65e0" : "\u00a7a" + bot), "\u00a77\u58f0\u8c03: \u00a7f25 \u4e2a \u00a77(F#3 ~ F#5)", "\u00a77\u00a7o\u70b9\u51fb\u6253\u5f00\u5206\u914d\u83dc\u5355\uff08\u4e8c\u7ea7\u83dc\u5355\uff09"));
        }
        this.inventory.setItem(36, NotebotGui.item(Material.JUKEBOX, "\u00a7e\u266a \u9009\u62e9\u66f2\u76ee", "\u00a77\u5f53\u524d: " + (String)(this.plugin.selectedSong() != null ? "\u00a7f" + this.plugin.selectedSong() : "\u00a78\u672a\u9009\u62e9"), "\u00a77\u628a .nbs \u6587\u4ef6\u653e\u5165 plugins/Notebot/songs \u540e", "\u00a77\u6267\u884c /notebot reload \u5237\u65b0\u66f2\u76ee\u5e93"));
        this.inventory.setItem(38, NotebotGui.item(Material.LIME_WOOL, "\u00a7a\u25b6 \u64ad\u653e", "\u00a77\u5f00\u59cb\u6f14\u594f\u9009\u4e2d\u66f2\u76ee"));
        this.inventory.setItem(39, NotebotGui.item(Material.YELLOW_WOOL, "\u00a7e\u23f8 \u6682\u505c / \u7ee7\u7eed", new String[0]));
        this.inventory.setItem(40, NotebotGui.item(Material.RED_WOOL, "\u00a7c\u23f9 \u505c\u6b62", new String[0]));
        this.inventory.setItem(42, NotebotGui.item(playback.loop() ? Material.LEVER : Material.REDSTONE_TORCH, "\u00a7d\ud83d\udd01 \u5faa\u73af\u64ad\u653e: " + (playback.loop() ? "\u00a7a\u5f00" : "\u00a7c\u5173"), new String[0]));
        this.inventory.setItem(44, NotebotGui.item(Material.PLAYER_HEAD, "\u00a7a+ \u751f\u6210\u4e00\u4e2a\u5047\u4eba", "\u00a77\u6bcf\u6b21\u70b9\u51fb\u751f\u6210\u4e0b\u4e00\u4e2a\u5047\u4eba", "\u00a77\u5df2\u5728\u7ebf " + bots.count() + " / " + this.plugin.config().botCount()));
        this.inventory.setItem(45, NotebotGui.item(Material.SKELETON_SKULL, "\u00a7c- \u79fb\u9664\u5168\u90e8\u5047\u4eba", new String[0]));
        String state = playback.isPlaying() ? "\u00a7a\u25b6 \u64ad\u653e\u4e2d" : (playback.isPaused() ? "\u00a7e\u23f8 \u5df2\u6682\u505c" : "\u00a77\u25a0 \u7a7a\u95f2");
        this.inventory.setItem(49, NotebotGui.item(Material.CLOCK, "\u00a7f\u72b6\u6001", "\u00a77\u72b6\u6001: " + state, "\u00a77\u66f2\u76ee: \u00a7f" + (playback.songName() != null ? playback.songName() : "\u00a78-"), "\u00a77\u5047\u4eba: \u00a7f" + bots.count() + " / " + this.plugin.config().botCount(), "\u00a77\u97f3\u8272\u5206\u914d: \u00a7f" + assignments.assignedCount() + " / 16"));
        this.inventory.setItem(53, NotebotGui.item(Material.BARRIER, "\u00a7c\u2715 \u5173\u95ed", new String[0]));
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < 16) {
            player.openInventory(new AssignGui(this.plugin, slot).getInventory());
            return;
        }
        switch (slot) {
            case 36: {
                player.openInventory(new SongSelectGui(this.plugin, 0).getInventory());
                break;
            }
            case 38: {
                if (this.plugin.selectedSong() == null) {
                    player.openInventory(new SongSelectGui(this.plugin, 0).getInventory());
                    return;
                }
                if (!this.plugin.playback().play(this.plugin.selectedSong(), player)) {
                    player.sendMessage(this.plugin.config().prefix() + "\u00a7c\u66f2\u76ee\u4e0d\u5b58\u5728\uff0c\u8bf7\u91cd\u65b0\u9009\u62e9");
                    return;
                }
                this.reopen(player);
                break;
            }
            case 39: {
                PlaybackManager playback = this.plugin.playback();
                if (playback.isPlaying()) {
                    playback.pause();
                    this.plugin.send((CommandSender)player, "\u00a7e\u5df2\u6682\u505c");
                } else if (playback.isPaused()) {
                    playback.resume();
                    this.plugin.send((CommandSender)player, "\u00a7a\u5df2\u7ee7\u7eed");
                } else {
                    player.sendMessage(this.plugin.config().prefix() + "\u00a7e\u5f53\u524d\u6ca1\u6709\u6b63\u5728\u64ad\u653e\u7684\u66f2\u76ee");
                    return;
                }
                this.reopen(player);
                break;
            }
            case 40: {
                this.plugin.playback().stop();
                this.reopen(player);
                break;
            }
            case 42: {
                this.plugin.playback().toggleLoop();
                this.plugin.send((CommandSender)player, "\u5faa\u73af\u64ad\u653e: " + (this.plugin.playback().loop() ? "\u00a7a\u5f00" : "\u00a7c\u5173"));
                this.reopen(player);
                break;
            }
            case 44: {
                String spawned = this.plugin.fakePlayers().spawnNext(player.getLocation());
                if (spawned != null) {
                    this.plugin.send((CommandSender)player, "\u00a7a\u5df2\u751f\u6210 \u00a7e" + spawned + "\u00a7a\uff08" + this.plugin.fakePlayers().count() + " / " + this.plugin.config().botCount() + "\uff09");
                } else {
                    this.plugin.send((CommandSender)player, "\u00a7e16 \u4e2a\u5047\u4eba\u5df2\u5168\u90e8\u5728\u7ebf");
                }
                this.reopen(player);
                break;
            }
            case 45: {
                int removed = this.plugin.fakePlayers().despawnAll();
                this.plugin.send((CommandSender)player, "\u00a7c\u5df2\u79fb\u9664 " + removed + " \u4e2a\u5047\u4eba");
                this.reopen(player);
                break;
            }
            case 53: {
                player.closeInventory();
                break;
            }
        }
    }

    private void reopen(Player player) {
        player.openInventory(new MainGui(this.plugin).getInventory());
    }
}

