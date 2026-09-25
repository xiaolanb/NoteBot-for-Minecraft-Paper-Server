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
import com.notebot.playback.NbsInstrument;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class AssignGui
implements NotebotGui {
    private final NotebotPlugin plugin;
    private final int instrumentId;
    private final Inventory inventory;

    public AssignGui(NotebotPlugin plugin, int instrumentId) {
        this.plugin = plugin;
        this.instrumentId = instrumentId;
        NbsInstrument instrument = NbsInstrument.byId(instrumentId);
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)54, (String)("\u00a78\u2630 \u5206\u914d: " + String.valueOf(instrument != null ? instrument.display() : Integer.valueOf(instrumentId))));
        this.rebuild();
    }

    private void rebuild() {
        this.inventory.clear();
        NbsInstrument instrument = NbsInstrument.byId(this.instrumentId);
        String current = this.plugin.assignments().botFor(this.instrumentId);
        List<String> names = this.plugin.fakePlayers().names();
        for (int i = 0; i < names.size() && i < 16; ++i) {
            String name = names.get(i);
            boolean selected = name.equals(current);
            this.inventory.setItem(i, NotebotGui.item(Material.PLAYER_HEAD, (selected ? "\u00a7a\u25b6 " : "\u00a7e") + name, selected ? "\u00a77\u5f53\u524d\u62c5\u5f53" : "\u00a77\u70b9\u51fb\u5206\u914d", "\u00a77\u00a7o\u70b9\u51fb\u540e\u8fd4\u56de\u4e0a\u7ea7\u83dc\u5355"));
        }
        if (names.isEmpty()) {
            this.inventory.setItem(13, NotebotGui.item(Material.PAPER, "\u00a78\u6682\u65e0\u5728\u7ebf\u5047\u4eba", "\u00a77\u5148\u6267\u884c /notebot spawn \u751f\u6210\u5047\u4eba"));
        }
        this.inventory.setItem(22, NotebotGui.item(Material.BARRIER, "\u00a7c\u2715 \u65e0\uff08\u4e0d\u6f14\u594f\uff09", current == null ? "\u00a77\u5f53\u524d" : "\u00a77\u70b9\u51fb\u53d6\u6d88\u5206\u914d"));
        this.inventory.setItem(30, NotebotGui.item(instrument != null ? instrument.icon() : Material.NOTE_BLOCK, "\u00a7a\u81ea\u52a8\uff08\u9ed8\u8ba4 " + this.plugin.config().botName(this.instrumentId) + "\uff09", "\u00a77\u70b9\u51fb\u6062\u590d\u9ed8\u8ba4\u62c5\u5f53"));
        this.inventory.setItem(49, NotebotGui.item(Material.OAK_DOOR, "\u00a77\u2190 \u8fd4\u56de", new String[0]));
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        int slot = event.getRawSlot();
        NbsInstrument instrument = NbsInstrument.byId(this.instrumentId);
        if (slot == 49) {
            player.openInventory(new MainGui(this.plugin).getInventory());
            return;
        }
        if (slot == 22) {
            this.plugin.assignments().assign(this.instrumentId, null);
            this.plugin.send((CommandSender)player, "\u00a7e" + instrument.display() + "\u00a7r \u2192 \u00a78\u65e0");
        } else if (slot == 30) {
            String name = this.plugin.config().botName(this.instrumentId);
            this.plugin.assignments().assign(this.instrumentId, name);
            this.plugin.send((CommandSender)player, "\u00a7e" + instrument.display() + "\u00a7r \u2192 \u00a7a" + name);
        } else if (slot >= 0 && slot < 16) {
            List<String> names = this.plugin.fakePlayers().names();
            if (slot < names.size()) {
                String name = names.get(slot);
                this.plugin.assignments().assign(this.instrumentId, name);
                this.plugin.send((CommandSender)player, "\u00a7e" + instrument.display() + "\u00a7r \u2192 \u00a7a" + name);
            }
        } else {
            return;
        }
        player.openInventory(new MainGui(this.plugin).getInventory());
    }
}

