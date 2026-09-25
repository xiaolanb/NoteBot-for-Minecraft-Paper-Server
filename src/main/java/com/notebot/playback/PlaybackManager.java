/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Note
 *  org.bukkit.SoundCategory
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.data.BlockData
 *  org.bukkit.block.data.type.NoteBlock
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.notebot.playback;

import com.notebot.Config;
import com.notebot.Lang;
import com.notebot.NotebotPlugin;
import com.notebot.bot.FakeBot;
import com.notebot.nbs.NbsNote;
import com.notebot.nbs.NbsSong;
import com.notebot.playback.NbsInstrument;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class PlaybackManager {
    private final NotebotPlugin plugin;
    private BukkitTask task;
    private NbsSong song;
    private String songName;
    private int noteIndex;
    private double tickPos;
    private long lastMs;
    private boolean paused;
    private boolean loop;
    private UUID conductorId;
    private boolean warnedNoStage;
    private BukkitTask rescanTask;

    public PlaybackManager(NotebotPlugin plugin) {
        this.plugin = plugin;
        this.loop = plugin.config().loop();
    }

    public boolean isPlaying() {
        return this.task != null && this.song != null && !this.paused;
    }

    public boolean isPaused() {
        return this.task != null && this.song != null && this.paused;
    }

    public boolean loop() {
        return this.loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void toggleLoop() {
        this.loop = !this.loop;
    }

    public String songName() {
        return this.songName;
    }

    public boolean play(String name, Player conductor) {
        NbsSong newSong = this.plugin.library().get(name);
        if (newSong == null) {
            return false;
        }
        this.stopInternal(true);
        Config config = this.plugin.config();
        if (config.autoSpawn() && this.plugin.fakePlayers().count() < config.botCount()) {
            Location anchor = conductor != null ? conductor.getLocation()
                    : (!Bukkit.getWorlds().isEmpty() ? ((World)Bukkit.getWorlds().get(0)).getSpawnLocation() : null);
            if (anchor != null) {
                this.plugin.fakePlayers().spawnAll(anchor);
            }
        }
        if (!config.stageEnabled()) {
            this.plugin.fakePlayers().rescanIfManual();
            if (this.rescanTask != null) {
                this.rescanTask.cancel();
                this.rescanTask = null;
            }
            if (config.autoRescanTicks() > 0L) {
                this.rescanTask = this.plugin.getServer().getScheduler().runTaskTimer((Plugin)this.plugin, () -> this.plugin.fakePlayers().rescanIfManual(), config.autoRescanTicks(), config.autoRescanTicks());
            }
        }
        this.song = newSong;
        this.songName = newSong.fileName;
        this.noteIndex = 0;
        this.tickPos = 0.0;
        this.paused = false;
        this.lastMs = System.currentTimeMillis();
        this.conductorId = conductor != null ? conductor.getUniqueId() : null;
        this.warnedNoStage = false;
        this.plugin.send((CommandSender)conductor, this.plugin.lang().fmt("play-start", "\u5f00\u59cb\u6f14\u594f \u00a7e{0}\u00a7r\uff08{1} \u4e2a\u97f3\u7b26, {2} TPS\uff09", newSong.displayName(), newSong.notes.size(), String.format("%.1f", newSong.ticksPerSecond)) + (this.loop ? Lang.color(", \u00a7d\u5faa\u73af\u00a7r") : ""));
        this.task = this.plugin.getServer().getScheduler().runTaskTimer((Plugin)this.plugin, this::tick, 1L, 1L);
        return true;
    }

    public boolean pause() {
        if (!this.isPlaying()) {
            return false;
        }
        this.paused = true;
        return true;
    }

    public boolean resume() {
        if (!this.isPaused()) {
            return false;
        }
        this.paused = false;
        this.lastMs = System.currentTimeMillis();
        return true;
    }

    public void stop() {
        this.stopInternal(false);
    }

    public void stopSilent() {
        this.stopInternal(true);
    }

    private void stopInternal(boolean silent) {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        if (this.rescanTask != null) {
            this.rescanTask.cancel();
            this.rescanTask = null;
        }
        if (this.song != null && !silent) {
            this.plugin.broadcast(this.plugin.lang().fmt("play-stopped", "\u5df2\u505c\u6b62\u6f14\u594f \u00a7e{0}", this.song.displayName()));
        }
        this.song = null;
        this.songName = null;
        this.paused = false;
        this.noteIndex = 0;
        this.tickPos = 0.0;
    }

    private void tick() {
        if (this.song == null) {
            this.stopInternal(true);
            return;
        }
        if (this.paused) {
            this.lastMs = System.currentTimeMillis();
            return;
        }
        long now = System.currentTimeMillis();
        this.tickPos += (double)(now - this.lastMs) * this.song.ticksPerSecond / 1000.0;
        this.lastMs = now;
        int currentTick = (int)this.tickPos;
        while (this.noteIndex < this.song.notes.size()) {
            NbsNote note = this.song.notes.get(this.noteIndex);
            if (note.tick > currentTick) break;
            this.playNote(note);
            ++this.noteIndex;
        }
        if (this.noteIndex >= this.song.notes.size() && currentTick >= this.song.lastTick + 8) {
            String finished = this.song.displayName();
            if (this.loop) {
                this.noteIndex = 0;
                this.tickPos = 0.0;
                this.lastMs = now;
                this.plugin.broadcast(this.plugin.lang().fmt("loop-replay", "\u00a7d\u5faa\u73af\u91cd\u64ad \u00a7e{0}", finished));
            } else {
                this.stopInternal(true);
                this.plugin.broadcast(this.plugin.lang().fmt("play-ended", "\u6f14\u594f\u7ed3\u675f \u00a7e{0}", finished));
            }
        }
    }

    private void playNote(NbsNote note) {
        int tone;
        if (note.instrument < 0 || note.instrument >= 16) {
            return;
        }
        NbsInstrument instrument = NbsInstrument.byId(note.instrument);
        String botName = this.plugin.assignments().botFor(instrument.id());
        if (botName == null) {
            return;
        }
        FakeBot bot = this.plugin.fakePlayers().get(botName);
        if (bot == null || bot.player() == null) {
            return;
        }
        if (note.key < 33 || note.key > 57) {
            if (!this.plugin.config().roundOutOfRange()) {
                return;
            }
            tone = Math.min(24, Math.max(0, note.key - 33));
        } else {
            tone = note.key - 33;
        }
        Config.SoundMode mode = this.plugin.config().soundMode();
        Location block = null;
        if (mode != Config.SoundMode.SIMULATE) {
            block = this.plugin.stage().blockOf(instrument.id(), tone);
        }
        if (block != null) {
            Block bukkitBlock = block.getBlock();
            NoteBlock data = (NoteBlock)bukkitBlock.getBlockData();
            int preTone = (tone + 24) % 25;
            if (data.getNote().getId() != preTone) {
                data.setNote(new Note(preTone));
                bukkitBlock.setBlockData((BlockData)data, false);
            }
            bot.clickBlock(block);
            return;
        }
        if (mode == Config.SoundMode.CLICK) {
            return;
        }
        if (!this.plugin.config().soundFallback()) {
            return;
        }
        if (!this.warnedNoStage) {
            this.warnedNoStage = true;
            this.plugin.broadcast(this.plugin.lang().get("fallback-warn", "\u00a7e\u97f3\u7b26\u76d2\u821e\u53f0\u672a\u642d\u5efa\uff0c\u5df2\u4f7f\u7528\u58f0\u97f3\u56de\u9000\u6a21\u5f0f\uff08\u65e0\u97f3\u7b26\u7c92\u5b50\uff1b\u53ef\u7528 /notebot stage build \u624b\u52a8\u642d\u53f0\uff09"));
        }
        float volume = (float)Math.min(1.0, (double)note.velocity / 100.0);
        float pitch = (float)Math.pow(2.0, (double)(note.key - 45) / 12.0);
        bot.player().getWorld().playSound(bot.player().getLocation(), instrument.sound(), SoundCategory.MASTER, volume, pitch);
        bot.swing();
    }
}

