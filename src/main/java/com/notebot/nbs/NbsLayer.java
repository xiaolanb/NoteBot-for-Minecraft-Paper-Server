/*
 * Decompiled with CFR 0.152.
 */
package com.notebot.nbs;

public final class NbsLayer {
    public final String name;
    public final boolean locked;
    public final int volume;
    public final int panning;

    public NbsLayer(String name, boolean locked, int volume, int panning) {
        this.name = name;
        this.locked = locked;
        this.volume = volume;
        this.panning = panning;
    }

    public String toString() {
        return "NbsLayer{name='" + this.name + "', volume=" + this.volume + "}";
    }
}

