/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.PluginCommand
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.notebot;

import com.notebot.Assignments;
import com.notebot.Config;
import com.notebot.Lang;
import com.notebot.Nms;
import com.notebot.SongLibrary;
import com.notebot.StageManager;
import com.notebot.bot.FakePlayerManager;
import com.notebot.command.NotebotCommand;
import com.notebot.gui.GuiListener;
import com.notebot.playback.PlaybackManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class NotebotPlugin
extends JavaPlugin {
    private Config config;
    private SongLibrary library;
    private Assignments assignments;
    private StageManager stage;
    private FakePlayerManager fakePlayers;
    private PlaybackManager playback;
    private Lang lang;
    private String selectedSong;
    private BukkitTask maintenanceTask;

    public void onEnable() {
        this.saveDefaultConfig();
        this.config = new Config(this);
        this.library = new SongLibrary(this);
        this.library.reload();
        this.assignments = new Assignments(this);
        this.assignments.load();
        Nms.init(this);
        this.lang = new Lang(this);
        this.lang.load();
        this.stage = new StageManager(this);
        this.fakePlayers = new FakePlayerManager(this);
        this.playback = new PlaybackManager(this);
        NotebotCommand command = new NotebotCommand(this);
        PluginCommand pluginCommand = this.getCommand("notebot");
        if (pluginCommand != null) {
            pluginCommand.setExecutor((CommandExecutor)command);
            pluginCommand.setTabCompleter((TabCompleter)command);
        }
        this.getServer().getPluginManager().registerEvents((Listener)new GuiListener(), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new BotDisplayListener(this), (Plugin)this);
        this.maintenanceTask = this.getServer().getScheduler().runTaskTimer((Plugin)this, () -> this.fakePlayers.maintain(), 20L, 20L);
        this.getLogger().info("Notebot \u5df2\u542f\u7528\u3002");
        this.getLogger().info("\u66f2\u76ee\u76ee\u5f55: " + this.library.dir().getAbsolutePath());
        this.getLogger().info("\u4f7f\u7528 /notebot gui \u914d\u7f6e\u97f3\u8272\u5206\u62c5\u5e76\u64ad\u653e\uff0c\u6216 /notebot play <\u66f2\u76ee>\u3002");
    }

    public void onDisable() {
        if (this.maintenanceTask != null) {
            this.maintenanceTask.cancel();
            this.maintenanceTask = null;
        }
        if (this.playback != null) {
            this.playback.stopSilent();
        }
        if (this.fakePlayers != null) {
            this.fakePlayers.despawnAll();
        }
        if (this.assignments != null) {
            this.assignments.save();
        }
        this.getLogger().info("Notebot \u5df2\u5378\u8f7d\u3002");
    }

    public void reloadAll() {
        this.reloadConfig();
        this.config = new Config(this);
        this.library.reload();
        this.assignments.load();
        this.lang.load();
        if (this.playback != null) {
            this.playback.setLoop(this.config.loop());
        }
    }

    public Config config() {
        return this.config;
    }

    public SongLibrary library() {
        return this.library;
    }

    public Assignments assignments() {
        return this.assignments;
    }

    public StageManager stage() {
        return this.stage;
    }

    public Lang lang() {
        return this.lang;
    }

    public FakePlayerManager fakePlayers() {
        return this.fakePlayers;
    }

    public PlaybackManager playback() {
        return this.playback;
    }

    public String selectedSong() {
        return this.selectedSong;
    }

    public void selectSong(String fileName) {
        this.selectedSong = fileName;
    }

    public void send(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(this.lang.prefix() + message);
        }
    }

    public void broadcast(String message) {
        Bukkit.broadcastMessage((String)(this.lang.prefix() + message));
    }
}

