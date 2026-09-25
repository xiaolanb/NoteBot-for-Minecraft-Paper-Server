/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Note
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.data.BlockData
 *  org.bukkit.block.data.type.NoteBlock
 */
package com.notebot;

import com.notebot.Config;
import com.notebot.Nms;
import com.notebot.NotebotPlugin;
import com.notebot.bot.FakeBot;
import com.notebot.playback.NbsInstrument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Player;

public final class StageManager {
    private static final int PLATFORM = 5;
    private final NotebotPlugin plugin;
    private final Map<Location, BlockData> originals = new HashMap<Location, BlockData>();
    private final Map<Long, Location> noteBlocks = new HashMap<Long, Location>();
    private final Map<Long, Location> detected = new HashMap<Long, Location>();
    private final Map<Integer, Location> botPositions = new HashMap<Integer, Location>();
    private Location center;
    private boolean built;

    public StageManager(NotebotPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isBuilt() {
        return this.built;
    }

    public Location center() {
        return this.center != null ? this.center.clone() : null;
    }

    public Location blockOf(int instrument, int pitch) {
        Location loc = this.noteBlocks.get(StageManager.key(instrument, pitch));
        if (loc == null) {
            loc = this.detected.get(StageManager.key(instrument, pitch));
        }
        return loc;
    }

    public String rescan(List<FakeBot> bots) {
        this.detected.clear();
        if (bots == null || bots.isEmpty()) {
            return "";
        }
        ArrayList<Location> positions = new ArrayList<Location>();
        for (FakeBot bot : bots) {
            Player player = bot.player();
            if (player == null || !player.isOnline()) continue;
            positions.add(player.getLocation());
        }
        if (positions.isEmpty()) {
            return "";
        }
        World world = positions.get(0).getWorld();
        if (world == null) {
            return "";
        }
        // 自动配对担当：读取脚底音符盒方块状态的 instrument 值（右键发声实际使用的
        // 音色），把该音色改派给站在它上面的假人（bot.auto-assign: false 可关闭）。
        // 已被分配给在线假人的音色不抢（GUI 分配优先），因此一个假人可担当多个音色。
        int reassigned = 0;
        if (this.plugin.config().autoAssign()) {
            Map<Integer, String> auto = new LinkedHashMap<Integer, String>();
            for (FakeBot bot : bots) {
                Player player = bot.player();
                if (player == null || !player.isOnline()) continue;
                Location pos = player.getLocation();
                if (pos.getWorld() == null || !pos.getWorld().equals((Object)world)) continue;
                int x = pos.getBlockX();
                int z = pos.getBlockZ();
                for (int y = pos.getBlockY() - 1; y >= pos.getBlockY() - 2; --y) {
                    if (world.getBlockAt(x, y, z).getType() != Material.NOTE_BLOCK) continue;
                    NbsInstrument instrument = NbsInstrument.byBukkitInstrument(((NoteBlock)world.getBlockAt(x, y, z).getBlockData()).getInstrument());
                    if (instrument != null) {
                        auto.putIfAbsent(instrument.id(), bot.name());
                    }
                    break;
                }
            }
            if (!auto.isEmpty()) {
                java.util.HashSet<String> onlineNames = new java.util.HashSet<String>();
                for (FakeBot bot : bots) {
                    Player player = bot.player();
                    if (player != null && player.isOnline()) {
                        onlineNames.add(bot.name());
                    }
                }
                reassigned = this.plugin.assignments().applyAuto(auto, onlineNames);
                if (reassigned > 0) {
                    this.plugin.getLogger().info("\u5df2\u6309\u811a\u5e95\u97f3\u7b26\u76d2\u81ea\u52a8\u5339\u914d\u62c5\u5f53\uff08" + reassigned + " \u4e2a\u97f3\u8272\uff09");
                }
            }
        }
        if (this.plugin.config().scanMode() == Config.ScanMode.BELOW) {
            for (Location pos : positions) {
                int x = pos.getBlockX();
                int z = pos.getBlockZ();
                for (int y = pos.getBlockY() - 1; y >= pos.getBlockY() - 2; --y) {
                    String name;
                    NbsInstrument instrument;
                    if (world.getBlockAt(x, y, z).getType() != Material.NOTE_BLOCK || (instrument = NbsInstrument.byVanillaName(name = Nms.INSTANCE != null ? Nms.INSTANCE.instrumentNameAt(world, x, y, z) : null)) == null) continue;
                    Location center = new Location(world, (double)x + 0.5, (double)y + 0.5, (double)z + 0.5);
                    for (int pitch = 0; pitch < 25; ++pitch) {
                        this.detected.putIfAbsent(StageManager.key(instrument.id(), pitch), center);
                    }
                }
            }
            return this.summary(reassigned);
        }
        int radius = this.plugin.config().scanRadius();
        int vertical = Math.max(2, radius / 2);
        for (Location pos : positions) {
            int cx = pos.getBlockX();
            int cy = pos.getBlockY();
            int cz = pos.getBlockZ();
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    for (int dy = -vertical; dy <= vertical; ++dy) {
                        String name;
                        NbsInstrument instrument;
                        int x = cx + dx;
                        int y = cy + dy;
                        int z = cz + dz;
                        if (world.getBlockAt(x, y, z).getType() != Material.NOTE_BLOCK || (instrument = NbsInstrument.byVanillaName(name = Nms.INSTANCE != null ? Nms.INSTANCE.instrumentNameAt(world, x, y, z) : null)) == null) continue;
                        NoteBlock data = (NoteBlock)world.getBlockAt(x, y, z).getBlockData();
                        byte pitch = data.getNote().getId();
                        Location center = new Location(world, (double)x + 0.5, (double)y + 0.5, (double)z + 0.5);
                        this.detected.putIfAbsent(StageManager.key(instrument.id(), pitch), center);
                    }
                }
            }
        }
        return this.summary(reassigned);
    }

    private String summary(int reassigned) {
        if (this.detected.isEmpty()) {
            return "";
        }
        int[] counts = new int[16];
        for (long key : this.detected.keySet()) {
            int n = (int)(key / 100L);
            counts[n] = counts[n] + 1;
        }
        StringBuilder sb = new StringBuilder("\u68c0\u6d4b\u5230 ").append(this.detected.size()).append(" \u4e2a\u97f3\u7b26\u76d2\uff1a");
        for (int i = 0; i < counts.length; ++i) {
            if (counts[i] <= 0) continue;
            sb.append("\u00a7e").append(NbsInstrument.byId(i).display()).append("\u00a7r\u00d7").append(counts[i]).append("  ");
        }
        String summary = sb.toString().trim();
        if (reassigned > 0) {
            summary = summary + " \u00a77\uff08\u5df2\u81ea\u52a8\u5339\u914d\u62c5\u5f53 " + reassigned + " \u4e2a\u97f3\u8272\uff09\u00a7r";
        }
        this.plugin.getLogger().info("\u624b\u52a8\u6a21\u5f0f\uff1a" + summary);
        return summary;
    }

    public Location botPosition(int instrument) {
        Location pos = this.botPositions.get(instrument);
        return pos != null ? pos.clone() : null;
    }

    public boolean build(Location center) {
        this.clear();
        if (center == null || center.getWorld() == null) {
            return false;
        }
        this.center = center.clone();
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int spacing = this.plugin.config().platformSpacing();
        int gridOffset = 3 * spacing / 2;
        for (int inst = 0; inst < 16; ++inst) {
            int ox = cx + inst % 4 * spacing - gridOffset;
            int oz = cz + inst / 4 * spacing - gridOffset;
            int pitch = 0;
            for (int x = 0; x < 5; ++x) {
                for (int z = 0; z < 5; ++z) {
                    // 基座在 cy-1，音符盒在 cy：5×5 共 25 个声调，无中央空洞
                    this.placeNoteBlock(world, ox + x, cy - 1, oz + z, inst, pitch);
                    ++pitch;
                }
            }
            // 假人站在正中央音符盒的顶面上
            this.botPositions.put(inst, new Location(world, (double)(ox + 2) + 0.5, (double)(cy + 1), (double)(oz + 2) + 0.5));
        }
        this.built = true;
        this.plugin.getLogger().info("\u97f3\u7b26\u76d2\u821e\u53f0\u5df2\u642d\u5efa\u4e8e " + world.getName() + " (" + cx + ", " + cy + ", " + cz + ")\uff0c16 \u7ec4 \u00d7 25 \u97f3\u7b26\u76d2");
        return true;
    }

    public void clear() {
        for (Map.Entry<Location, BlockData> entry : this.originals.entrySet()) {
            entry.getKey().getBlock().setBlockData(entry.getValue(), false);
        }
        this.originals.clear();
        this.noteBlocks.clear();
        this.detected.clear();
        this.botPositions.clear();
        this.built = false;
    }

    private void placeNoteBlock(World world, int x, int y, int z, int instrument, int pitch) {
        NbsInstrument inst = NbsInstrument.byId(instrument);
        Location baseLoc = new Location(world, (double)x, (double)y, (double)z);
        Location noteLoc = new Location(world, (double)x, (double)(y + 1), (double)z);
        this.saveOriginal(baseLoc);
        this.saveOriginal(noteLoc);
        baseLoc.getBlock().setType(inst.baseBlock(), false);
        Block block = noteLoc.getBlock();
        block.setType(Material.NOTE_BLOCK, false);
        NoteBlock data = (NoteBlock)block.getBlockData();
        data.setNote(new Note(pitch));
        block.setBlockData((BlockData)data, false);
        // 立即把音色属性写成基座对应的音色（并刷新给客户端），否则全是 harp
        if (Nms.INSTANCE != null) {
            Nms.INSTANCE.setNoteBlockInstrument(world, x, y + 1, z, inst.name());
        }
        this.noteBlocks.put(StageManager.key(instrument, pitch), noteLoc.clone().add(0.5, 0.5, 0.5));
    }

    private void saveOriginal(Location loc) {
        this.originals.putIfAbsent(loc.clone(), loc.getBlock().getBlockData());
    }

    private static long key(int instrument, int pitch) {
        return (long)instrument * 100L + (long)pitch;
    }
}

