/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package com.notebot;

import com.notebot.NotebotPlugin;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Lang {
    private static final Pattern HEX = Pattern.compile("&#([0-9a-fA-F]{6})");
    private final NotebotPlugin plugin;
    private YamlConfiguration yaml;
    private String prefix;

    public Lang(NotebotPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(this.plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            this.plugin.saveResource("messages.yml", false);
        }
        this.yaml = YamlConfiguration.loadConfiguration((File)file);
        this.prefix = Lang.color(this.raw("prefix", "\u00a7b[Notebot]\u00a7r "));
    }

    public String prefix() {
        return this.prefix;
    }

    public String get(String key) {
        return Lang.color(this.raw(key, ""));
    }

    public String get(String key, String def) {
        return Lang.color(this.raw(key, def));
    }

    public String fmt(String key, String def, Object ... args) {
        String s = this.raw(key, def);
        for (int i = 0; i < args.length; ++i) {
            s = s.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return Lang.color(s);
    }

    private String raw(String key, String def) {
        if (this.yaml != null && this.yaml.contains(key)) {
            return this.yaml.getString(key);
        }
        return def;
    }

    public static String color(String s) {
        if (s == null) {
            return "";
        }
        Matcher matcher = HEX.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            StringBuilder hex = new StringBuilder("\u00a7x");
            for (char c : matcher.group(1).toCharArray()) {
                hex.append('\u00a7').append(c);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(hex.toString()));
        }
        matcher.appendTail(sb);
        s = sb.toString();
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ++i) {
            char code;
            char c = s.charAt(i);
            if (c == '&' && i + 1 < s.length() && "0123456789abcdefklmnor".indexOf(code = Character.toLowerCase(s.charAt(i + 1))) >= 0) {
                out.append('\u00a7').append(code);
                ++i;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }
}

