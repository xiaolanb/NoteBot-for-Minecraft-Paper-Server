/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.util.Vector
 */
package com.notebot.bot;

import com.notebot.Nms;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class FakeBot {
    private final String name;
    private final int index;
    private final Player player;
    private float lastYaw = Float.NaN;
    private float lastPitch = Float.NaN;

    FakeBot(String name, int index, Player player) {
        this.name = name;
        this.index = index;
        this.player = player;
    }

    public String name() {
        return this.name;
    }

    public int index() {
        return this.index;
    }

    public Player player() {
        return this.player;
    }

    /** 假人的真实 UUID（与 PlayerList 中的一致） */
    public java.util.UUID uuid() {
        return this.player != null ? this.player.getUniqueId() : null;
    }

    public void swing() {
        if (this.player != null && this.player.isOnline()) {
            this.player.swingMainHand();
        }
    }

    public void clickBlock(Location block) {
        if (this.player == null || !this.player.isOnline() || block == null) {
            return;
        }
        this.lookAt(block);
        Nms.INSTANCE.clickBlock(this.player, block);
    }

    public void resetView() {
        this.lastYaw = Float.NaN;
        this.lastPitch = Float.NaN;
    }

    public void lookAt(Location target) {
        if (this.player == null || !this.player.isOnline() || target == null) {
            return;
        }
        if (!target.getWorld().equals((Object)this.player.getWorld())) {
            return;
        }
        Location eye = this.player.getEyeLocation();
        Vector direction = target.toVector().subtract(eye.toVector());
        if (direction.lengthSquared() < 1.0E-4) {
            return;
        }
        Location facing = eye.clone();
        facing.setDirection(direction);
        float yaw = facing.getYaw();
        float pitch = facing.getPitch();
        if (Math.abs(yaw - this.lastYaw) < 0.4f && Math.abs(pitch - this.lastPitch) < 0.4f) {
            return;
        }
        this.lastYaw = yaw;
        this.lastPitch = pitch;
        Nms.INSTANCE.rotateHead(this.player, yaw, pitch);
    }
}

