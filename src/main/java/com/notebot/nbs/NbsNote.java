/*
 * Decompiled with CFR 0.152.
 */
package com.notebot.nbs;

public final class NbsNote {
    public final int tick;
    public final int layer;
    public final int instrument;
    public final int key;
    public final int velocity;
    public final int panning;
    public final int pitch;

    public NbsNote(int tick, int layer, int instrument, int key, int velocity, int panning, int pitch) {
        this.tick = tick;
        this.layer = layer;
        this.instrument = instrument;
        this.key = key;
        this.velocity = velocity;
        this.panning = panning;
        this.pitch = pitch;
    }

    public String toString() {
        return "NbsNote{tick=" + this.tick + ", layer=" + this.layer + ", instrument=" + this.instrument + ", key=" + this.key + ", velocity=" + this.velocity + "}";
    }
}

