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
package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcCommand> {
    private final NettyDispatcher dispatcher;

    public NettyServerHandler(NettyDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcCommand msg) {
        RpcContext context = new RpcContext(ctx);
        this.dispatcher.dispatch(context, msg);

        // The related remoting server has been shutdown, so close the connected channel
        NettyHelper.close(ctx.channel());
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        changeAutoRead(ctx);
        super.channelWritabilityChanged(ctx);
    }

    private void changeAutoRead(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();

        if (!channel.isWritable()) {
            channel.config().setAutoRead(false);
            log.warn("Channel[{}] auto-read is disabled, bytes to drain before it turns writable: {}",
                NettyHelper.getRemoteAddr(channel), channel.bytesBeforeWritable());
            return;
        }

        if (channel.config().isAutoRead()) {
            return;
        }

        channel.config().setAutoRead(true);
        log.info("Channel[{}] turns writable, bytes to buffer before changing channel to un-writable: {}",
            NettyHelper.getRemoteAddr(channel), channel.bytesBeforeUnwritable());
    }
}

