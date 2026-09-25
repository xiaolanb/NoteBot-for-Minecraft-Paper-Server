/*
 * Decompiled with CFR 0.152.
 */
package com.notebot.nbs;

import com.notebot.nbs.NbsCustomInstrument;
import com.notebot.nbs.NbsLayer;
import com.notebot.nbs.NbsNote;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class NbsSong {
    public final String fileName;
    public final String title;
    public final String author;
    public final String description;
    public final int tempoRaw;
    public final double ticksPerSecond;
    public final int lastTick;
    public final boolean loop;
    public final int loopStartTick;
    public final int loopCount;
    public final List<NbsLayer> layers;
    public final List<NbsCustomInstrument> customInstruments;
    public final List<NbsNote> notes;

    public NbsSong(String fileName, String title, String author, String description, int tempoRaw, double ticksPerSecond, int lastTick, boolean loop, int loopStartTick, int loopCount, List<NbsLayer> layers, List<NbsCustomInstrument> customInstruments, List<NbsNote> notes) {
        this.fileName = fileName;
        this.title = title;
        this.author = author;
        this.description = description;
        this.tempoRaw = tempoRaw;
        this.ticksPerSecond = ticksPerSecond;
        this.lastTick = lastTick;
        this.loop = loop;
        this.loopStartTick = loopStartTick;
        this.loopCount = loopCount;
        this.layers = Collections.unmodifiableList(layers);
        this.customInstruments = Collections.unmodifiableList(customInstruments);
        this.notes = Collections.unmodifiableList(notes);
    }

    public Set<Integer> usedVanillaInstruments() {
        LinkedHashSet<Integer> set = new LinkedHashSet<Integer>();
        for (NbsNote note : this.notes) {
            if (note.instrument < 0 || note.instrument > 15) continue;
            set.add(note.instrument);
        }
        return set;
    }

    public String displayName() {
        String t = this.title == null || this.title.isBlank() ? this.fileName : this.title;
        String a = this.author == null || this.author.isBlank() ? "" : " - " + this.author;
        return t + a;
    }
}

