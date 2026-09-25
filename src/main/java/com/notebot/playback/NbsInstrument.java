/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 */
package com.notebot.playback;

import java.util.Locale;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Sound;

public enum NbsInstrument {
    HARP(0, "\u7ad6\u7434 HARP", Sound.BLOCK_NOTE_BLOCK_HARP, Material.GRASS_BLOCK, Material.GRASS_BLOCK),
    BASS(1, "\u8d1d\u65af BASS", Sound.BLOCK_NOTE_BLOCK_BASS, Material.OAK_PLANKS, Material.OAK_PLANKS),
    BASEDRUM(2, "\u5927\u9f13 BASEDRUM", Sound.BLOCK_NOTE_BLOCK_BASEDRUM, Material.STONE, Material.STONE),
    SNARE(3, "\u519b\u9f13 SNARE", Sound.BLOCK_NOTE_BLOCK_SNARE, Material.SAND, Material.SAND),
    HAT(4, "\u8e29\u9572 HAT", Sound.BLOCK_NOTE_BLOCK_HAT, Material.GLASS, Material.GLASS),
    GUITAR(5, "\u5409\u4ed6 GUITAR", Sound.BLOCK_NOTE_BLOCK_GUITAR, Material.WHITE_WOOL, Material.WHITE_WOOL),
    FLUTE(6, "\u957f\u7b1b FLUTE", Sound.BLOCK_NOTE_BLOCK_FLUTE, Material.CLAY, Material.CLAY),
    BELL(7, "\u94c3\u94db BELL", Sound.BLOCK_NOTE_BLOCK_BELL, Material.GOLD_BLOCK, Material.GOLD_BLOCK),
    CHIME(8, "\u7ba1\u949f CHIME", Sound.BLOCK_NOTE_BLOCK_CHIME, Material.PACKED_ICE, Material.PACKED_ICE),
    XYLOPHONE(9, "\u6728\u7434 XYLOPHONE", Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, Material.BONE_BLOCK, Material.BONE_BLOCK),
    IRON_XYLOPHONE(10, "\u94c1\u6728\u7434 IRON_XYLOPHONE", Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, Material.IRON_BLOCK, Material.IRON_BLOCK),
    COW_BELL(11, "\u725b\u94c3 COW_BELL", Sound.BLOCK_NOTE_BLOCK_COW_BELL, Material.SOUL_SAND, Material.SOUL_SAND),
    DIDGERIDOO(12, "\u8fea\u5409\u91cc\u675c\u7ba1 DIDGERIDOO", Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, Material.PUMPKIN, Material.PUMPKIN),
    BIT(13, "\u82af\u7247\u97f3 BIT", Sound.BLOCK_NOTE_BLOCK_BIT, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK),
    BANJO(14, "\u73ed\u5353\u7434 BANJO", Sound.BLOCK_NOTE_BLOCK_BANJO, Material.HAY_BLOCK, Material.HAY_BLOCK),
    PLING(15, "\u7535\u5b50\u97f3 PLING", Sound.BLOCK_NOTE_BLOCK_PLING, Material.GLOWSTONE, Material.GLOWSTONE);

    public static final int COUNT = 16;
    public static final int PITCHES = 25;
    private final int id;
    private final String display;
    private final Sound sound;
    private final Material icon;
    private final Material baseBlock;

    private NbsInstrument(int id, String display, Sound sound, Material icon, Material baseBlock) {
        this.id = id;
        this.display = display;
        this.sound = sound;
        this.icon = icon;
        this.baseBlock = baseBlock;
    }

    public int id() {
        return this.id;
    }

    public String display() {
        return this.display;
    }

    public Sound sound() {
        return this.sound;
    }

    public Material icon() {
        return this.icon;
    }

    public Material baseBlock() {
        return this.baseBlock;
    }

    public static NbsInstrument byId(int id) {
        for (NbsInstrument instrument : NbsInstrument.values()) {
            if (instrument.id != id) continue;
            return instrument;
        }
        return null;
    }

    public static NbsInstrument parse(String text) {
        if (text == null) {
            return null;
        }
        try {
            return NbsInstrument.byId(Integer.parseInt(text));
        }
        catch (NumberFormatException numberFormatException) {
            String lower = text.toLowerCase(Locale.ROOT);
            for (NbsInstrument instrument : NbsInstrument.values()) {
                if (!instrument.name().toLowerCase(Locale.ROOT).startsWith(lower) && !instrument.display().toLowerCase(Locale.ROOT).contains(lower)) continue;
                return instrument;
            }
            return null;
        }
    }

    public static NbsInstrument byVanillaName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return NbsInstrument.valueOf(name);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 按音符盒方块状态里的音色值查找（这是右键发声实际使用的音色） */
    public static NbsInstrument byBukkitInstrument(Instrument instrument) {
        if (instrument == null) {
            return null;
        }
        switch (instrument) {
            case PIANO: {
                return HARP;
            }
            case BASS_GUITAR: {
                return BASS;
            }
            case BASS_DRUM: {
                return BASEDRUM;
            }
            case SNARE_DRUM: {
                return SNARE;
            }
            case STICKS: {
                return HAT;
            }
            case GUITAR: {
                return GUITAR;
            }
            case FLUTE: {
                return FLUTE;
            }
            case BELL: {
                return BELL;
            }
            case CHIME: {
                return CHIME;
            }
            case XYLOPHONE: {
                return XYLOPHONE;
            }
            case IRON_XYLOPHONE: {
                return IRON_XYLOPHONE;
            }
            case COW_BELL: {
                return COW_BELL;
            }
            case DIDGERIDOO: {
                return DIDGERIDOO;
            }
            case BIT: {
                return BIT;
            }
            case BANJO: {
                return BANJO;
            }
            case PLING: {
                return PLING;
            }
        }
        return null;
    }

    public static int toneOfKey(int key) {
        return Math.min(24, Math.max(0, key - 33));
    }
}

