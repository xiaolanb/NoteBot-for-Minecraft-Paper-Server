/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package com.notebot;

import com.notebot.NotebotPlugin;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Assignments {
    private final NotebotPlugin plugin;
    private final Map<Integer, String> map = new LinkedHashMap<Integer, String>();

    public Assignments(NotebotPlugin plugin) {
        this.plugin = plugin;
    }

    public String botFor(int instrument) {
        return this.map.get(instrument);
    }

    public int assignedCount() {
        return this.map.size();
    }

    public void assign(int instrument, String botName) {
        if (botName == null) {
            this.map.remove(instrument);
        } else {
            this.map.put(instrument, botName);
        }
        this.save();
    }

    public Map<Integer, String> snapshot() {
        return new LinkedHashMap<Integer, String>(this.map);
    }

    /**
     * 自动匹配：按脚底音符盒的检测结果批量改派音色给假人（只写一次盘）。
     * 已被分配给“在线假人”的音色不会被抢（GUI 分配优先），因此一个假人
     * 可以同时担当多个音色；只自动填补无分配、或原担当假人已离线的音色。
     *
     * @return 实际改动的音色数量
     */
    public int applyAuto(Map<Integer, String> mapping, java.util.Collection<String> onlineNames) {
        int changed = 0;
        for (Map.Entry<Integer, String> entry : mapping.entrySet()) {
            String botName = entry.getValue();
            if (botName == null) {
                continue;
            }
            String current = this.map.get(entry.getKey());
            if (current != null && onlineNames != null && onlineNames.contains(current)) {
                continue; // 在线假人的已有分配不抢
            }
            if (!botName.equals(current)) {
                this.map.put(entry.getKey(), botName);
                ++changed;
            }
        }
        if (changed > 0) {
            this.save();
        }
        return changed;
    }

    public void setDefaults() {
        this.map.clear();
        for (int i = 0; i < 16; ++i) {
            this.map.put(i, this.plugin.config().botName(i));
        }
        this.save();
    }

    public void load() {
        File file = new File(this.plugin.getDataFolder(), "assignments.yml");
        this.map.clear();
        if (file.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration((File)file);
            ConfigurationSection section = yaml.getConfigurationSection("assignments");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    try {
                        int instrument = Integer.parseInt(key);
                        String botName = section.getString(key);
                        if (instrument < 0 || instrument > 15 || botName == null) continue;
                        this.map.put(instrument, botName);
                    }
                    catch (NumberFormatException numberFormatException) {}
                }
            }
            if (this.map.isEmpty()) {
                this.setDefaults();
            }
        } else {
            this.setDefaults();
        }
    }

    public void save() {
        File file = new File(this.plugin.getDataFolder(), "assignments.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<Integer, String> entry : this.map.entrySet()) {
            yaml.set("assignments." + String.valueOf(entry.getKey()), (Object)entry.getValue());
        }
        try {
            yaml.save(file);
        }
        catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "\u65e0\u6cd5\u4fdd\u5b58\u97f3\u8272\u5206\u914d assignments.yml", ex);
        }
    }
}

