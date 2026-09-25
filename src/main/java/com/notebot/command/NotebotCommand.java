/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 */
package com.notebot.command;

import com.notebot.Lang;
import com.notebot.NotebotPlugin;
import com.notebot.gui.MainGui;
import com.notebot.nbs.NbsSong;
import com.notebot.playback.NbsInstrument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class NotebotCommand
implements CommandExecutor,
TabCompleter {
    private static final List<String> SUB_COMMANDS = List.of("spawn", "despawn", "play", "stop", "pause", "resume", "gui", "list", "reload", "tphere", "stage", "rescan");
    private final NotebotPlugin plugin;

    public NotebotCommand(NotebotPlugin plugin) {
        this.plugin = plugin;
    }

    private Lang lang() {
        return this.plugin.lang();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            this.sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "spawn": {
                Location anchor;
                if (sender instanceof Player) {
                    Player player = (Player)sender;
                    anchor = player.getLocation();
                } else {
                    anchor = !Bukkit.getWorlds().isEmpty() ? ((World)Bukkit.getWorlds().get(0)).getSpawnLocation() : null;
                }
                if (anchor == null) {
                    sender.sendMessage(this.lang().prefix() + this.lang().get("spawn-no-anchor"));
                    return true;
                }
                if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
                    int count = this.plugin.fakePlayers().spawnAll(anchor);
                    sender.sendMessage(this.lang().prefix() + this.lang().fmt("spawn-all-start", "\u00a7a\u6b63\u5728\u9010\u4e2a\u751f\u6210 {0} \u4e2a\u5047\u4eba\u2026", count));
                    break;
                }
                String spawned = this.plugin.fakePlayers().spawnNext(anchor);
                if (spawned != null) {
                    sender.sendMessage(this.lang().prefix() + this.lang().fmt("spawn-one", "\u00a7a\u5df2\u751f\u6210 \u00a7e{0}\u00a7a\uff08{1} / {2}\uff09", spawned, this.plugin.fakePlayers().count(), this.plugin.config().botCount()));
                } else {
                    sender.sendMessage(this.lang().prefix() + this.lang().get("spawn-full"));
                }
                String summary = this.plugin.fakePlayers().rescanIfManual();
                if (summary.isEmpty()) break;
                sender.sendMessage(this.lang().prefix() + summary);
                break;
            }
            case "despawn": {
                this.plugin.playback().stopSilent();
                if (args.length > 1) {
                    if (this.plugin.fakePlayers().remove(args[1])) {
                        sender.sendMessage(this.lang().prefix() + this.lang().fmt("despawn-one", "\u00a7c\u5df2\u79fb\u9664\u5047\u4eba \u00a7e{0}", args[1]));
                        break;
                    }
                    sender.sendMessage(this.lang().prefix() + this.lang().fmt("despawn-not-found", "\u00a7c\u5047\u4eba\u4e0d\u5b58\u5728\u6216\u4e0d\u5728\u7ebf: {0}", args[1]));
                    break;
                }
                int removed = this.plugin.fakePlayers().despawnAll();
                sender.sendMessage(this.lang().prefix() + this.lang().fmt("despawn-done", "\u00a7c\u5df2\u79fb\u9664 {0} \u4e2a\u5047\u4eba\uff0c\u821e\u53f0\u5df2\u6e05\u9664\u5e76\u8fd8\u539f\u5730\u5f62", removed));
                break;
            }
            case "play": {
                if (args.length < 2) {
                    sender.sendMessage(this.lang().prefix() + "\u00a7c\u7528\u6cd5: /notebot play <\u66f2\u76ee>");
                    return true;
                }
                String name = String.join((CharSequence)" ", Arrays.copyOfRange(args, 1, args.length));
                Player conductor = sender instanceof Player ? (Player)sender : null;
                if (this.plugin.playback().play(name, conductor)) break;
                sender.sendMessage(this.lang().prefix() + this.lang().fmt("play-not-found", "\u00a7c\u672a\u627e\u5230\u66f2\u76ee: {0}", name));
                break;
            }
            case "stop": {
                this.plugin.playback().stop();
                break;
            }
            case "pause": {
                if (this.plugin.playback().pause()) break;
                sender.sendMessage(this.lang().prefix() + this.lang().get("nothing-playing"));
                break;
            }
            case "resume": {
                if (this.plugin.playback().resume()) break;
                sender.sendMessage(this.lang().prefix() + this.lang().get("nothing-paused"));
                break;
            }
            case "tphere": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(this.lang().prefix() + this.lang().get("tphere-player-only"));
                    return true;
                }
                Player player = (Player)sender;
                Location loc = player.getLocation();
                if (args.length >= 2) {
                    if (this.plugin.fakePlayers().teleportOne(args[1], loc)) {
                        sender.sendMessage(this.lang().prefix() + this.lang().fmt("tphere-one", "\u00a7a\u5047\u4eba \u00a7e{0}\u00a7a \u5df2\u4f20\u9001\u5230\u4f60\u8eab\u8fb9\uff08{1}, {2}, {3}\uff09", args[1], loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                        break;
                    }
                    sender.sendMessage(this.lang().prefix() + this.lang().fmt("tphere-not-found", "\u00a7c\u5047\u4eba\u4e0d\u5b58\u5728\u6216\u4e0d\u5728\u7ebf: {0}", args[1]));
                    break;
                }
                this.plugin.fakePlayers().teleportTo(loc);
                sender.sendMessage(this.lang().prefix() + this.lang().fmt("tphere-all", "\u00a7a\u5168\u90e8\u5047\u4eba\u5df2\u4f20\u9001\u5230\u4f60\u8eab\u8fb9\uff08{0}, {1}, {2}\uff09", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                break;
            }
            case "gui": {
                if (sender instanceof Player) {
                    Player player = (Player)sender;
                    player.openInventory(new MainGui(this.plugin).getInventory());
                    break;
                }
                sender.sendMessage(this.lang().prefix() + "\u00a7c\u8be5\u547d\u4ee4\u53ea\u80fd\u7531\u73a9\u5bb6\u6267\u884c");
                break;
            }
            case "stage": {
                String action = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : "";
                if (action.equals("build")) {
                    Location anchor;
                    if (sender instanceof Player) {
                        anchor = ((Player)sender).getLocation();
                    } else {
                        anchor = !Bukkit.getWorlds().isEmpty() ? ((World)Bukkit.getWorlds().get(0)).getSpawnLocation() : null;
                    }
                    if (this.plugin.stage().build(anchor)) {
                        sender.sendMessage(this.lang().prefix() + this.lang().get("stage-built"));
                        break;
                    }
                    sender.sendMessage(this.lang().prefix() + this.lang().get("stage-build-fail"));
                    break;
                }
                if (action.equals("clear")) {
                    this.plugin.stage().clear();
                    sender.sendMessage(this.lang().prefix() + this.lang().get("stage-cleared"));
                    break;
                }
                sender.sendMessage(this.lang().prefix() + this.lang().get("stage-usage"));
                break;
            }
            case "rescan": {
                String summary = this.plugin.fakePlayers().rescanIfManual();
                if (summary.isEmpty()) {
                    sender.sendMessage(this.lang().prefix() + this.lang().fmt("rescan-empty", "\u00a7e\u672a\u626b\u63cf\u5230\u97f3\u7b26\u76d2\uff08\u534a\u5f84 {0} \u683c\uff0c\u4ec5\u624b\u52a8\u6a21\u5f0f\u751f\u6548\uff09", this.plugin.config().scanRadius()));
                    break;
                }
                sender.sendMessage(this.lang().prefix() + summary);
                break;
            }
            case "list": {
                this.sendList(sender);
                break;
            }
            case "reload": {
                this.plugin.reloadAll();
                sender.sendMessage(this.lang().prefix() + this.lang().get("reload-done"));
                break;
            }
            default: {
                this.sendHelp(sender);
            }
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        String p = this.lang().prefix();
        sender.sendMessage(p + "\u00a7e=== Notebot \u547d\u4ee4 ===");
        sender.sendMessage(p + "\u00a7f/notebot spawn \u00a77- \u751f\u6210\u4e00\u4e2a\u5047\u4eba\uff08spawn all = \u5168\u90e8\uff09");
        sender.sendMessage(p + "\u00a7f/notebot despawn \u00a77- \u79fb\u9664\u5047\u4eba\u5e76\u8fd8\u539f\u5730\u5f62");
        sender.sendMessage(p + "\u00a7f/notebot play <\u66f2\u76ee> \u00a77- \u64ad\u653e songs \u76ee\u5f55\u4e0b\u7684 .nbs");
        sender.sendMessage(p + "\u00a7f/notebot stop | pause | resume \u00a77- \u505c\u6b62 / \u6682\u505c / \u7ee7\u7eed");
        sender.sendMessage(p + "\u00a7f/notebot tphere [\u5047\u4eba\u540d] \u00a77- \u5047\u4eba\u4f20\u9001\u5230\u4f60\uff08\u6216\u6307\u5b9a\u5047\u4eba\uff09\u8eab\u8fb9");
        sender.sendMessage(p + "\u00a7f/notebot rescan \u00a77- \u91cd\u65b0\u626b\u63cf\u8eab\u8fb9\u7684\u97f3\u7b26\u76d2\uff08\u624b\u52a8\u6a21\u5f0f\uff09");
        sender.sendMessage(p + "\u00a7f/notebot gui | list | reload");
    }

    private void sendList(CommandSender sender) {
        String p = this.lang().prefix();
        sender.sendMessage(p + "\u00a7e=== Notebot \u72b6\u6001 ===");
        sender.sendMessage(p + "\u00a77\u5047\u4eba (" + this.plugin.fakePlayers().count() + " / " + this.plugin.config().botCount() + "): \u00a7f" + String.join((CharSequence)"\u00a78, \u00a7f", this.plugin.fakePlayers().names()));
        StringBuilder assignments = new StringBuilder();
        for (Map.Entry<Integer, String> entry : this.plugin.assignments().snapshot().entrySet()) {
            NbsInstrument instrument = NbsInstrument.byId(entry.getKey());
            if (instrument == null) continue;
            if (assignments.length() > 0) {
                assignments.append("\u00a78 | ");
            }
            assignments.append("\u00a7f").append(instrument.display()).append(" \u2192 \u00a7a").append(entry.getValue());
        }
        sender.sendMessage(p + "\u00a77\u97f3\u8272\u5206\u62c5: " + String.valueOf(assignments));
        sender.sendMessage(p + "\u00a77\u821e\u53f0: " + (this.plugin.stage().isBuilt() ? "\u00a7a\u5df2\u642d\u5efa" : "\u00a78\u672a\u642d\u5efa"));
        sender.sendMessage(p + "\u00a77\u66f2\u76ee (" + this.plugin.library().size() + "):");
        for (NbsSong song : this.plugin.library().all()) {
            sender.sendMessage(p + "  \u00a7f" + song.fileName + " \u00a77- " + song.notes.size() + " \u97f3\u7b26, " + String.format("%.1f", song.ticksPerSecond) + " TPS");
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            ArrayList<String> result = new ArrayList<String>();
            for (String sub : SUB_COMMANDS) {
                if (!sub.startsWith(prefix)) continue;
                result.add(sub);
            }
            return result;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("play")) {
            String joined = String.join((CharSequence)" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase(Locale.ROOT);
            ArrayList<String> result = new ArrayList<String>();
            for (NbsSong song : this.plugin.library().all()) {
                if (!song.fileName.toLowerCase(Locale.ROOT).startsWith(joined)) continue;
                result.add(song.fileName);
            }
            return result;
        }
        if (args[0].equalsIgnoreCase("tphere") && args.length == 2) {
            return this.plugin.fakePlayers().names();
        }
        if (args[0].equalsIgnoreCase("despawn") && args.length == 2) {
            return this.plugin.fakePlayers().names();
        }
        if (args[0].equalsIgnoreCase("stage") && args.length == 2) {
            return List.of("build", "clear");
        }
        return List.of();
    }
}

