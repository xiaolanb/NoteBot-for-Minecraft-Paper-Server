/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.entity.Player
 *  org.bukkit.profile.PlayerProfile
 */
package com.notebot;

import com.notebot.NotebotPlugin;
import com.notebot.bot.FakeChannel;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

public final class Nms {
    public static Nms INSTANCE;
    private final NotebotPlugin plugin;
    private final Class<?> cServerPlayer;
    private final Class<?> cConnection;
    private final Class<?> cPacketFlow;
    private final Class<?> cClientInformation;
    private final Class<?> cCommonListenerCookie;
    private final Class<?> cGameProfile;
    private final Class<?> cServerboundKeepAlivePacket;
    private final Class<?> cRotPacket;
    private final Class<?> cComponent;
    private final Class<?> cLongArrayList;
    private final Constructor<?> gameProfileCtor;
    private final Constructor<?> serverPlayerCtor;
    private final Constructor<?> connectionCtor;
    private final Constructor<?> keepAlivePacketCtor;
    private final Constructor<?> rotCtor;
    private final Constructor<?> blockPosCtor;
    private final Constructor<?> vec3Ctor;
    private final Constructor<?> blockHitResultCtor;
    private final Constructor<?> playerInfoRemoveCtor;
    private final Method craftServerGetServer;
    private final Method craftWorldGetHandle;
    private final Method craftPlayerGetHandle;
    private final Method clientInfoCreateDefault;
    private final Method cookieCreateInitial;
    private final Method playerListPlaceNewPlayer;
    private final Method getPlayerList;
    private final Method handleKeepAlive;
    private final Method componentLiteral;
    private final Method listenerDisconnect;
    private final Method connectionHandleDisconnection;
    private final Method playerListRemove;
    private final Method entityGetId;
    private final Method entityGetX;
    private final Method entityGetY;
    private final Method entityGetZ;
    private final Method entityLevel;
    private final Method levelDimension;
    private final Method playerListBroadcast;
    private final Method longListToLongArray;
    private final Method craftProfileGetGameProfile;
    private final Method gameModeUseItemOn;
    private final Method levelGetBlockState;
    private final Method noteBlockSetInstrument;
    private final Method blockStateGetValue;
    private final Method stateHolderSetValue;
    private final Method craftBlockDataFromData;
    private final Method playerListBroadcastAll;
    private final Field packetFlowServerbound;
    private final Field connectionChannel;
    private final Field connectionAddress;
    private final Field serverPlayerConnection;
    private final Field listenerConnectionField;
    private final Field listenerKeepAlives;
    private final Field playerListPlayersField;
    private final Field serverPlayerGameModeField;
    /** 1.21.9+ 才存在：是否允许出现在服务器列表(MOTD)玩家样本中 */
    private final Field serverPlayerAllowsListing;
    private final Field noteBlockInstrumentProperty;
    private final Field blocksNoteBlock;
    private final Field fXRot;
    private final Field fYRot;
    private final Field fXRotO;
    private final Field fYRotO;
    private final Field fYHeadRot;
    private final Object mcServer;
    private final Object playerList;
    private final Object directionUp;
    private final Object handMainHand;
    private final Object itemStackEmpty;
    private final Class<?> cNoteBlockInstrumentEnum;

    public static void init(NotebotPlugin plugin) {
        try {
            INSTANCE = new Nms(plugin);
            plugin.getLogger().info("NMS \u521d\u59cb\u5316\u5b8c\u6210\uff0c\u670d\u52a1\u5668\u7248\u672c: " + Bukkit.getMinecraftVersion());
        }
        catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "NMS \u521d\u59cb\u5316\u5931\u8d25\uff0c\u5047\u4eba\u529f\u80fd\u4e0d\u53ef\u7528", t);
            INSTANCE = null;
        }
    }

    private Nms(NotebotPlugin plugin) throws Exception {
        this.plugin = plugin;
        this.cServerPlayer = Nms.cls("net.minecraft.server.level.ServerPlayer");
        this.cConnection = Nms.cls("net.minecraft.network.Connection");
        this.cPacketFlow = Nms.cls("net.minecraft.network.protocol.PacketFlow");
        this.cClientInformation = Nms.cls("net.minecraft.server.level.ClientInformation");
        this.cCommonListenerCookie = Nms.cls("net.minecraft.server.network.CommonListenerCookie");
        this.cServerboundKeepAlivePacket = Nms.cls("net.minecraft.network.protocol.common.ServerboundKeepAlivePacket");
        this.cRotPacket = Nms.cls("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket$Rot");
        this.cComponent = Nms.cls("net.minecraft.network.chat.Component");
        Class<?> cServerLevel = Nms.cls("net.minecraft.server.level.ServerLevel");
        Class<?> cMinecraftServer = Nms.cls("net.minecraft.server.MinecraftServer");
        Class<?> cPlayerList = Nms.cls("net.minecraft.server.players.PlayerList");
        Class<?> cGamePacketListenerCommon = Nms.cls("net.minecraft.server.network.ServerCommonPacketListenerImpl");
        this.cLongArrayList = Nms.cls("it.unimi.dsi.fastutil.longs.LongArrayList");
        this.cGameProfile = Nms.tryCls("com.mojang.authlib.GameProfile");
        this.gameProfileCtor = this.cGameProfile != null ? Nms.ctor(this.cGameProfile, UUID.class, String.class) : null;
        this.serverPlayerCtor = Nms.ctor(this.cServerPlayer, cMinecraftServer, cServerLevel, this.cGameProfile, this.cClientInformation);
        this.connectionCtor = Nms.ctor(this.cConnection, this.cPacketFlow);
        this.keepAlivePacketCtor = Nms.ctor(this.cServerboundKeepAlivePacket, Long.TYPE);
        this.rotCtor = Nms.ctor(this.cRotPacket, Integer.TYPE, Byte.TYPE, Byte.TYPE, Boolean.TYPE);
        this.blockPosCtor = Nms.ctor(Nms.cls("net.minecraft.core.BlockPos"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
        this.vec3Ctor = Nms.ctor(Nms.cls("net.minecraft.world.phys.Vec3"), Double.TYPE, Double.TYPE, Double.TYPE);
        this.blockHitResultCtor = Nms.ctor(Nms.cls("net.minecraft.world.phys.BlockHitResult"), Nms.cls("net.minecraft.world.phys.Vec3"), Nms.cls("net.minecraft.core.Direction"), Nms.cls("net.minecraft.core.BlockPos"), Boolean.TYPE);
        this.craftServerGetServer = Nms.method(Bukkit.getServer().getClass(), "getServer", new Class[0]);
        this.craftWorldGetHandle = Nms.method(((World)Bukkit.getWorlds().get(0)).getClass(), "getHandle", new Class[0]);
        this.clientInfoCreateDefault = Nms.method(this.cClientInformation, "createDefault", new Class[0]);
        this.cookieCreateInitial = Nms.method(this.cCommonListenerCookie, "createInitial", this.cGameProfile, Boolean.TYPE);
        this.playerListPlaceNewPlayer = Nms.method(cPlayerList, "placeNewPlayer", this.cConnection, this.cServerPlayer, this.cCommonListenerCookie);
        this.getPlayerList = Nms.method(cMinecraftServer, "getPlayerList", new Class[0]);
        this.handleKeepAlive = Nms.method(cGamePacketListenerCommon, "handleKeepAlive", this.cServerboundKeepAlivePacket);
        this.componentLiteral = Nms.method(this.cComponent, "literal", String.class);
        this.listenerDisconnect = Nms.method(cGamePacketListenerCommon, "disconnect", this.cComponent);
        this.connectionHandleDisconnection = Nms.method(this.cConnection, "handleDisconnection", new Class[0]);
        this.playerListRemove = Nms.method(cPlayerList, "remove", this.cServerPlayer);
        this.gameModeUseItemOn = Nms.method(Nms.cls("net.minecraft.server.level.ServerPlayerGameMode"), "useItemOn", this.cServerPlayer, Nms.cls("net.minecraft.world.level.Level"), Nms.cls("net.minecraft.world.item.ItemStack"), Nms.cls("net.minecraft.world.InteractionHand"), Nms.cls("net.minecraft.world.phys.BlockHitResult"));
        Class<?> cNoteBlock = Nms.cls("net.minecraft.world.level.block.NoteBlock");
        this.levelGetBlockState = Nms.method(cServerLevel, "getBlockState", Nms.cls("net.minecraft.core.BlockPos"));
        this.noteBlockSetInstrument = Nms.method(cNoteBlock, "setInstrument", Nms.cls("net.minecraft.world.level.LevelReader"), Nms.cls("net.minecraft.core.BlockPos"), Nms.cls("net.minecraft.world.level.block.state.BlockState"));
        this.blockStateGetValue = Nms.method(Nms.cls("net.minecraft.world.level.block.state.BlockState"), "getValue", Nms.cls("net.minecraft.world.level.block.state.properties.Property"));
        this.stateHolderSetValue = Nms.method(Nms.cls("net.minecraft.world.level.block.state.StateHolder"), "setValue", Nms.cls("net.minecraft.world.level.block.state.properties.Property"), Comparable.class);
        this.craftBlockDataFromData = Nms.method(Nms.cls("org.bukkit.craftbukkit.block.data.CraftBlockData"), "fromData", Nms.cls("net.minecraft.world.level.block.state.BlockState"));
        this.cNoteBlockInstrumentEnum = Nms.cls("net.minecraft.world.level.block.state.properties.NoteBlockInstrument");
        this.noteBlockInstrumentProperty = Nms.field(cNoteBlock, "INSTRUMENT");
        this.blocksNoteBlock = Nms.field(Nms.cls("net.minecraft.world.level.block.Blocks"), "NOTE_BLOCK");
        this.entityGetId = Nms.method(Nms.cls("net.minecraft.world.entity.Entity"), "getId", new Class[0]);
        this.entityGetX = Nms.method(Nms.cls("net.minecraft.world.entity.Entity"), "getX", new Class[0]);
        this.entityGetY = Nms.method(Nms.cls("net.minecraft.world.entity.Entity"), "getY", new Class[0]);
        this.entityGetZ = Nms.method(Nms.cls("net.minecraft.world.entity.Entity"), "getZ", new Class[0]);
        this.entityLevel = Nms.method(Nms.cls("net.minecraft.world.entity.Entity"), "level", new Class[0]);
        this.levelDimension = Nms.method(Nms.cls("net.minecraft.world.level.Level"), "dimension", new Class[0]);
        this.playerListBroadcast = Nms.method(cPlayerList, "broadcast", Nms.cls("net.minecraft.world.entity.player.Player"), Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Nms.cls("net.minecraft.resources.ResourceKey"), Nms.cls("net.minecraft.network.protocol.Packet"));
        this.longListToLongArray = Nms.method(this.cLongArrayList, "toLongArray", new Class[0]);
        this.packetFlowServerbound = Nms.field(this.cPacketFlow, "SERVERBOUND");
        this.connectionChannel = Nms.field(this.cConnection, "channel");
        this.connectionAddress = Nms.field(this.cConnection, "address");
        this.serverPlayerConnection = Nms.field(this.cServerPlayer, "connection");
        this.listenerConnectionField = Nms.field(cGamePacketListenerCommon, "connection");
        this.playerListPlayersField = Nms.field(cPlayerList, "players");
        this.serverPlayerGameModeField = Nms.field(this.cServerPlayer, "gameMode");
        this.playerListBroadcastAll = Nms.method(cPlayerList, "broadcastAll", Nms.cls("net.minecraft.network.protocol.Packet"));
        this.playerInfoRemoveCtor = Nms.ctor(Nms.cls("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket"), List.class);
        Field allowsListing = null;
        try {
            allowsListing = Nms.field(this.cServerPlayer, "allowsListing");
        }
        catch (Throwable throwable) {
            // 旧版本没有该字段：MOTD 玩家样本按原版行为处理
        }
        this.serverPlayerAllowsListing = allowsListing;
        this.listenerKeepAlives = Nms.field(cGamePacketListenerCommon, "keepAlives");
        Class<?> cEntity = Nms.cls("net.minecraft.world.entity.Entity");
        this.fXRot = Nms.field(cEntity, "xRot");
        this.fYRot = Nms.field(cEntity, "yRot");
        this.fXRotO = Nms.field(cEntity, "xRotO");
        this.fYRotO = Nms.field(cEntity, "yRotO");
        this.fYHeadRot = Nms.field(Nms.cls("net.minecraft.world.entity.LivingEntity"), "yHeadRot");
        Method profileGetter = null;
        try {
            PlayerProfile sample = Bukkit.createPlayerProfile((UUID)UUID.randomUUID(), (String)"x");
            profileGetter = Nms.method(sample.getClass(), "getGameProfile", new Class[0]);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.craftProfileGetGameProfile = profileGetter;
        this.craftPlayerGetHandle = Nms.method(Nms.clsCraftPlayer(), "getHandle", new Class[0]);
        this.mcServer = this.craftServerGetServer.invoke((Object)Bukkit.getServer(), new Object[0]);
        this.playerList = this.getPlayerList.invoke(this.mcServer, new Object[0]);
        this.directionUp = Enum.valueOf(Nms.cls("net.minecraft.core.Direction").asSubclass(Enum.class), "UP");
        this.handMainHand = Enum.valueOf(Nms.cls("net.minecraft.world.InteractionHand").asSubclass(Enum.class), "MAIN_HAND");
        this.itemStackEmpty = Nms.field(Nms.cls("net.minecraft.world.item.ItemStack"), "EMPTY").get(null);
    }

    private static Class<?> clsCraftPlayer() throws Exception {
        try {
            return Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
        }
        catch (ClassNotFoundException e) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Class c = p.getClass();
                if (!c.getSimpleName().equals("CraftPlayer")) continue;
                return c;
            }
            throw e;
        }
    }

    public Player spawnFakePlayer(String name, UUID uuid, World world, double x, double y, double z, float yaw, float pitch) {
        if (INSTANCE == null) {
            return null;
        }
        try {
            Object profile = this.newGameProfile(uuid, name);
            Object clientInfo = this.clientInfoCreateDefault.invoke(null, new Object[0]);
            Object level = this.craftWorldGetHandle.invoke((Object)world, new Object[0]);
            Object serverPlayer = this.serverPlayerCtor.newInstance(this.mcServer, level, profile, clientInfo);
            // 1.21.9+：假人用默认 ClientInformation 时 allowsListing=false，
            // 服务器列表会把名字替换成 "Anonymous Player"；标记为允许列出后
            // MOTD 玩家样本才会显示假人名字（可由 display.motd 进一步过滤）。
            if (this.serverPlayerAllowsListing != null) {
                this.serverPlayerAllowsListing.setBoolean(serverPlayer, true);
            }
            Object connection = this.connectionCtor.newInstance(this.packetFlowServerbound.get(null));
            FakeChannel fakeChannel = new FakeChannel();
            this.connectionChannel.set(connection, (Object)fakeChannel);
            this.connectionAddress.set(connection, ((Object)((Object)fakeChannel)).getClass().getMethod("remoteAddress", new Class[0]).invoke((Object)fakeChannel, new Object[0]));
            Object cookie = this.cookieCreateInitial.invoke(null, profile, false);
            this.playerListPlaceNewPlayer.invoke(this.playerList, connection, serverPlayer, cookie);
            Player bukkit = Bukkit.getPlayer((UUID)uuid);
            if (bukkit != null) {
                bukkit.teleport(new Location(world, x, y, z, yaw, pitch));
            }
            return bukkit;
        }
        catch (Throwable t) {
            this.plugin.getLogger().log(Level.SEVERE, "\u5047\u4eba\u751f\u6210\u5f02\u5e38: " + name, t);
            return null;
        }
    }

    public void removeFakePlayer(Player player) {
        try {
            Collection collection;
            Object handle = this.craftPlayerGetHandle.invoke((Object)player, new Object[0]);
            Object listener = this.serverPlayerConnection.get(handle);
            if (listener == null) {
                return;
            }
            try {
                this.listenerDisconnect.invoke(listener, this.componentLiteral.invoke(null, "Notebot removed"));
                Object connection = this.listenerConnectionField.get(listener);
                if (connection != null) {
                    this.connectionHandleDisconnection.invoke(connection, new Object[0]);
                }
            }
            catch (Throwable t) {
                this.plugin.getLogger().log(Level.WARNING, "\u5047\u4eba\u65ad\u5f00\u6d41\u7a0b\u5f02\u5e38\uff08\u5c06\u4f7f\u7528\u515c\u5e95\u79fb\u9664\uff09 " + player.getName(), t);
            }
            Object playersList = this.playerListPlayersField.get(this.playerList);
            if (playersList instanceof Collection && (collection = (Collection)playersList).contains(handle)) {
                this.playerListRemove.invoke(this.playerList, handle);
            }
        }
        catch (Throwable t) {
            this.plugin.getLogger().log(Level.WARNING, "\u79fb\u9664\u5047\u4eba\u5f02\u5e38: " + player.getName(), t);
        }
    }

    public void maintainConnection(Player player) {
        try {
            long[] ids;
            Object handle = this.craftPlayerGetHandle.invoke((Object)player, new Object[0]);
            Object listener = this.serverPlayerConnection.get(handle);
            if (listener == null) {
                return;
            }
            Object keepAlives = this.listenerKeepAlives.get(listener);
            if (keepAlives == null) {
                return;
            }
            for (long id : ids = (long[])this.longListToLongArray.invoke(keepAlives, new Object[0])) {
                this.handleKeepAlive.invoke(listener, this.keepAlivePacketCtor.newInstance(id));
            }
        }
        catch (Throwable t) {
            this.plugin.getLogger().log(Level.WARNING, "\u5047\u4eba keepalive \u7ef4\u62a4\u5f02\u5e38: " + player.getName(), t);
        }
    }

    public void rotateHead(Player player, float yaw, float pitch) {
        try {
            Object handle = this.craftPlayerGetHandle.invoke((Object)player, new Object[0]);
            int entityId = (Integer)this.entityGetId.invoke(handle, new Object[0]);
            this.fYRot.setFloat(handle, yaw);
            this.fYRotO.setFloat(handle, yaw);
            this.fXRot.setFloat(handle, pitch);
            this.fXRotO.setFloat(handle, pitch);
            this.fYHeadRot.setFloat(handle, yaw);
            Object rotPacket = this.rotCtor.newInstance(entityId, (byte)(yaw * 256.0f / 360.0f), (byte)(pitch * 256.0f / 360.0f), false);
            Object levelHandle = this.entityLevel.invoke(handle, new Object[0]);
            Object dimension = this.levelDimension.invoke(levelHandle, new Object[0]);
            double x = (Double)this.entityGetX.invoke(handle, new Object[0]);
            double y = (Double)this.entityGetY.invoke(handle, new Object[0]);
            double z = (Double)this.entityGetZ.invoke(handle, new Object[0]);
            this.playerListBroadcast.invoke(this.playerList, null, x, y, z, this.plugin.config().soundRadius(), dimension, rotPacket);
        }
        catch (Throwable t) {
            this.plugin.getLogger().log(Level.WARNING, "\u5047\u4eba\u8f6c\u5934\u5f02\u5e38: " + player.getName(), t);
        }
    }

    public void clickBlock(Player player, Location blockCenter) {
        try {
            Object handle = this.craftPlayerGetHandle.invoke((Object)player, new Object[0]);
            Object gameMode = this.serverPlayerGameModeField.get(handle);
            Object vec = this.vec3Ctor.newInstance(blockCenter.getX(), blockCenter.getY(), blockCenter.getZ());
            Object pos = this.blockPosCtor.newInstance(blockCenter.getBlockX(), blockCenter.getBlockY(), blockCenter.getBlockZ());
            Object hit = this.blockHitResultCtor.newInstance(vec, this.directionUp, pos, false);
            Object level = this.craftWorldGetHandle.invoke((Object)blockCenter.getWorld(), new Object[0]);
            this.gameModeUseItemOn.invoke(gameMode, handle, level, this.itemStackEmpty, this.handMainHand, hit);
            player.swingMainHand();
        }
        catch (Throwable t) {
            this.plugin.getLogger().log(Level.WARNING, "\u5047\u4eba\u70b9\u51fb\u97f3\u7b26\u76d2\u5f02\u5e38: " + player.getName(), t);
        }
    }

    public String instrumentNameAt(World world, int x, int y, int z) {
        try {
            Object level = this.craftWorldGetHandle.invoke((Object)world, new Object[0]);
            Object pos = this.blockPosCtor.newInstance(x, y, z);
            Object state = this.levelGetBlockState.invoke(level, pos);
            Object noteBlock = this.blocksNoteBlock.get(null);
            Object instrumented = this.noteBlockSetInstrument.invoke(noteBlock, level, pos, state);
            Object value = this.blockStateGetValue.invoke(instrumented, this.noteBlockInstrumentProperty.get(null));
            return value.toString();
        }
        catch (Throwable t) {
            return null;
        }
    }

    /**
     * 强制把指定音符盒方块设成目标音色（不依赖基座方块），并刷新给所有玩家。
     * instrument 为原版枚举名（HARP/BASS/BASEDRUM/...）。
     */
    public boolean setNoteBlockInstrument(World world, int x, int y, int z, String instrument) {
        try {
            Object level = this.craftWorldGetHandle.invoke((Object)world, new Object[0]);
            Object pos = this.blockPosCtor.newInstance(x, y, z);
            Object state = this.levelGetBlockState.invoke(level, pos);
            Object craftData = null;
            Object enumValue = this.instrumentValue(instrument);
            if (enumValue != null) {
                Object updated = this.stateHolderSetValue.invoke(state, this.noteBlockInstrumentProperty.get(null), enumValue);
                craftData = this.craftBlockDataFromData.invoke(null, updated);
            }
            if (craftData == null) {
                return false;
            }
            BlockData data = (BlockData)craftData;
            if (Bukkit.isPrimaryThread()) {
                world.getBlockAt(x, y, z).setBlockData(data, false);
            } else {
                Bukkit.getScheduler().runTask((org.bukkit.plugin.Plugin)this.plugin, () -> world.getBlockAt(x, y, z).setBlockData(data, false));
            }
            return true;
        }
        catch (Throwable t) {
            this.plugin.getLogger().log(Level.WARNING, "\u8bbe\u7f6e\u97f3\u7b26\u76d2\u97f3\u8272\u5f02\u5e38: " + instrument, t);
            return false;
        }
    }

    private Object instrumentValue(String instrument) {
        if (instrument == null || this.cNoteBlockInstrumentEnum == null) {
            return null;
        }
        String want = instrument.trim().toUpperCase(Locale.ROOT);
        Object[] constants = this.cNoteBlockInstrumentEnum.getEnumConstants();
        if (constants == null) {
            return null;
        }
        for (Object constant : constants) {
            if (((Enum)constant).name().equals(want)) {
                return constant;
            }
        }
        return null;
    }

    /** 从所有客户端 Tab 列表中隐藏指定 UUID（服务端仍保留这些假人） */
    public void hideFromTablist(Collection<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return;
        }
        if (this.playerInfoRemoveCtor == null || this.playerListBroadcastAll == null) {
            return;
        }
        try {
            Object packet = this.playerInfoRemoveCtor.newInstance(new ArrayList<UUID>(uuids));
            this.playerListBroadcastAll.invoke(this.playerList, packet);
            this.plugin.getLogger().info("\u5df2\u4ece Tab \u5217\u8868\u9690\u85cf " + uuids.size() + " \u4e2a\u5047\u4eba");
        }
        catch (Throwable t) {
            this.plugin.getLogger().log(Level.WARNING, "\u9690\u85cf Tab \u5217\u8868\u5047\u4eba\u5f02\u5e38", t);
        }
    }

    private Object newGameProfile(UUID uuid, String name) throws Exception {
        if (this.gameProfileCtor != null) {
            return this.gameProfileCtor.newInstance(uuid, name);
        }
        if (this.craftProfileGetGameProfile != null) {
            PlayerProfile profile = Bukkit.createPlayerProfile((UUID)uuid, (String)name);
            return this.craftProfileGetGameProfile.invoke((Object)profile, new Object[0]);
        }
        throw new IllegalStateException("\u65e0\u6cd5\u6784\u9020 GameProfile");
    }

    private static Class<?> cls(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }

    private static Class<?> tryCls(String name) {
        try {
            return Class.forName(name);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Constructor<?> ctor(Class<?> type, Class<?> ... params) {
        try {
            Constructor<?> c = type.getDeclaredConstructor(params);
            c.setAccessible(true);
            return c;
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException("\u6784\u9020\u5668\u4e0d\u5b58\u5728: " + type.getName(), e);
        }
    }

    private static Method method(Class<?> type, String name, Class<?> ... params) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Method m = current.getDeclaredMethod(name, params);
                m.setAccessible(true);
                return m;
            }
            catch (NoSuchMethodException e) {
                continue;
            }
        }
        throw new IllegalStateException("\u65b9\u6cd5\u4e0d\u5b58\u5728: " + name + " on " + type.getName());
    }

    private static Field field(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field f = current.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            }
            catch (NoSuchFieldException e) {
                continue;
            }
        }
        throw new IllegalStateException("\u5b57\u6bb5\u4e0d\u5b58\u5728: " + name + " on " + type.getName());
    }
}

