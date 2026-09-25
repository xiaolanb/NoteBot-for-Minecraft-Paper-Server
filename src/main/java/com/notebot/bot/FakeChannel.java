/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.AbstractChannel
 *  io.netty.channel.AbstractChannel$AbstractUnsafe
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelConfig
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelMetadata
 *  io.netty.channel.ChannelOutboundBuffer
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.ChannelPromise
 *  io.netty.channel.DefaultChannelConfig
 *  io.netty.channel.DefaultEventLoop
 *  io.netty.channel.EventLoop
 */
package com.notebot.bot;

import com.notebot.bot.FakeChannelPipeline;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class FakeChannel
extends AbstractChannel {
    private static final EventLoop EVENT_LOOP = new DefaultEventLoop();
    private static final SocketAddress LOOPBACK = new InetSocketAddress(InetAddress.getLoopbackAddress(), 25565);
    private final ChannelConfig config = new DefaultChannelConfig((Channel)this);
    private final ChannelPipeline pipeline = new FakeChannelPipeline((Channel)this);
    private volatile boolean closed;

    public FakeChannel() {
        super(null);
    }

    public ChannelConfig config() {
        this.config.setAutoRead(true);
        return this.config;
    }

    protected void doBeginRead() {
    }

    protected void doBind(SocketAddress localAddress) {
    }

    protected void doClose() {
        this.closed = true;
    }

    protected void doDisconnect() {
        this.closed = true;
    }

    protected void doWrite(ChannelOutboundBuffer in) {
        Object message;
        while ((message = in.current()) != null) {
            in.remove();
        }
    }

    public boolean isActive() {
        return !this.closed;
    }

    protected boolean isCompatible(EventLoop loop) {
        return true;
    }

    public boolean isOpen() {
        return !this.closed;
    }

    public ChannelFuture close() {
        this.closed = true;
        return this.newPromise().setSuccess();
    }

    public ChannelFuture close(ChannelPromise promise) {
        this.closed = true;
        promise.setSuccess();
        return promise;
    }

    public ChannelFuture disconnect() {
        this.closed = true;
        return this.newPromise().setSuccess();
    }

    public ChannelFuture disconnect(ChannelPromise promise) {
        this.closed = true;
        promise.setSuccess();
        return promise;
    }

    public ChannelPipeline pipeline() {
        return this.pipeline;
    }

    protected SocketAddress localAddress0() {
        return LOOPBACK;
    }

    public ChannelMetadata metadata() {
        return new ChannelMetadata(true);
    }

    protected AbstractChannel.AbstractUnsafe newUnsafe() {
        return new AbstractUnsafe(){

            public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
                this.safeSetSuccess(promise);
            }
        };
    }

    protected SocketAddress remoteAddress0() {
        return LOOPBACK;
    }

    public EventLoop eventLoop() {
        return EVENT_LOOP;
    }

    public Channel parent() {
        return null;
    }
}

