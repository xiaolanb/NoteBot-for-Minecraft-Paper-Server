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

import com.notebot.NotebotPlugin;
import com.notebot.gui.MainGui;
import com.notebot.gui.NotebotGui;
import com.notebot.nbs.NbsSong;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class SongSelectGui
implements NotebotGui {
    private static final int PAGE_SIZE = 45;
    private final NotebotPlugin plugin;
    private final int page;
    private final Inventory inventory;

    public SongSelectGui(NotebotPlugin plugin, int page) {
        this.plugin = plugin;
        this.page = page;
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)54, (String)plugin.config().guiSongTitle());
        this.rebuild();
    }

    private void rebuild() {
        this.inventory.clear();
        ArrayList<NbsSong> songs = new ArrayList<NbsSong>(this.plugin.library().all());
        if (songs.isEmpty()) {
            this.inventory.setItem(22, NotebotGui.item(Material.BARRIER, "\u00a7c\u66f2\u76ee\u5e93\u4e3a\u7a7a", "\u00a77\u628a .nbs \u6587\u4ef6\u653e\u5165 plugins/Notebot/songs", "\u00a77\u7136\u540e\u6267\u884c /notebot reload"));
        } else {
            int index;
            int totalPages = Math.max(1, (int)Math.ceil((double)songs.size() / 45.0));
            int p = Math.min(Math.max(this.page, 0), totalPages - 1);
            for (int i = 0; i < 45 && (index = p * 45 + i) < songs.size(); ++i) {
                NbsSong song = (NbsSong)songs.get(index);
                this.inventory.setItem(i, NotebotGui.item(Material.NOTE_BLOCK, "\u00a7e" + song.displayName(), "\u00a77\u6587\u4ef6: \u00a7f" + song.fileName, "\u00a77\u97f3\u7b26: \u00a7f" + song.notes.size(), "\u00a77\u97f3\u8f68: \u00a7f" + song.layers.size(), "\u00a77\u901f\u5ea6: \u00a7f" + String.format("%.1f TPS", song.ticksPerSecond), this.plugin.lang().get("gui-click-play", "\u00a77\u00a7o\u70b9\u51fb\u7acb\u5373\u64ad\u653e")));
            }
            if (p > 0) {
                this.inventory.setItem(45, NotebotGui.item(Material.ARROW, "\u00a7e\u2190 \u4e0a\u4e00\u9875", new String[0]));
            }
            if (p < totalPages - 1) {
                this.inventory.setItem(53, NotebotGui.item(Material.ARROW, "\u00a7e\u4e0b\u4e00\u9875 \u2192", new String[0]));
            }
        }
        this.inventory.setItem(49, NotebotGui.item(Material.OAK_DOOR, "\u00a77\u8fd4\u56de\u63a7\u5236\u53f0", new String[0]));
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        ArrayList<NbsSong> songs;
        int index;
        Player player = (Player)event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == 49) {
            player.openInventory(new MainGui(this.plugin).getInventory());
            return;
        }
        if (slot == 45 || slot == 53) {
            player.openInventory(new SongSelectGui(this.plugin, this.page + (slot == 53 ? 1 : -1)).getInventory());
            return;
        }
        if (slot >= 0 && slot < 45 && (index = this.page * 45 + slot) < (songs = new ArrayList<NbsSong>(this.plugin.library().all())).size()) {
            String fileName = ((NbsSong)songs.get((int)index)).fileName;
            this.plugin.selectSong(fileName);
            if (this.plugin.playback().play(fileName, player)) {
                this.plugin.send((CommandSender)player, this.plugin.lang().fmt("gui-song-selected", "\u00a7a\u5df2\u9009\u62e9\u5e76\u5f00\u59cb\u64ad\u653e \u00a7e{0}", ((NbsSong)songs.get(index)).displayName()));
            } else {
                player.sendMessage(this.plugin.lang().prefix() + this.plugin.lang().get("gui-no-song", "\u00a7c\u66f2\u76ee\u4e0d\u5b58\u5728\uff0c\u8bf7\u91cd\u65b0\u9009\u62e9"));
            }
            player.openInventory(new MainGui(this.plugin).getInventory());
        }
    }
}

