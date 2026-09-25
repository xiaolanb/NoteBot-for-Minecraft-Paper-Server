/*
 * Decompiled with CFR 0.152.
 */
package com.notebot;

import com.notebot.NotebotPlugin;
import com.notebot.nbs.NbsReader;
import com.notebot.nbs.NbsSong;
import java.io.File;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public final class SongLibrary {
    private final NotebotPlugin plugin;
    private final Map<String, NbsSong> songs = new TreeMap<String, NbsSong>(String.CASE_INSENSITIVE_ORDER);

    public SongLibrary(NotebotPlugin plugin) {
        this.plugin = plugin;
    }

    public File dir() {
        File dir = new File(this.plugin.getDataFolder(), "songs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public void reload() {
        this.songs.clear();
        File[] files = this.dir().listFiles((d, name) -> name.toLowerCase(Locale.ROOT).endsWith(".nbs"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                NbsSong song = NbsReader.read(file);
                this.songs.put(file.getName(), song);
                this.plugin.getLogger().info("\u5df2\u52a0\u8f7d\u66f2\u76ee " + file.getName() + "\uff08" + song.notes.size() + " \u97f3\u7b26, " + song.layers.size() + " \u97f3\u8f68, " + String.format("%.1f", song.ticksPerSecond) + " TPS\uff09");
            }
            catch (Exception ex) {
                this.plugin.getLogger().warning("NBS \u89e3\u6790\u5931\u8d25: " + file.getName() + " - " + ex.getMessage());
            }
        }
    }

    public NbsSong get(String name) {
        if (name == null) {
            return null;
        }
        NbsSong song = this.songs.get(name);
        if (song == null && !name.toLowerCase(Locale.ROOT).endsWith(".nbs")) {
            song = this.songs.get(name + ".nbs");
        }
        return song;
    }

    public Collection<NbsSong> all() {
        return this.songs.values();
    }

    public int size() {
        return this.songs.size();
    }
}

