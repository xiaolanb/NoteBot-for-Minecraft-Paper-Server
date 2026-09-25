/*
 * Decompiled with CFR 0.152.
 */
package com.notebot.nbs;

public final class NbsCustomInstrument {
    public final String name;
    public final String soundFile;
    public final int key;
    public final int pressKey;

    public NbsCustomInstrument(String name, String soundFile, int key, int pressKey) {
        this.name = name;
        this.soundFile = soundFile;
        this.key = key;
        this.pressKey = pressKey;
    }

    public String toString() {
        return "NbsCustomInstrument{name='" + this.name + "', soundFile='" + this.soundFile + "'}";
    }
}

