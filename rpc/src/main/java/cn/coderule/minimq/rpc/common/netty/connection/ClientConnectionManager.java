/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.coderule.minimq.rpc.common.netty.connection;

import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.rpc.common.netty.NettyClient;
import cn.coderule.minimq.rpc.common.netty.event.NettyEvent;
import cn.coderule.minimq.rpc.common.netty.event.NettyEventExecutor;
import cn.coderule.minimq.rpc.common.netty.event.NettyEventType;
import cn.coderule.minimq.rpc.common.netty.service.NettyHelper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientConnectionManager extends ChannelDuplexHandler {
    private final NettyEventExecutor eventExecutor;
    private final NettyClient nettyClient;

    public ClientConnectionManager(NettyClient nettyClient, NettyEventExecutor eventExecutor) {
        this.nettyClient = nettyClient;
        this.eventExecutor = eventExecutor;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
        ChannelPromise promise) throws Exception {
        final String local = localAddress == null ? "UNKNOWN" : NetworkUtil.getAddress(localAddress);
        final String remote = remoteAddress == null ? "UNKNOWN" : NetworkUtil.getAddress(remoteAddress);
        log.info("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);

        super.connect(ctx, remoteAddress, localAddress, promise);

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.CONNECT, remote, ctx.channel()));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY CLIENT PIPELINE: ACTIVE, {}", remoteAddress);
        super.channelActive(ctx);

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.ACTIVE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
        nettyClient.closeChannel(ctx.channel());
        super.disconnect(ctx, promise);

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
        nettyClient.closeChannel(ctx.channel());
        super.close(ctx, promise);
        nettyClient.failFast(ctx.channel());
        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY CLIENT PIPELINE: channelInactive, the channel[{}]", remoteAddress);
        nettyClient.closeChannel(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent event)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }

        if (!event.state().equals(IdleState.ALL_IDLE)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }

        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.warn("NETTY CLIENT PIPELINE: IDLE exception [{}]", remoteAddress);
        nettyClient.closeChannel(ctx.channel());
        if (eventExecutor.getRpcListener() != null) {
            NettyEvent idleEvent = new NettyEvent(NettyEventType.IDLE, remoteAddress, ctx.channel());
            eventExecutor.putNettyEvent(idleEvent);
        }

        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
        log.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
        nettyClient.closeChannel(ctx.channel());
        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
        }
    }

}

