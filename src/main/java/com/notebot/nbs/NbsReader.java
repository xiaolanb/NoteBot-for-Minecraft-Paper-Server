/*
 * Decompiled with CFR 0.152.
 */
package com.notebot.nbs;

import com.notebot.nbs.NbsCustomInstrument;
import com.notebot.nbs.NbsLayer;
import com.notebot.nbs.NbsNote;
import com.notebot.nbs.NbsSong;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class NbsReader {
    private NbsReader() {
    }

    public static NbsSong read(File file) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));){
            short jumpTicks;
            int length = NbsReader.readShort(in);
            int version = 0;
            int vanillaCount = 9;
            if (length == 0) {
                version = in.readUnsignedByte();
                vanillaCount = in.readUnsignedByte();
                if (version >= 3) {
                    length = NbsReader.readShort(in);
                }
            }
            int layerCount = NbsReader.readShort(in);
            String title = NbsReader.readString(in);
            String author = NbsReader.readString(in);
            NbsReader.readString(in);
            String description = NbsReader.readString(in);
            short tempoRaw = NbsReader.readShort(in);
            double tps = (double)Math.max(tempoRaw, 1) / 100.0;
            in.readUnsignedByte();
            in.readUnsignedByte();
            in.readUnsignedByte();
            NbsReader.readInt(in);
            NbsReader.readInt(in);
            NbsReader.readInt(in);
            NbsReader.readInt(in);
            NbsReader.readInt(in);
            NbsReader.readString(in);
            boolean loop = false;
            int maxLoopCount = 0;
            short loopStartTick = 0;
            if (version >= 4) {
                loop = in.readUnsignedByte() != 0;
                maxLoopCount = in.readUnsignedByte();
                loopStartTick = NbsReader.readShort(in);
            }
            ArrayList<NbsNote> notes = new ArrayList<NbsNote>();
            int tick = -1;
            while ((jumpTicks = NbsReader.readShort(in)) != 0) {
                short jumpLayers;
                tick += jumpTicks;
                int layer = -1;
                while ((jumpLayers = NbsReader.readShort(in)) != 0) {
                    layer += jumpLayers;
                    int instrument = in.readUnsignedByte();
                    int key = in.readUnsignedByte();
                    int velocity = 100;
                    int panning = 100;
                    short pitch = 0;
                    if (version >= 4) {
                        velocity = in.readUnsignedByte();
                        panning = in.readUnsignedByte();
                        pitch = NbsReader.readShort(in);
                    }
                    notes.add(new NbsNote(tick, layer, instrument, key, velocity, panning, pitch));
                }
            }
            ArrayList<NbsLayer> layers = new ArrayList<NbsLayer>(Math.max(layerCount, 0));
            for (int i = 0; i < layerCount; ++i) {
                String layerName = NbsReader.readString(in);
                boolean locked = false;
                if (version >= 4) {
                    locked = in.readUnsignedByte() != 0;
                }
                int volume = in.readUnsignedByte();
                int panning = 100;
                if (version >= 2) {
                    panning = in.readUnsignedByte();
                }
                layers.add(new NbsLayer(layerName, locked, volume, panning));
            }
            ArrayList<NbsCustomInstrument> customInstruments = new ArrayList<NbsCustomInstrument>();
            int customCount = NbsReader.readByteSafe(in, 0);
            for (int i = 0; i < customCount; ++i) {
                String ciName = NbsReader.readString(in);
                String soundFile = NbsReader.readString(in);
                int ciPitch = in.readUnsignedByte();
                int pressKey = in.readUnsignedByte();
                customInstruments.add(new NbsCustomInstrument(ciName, soundFile, ciPitch, pressKey));
            }
            notes.sort((a, b) -> {
                if (a.tick != b.tick) {
                    return Integer.compare(a.tick, b.tick);
                }
                return Integer.compare(a.layer, b.layer);
            });
            int lastTick = notes.isEmpty() ? 0 : ((NbsNote)notes.get((int)(notes.size() - 1))).tick;
            int headerLength = length > 0 ? length : lastTick + 1;
            NbsSong nbsSong = new NbsSong(file.getName(), title, author, description, tempoRaw, tps, Math.max(lastTick, headerLength - 1), loop, loopStartTick, maxLoopCount, layers, customInstruments, notes);
            return nbsSong;
        }
    }

    private static short readShort(DataInputStream in) throws IOException {
        return Short.reverseBytes(in.readShort());
    }

    private static int readInt(DataInputStream in) throws IOException {
        return Integer.reverseBytes(in.readInt());
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = NbsReader.readInt(in);
        if (length < 0 || length > 1000000) {
            throw new IOException("NBS \u5b57\u7b26\u4e32\u957f\u5ea6\u975e\u6cd5: " + length);
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static int readByteSafe(DataInputStream in, int defaultValue) throws IOException {
        try {
            return in.readUnsignedByte();
        }
        catch (EOFException e) {
            return defaultValue;
        }
    }
}

