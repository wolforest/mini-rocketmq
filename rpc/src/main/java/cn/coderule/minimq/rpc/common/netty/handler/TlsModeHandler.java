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
package cn.coderule.minimq.rpc.common.netty.handler;

import cn.coderule.minimq.rpc.common.enums.TlsMode;
import cn.coderule.minimq.rpc.common.netty.codec.FileRegionEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class TlsModeHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static final String TLS_MODE_HANDLER = "TlsModeHandler";
    public static final String TLS_HANDLER_NAME = "sslHandler";
    public static final String FILE_REGION_ENCODER_NAME = "fileRegionEncoder";
    private static final byte HANDSHAKE_MAGIC_CODE = 0x16;


    private final TlsMode tlsMode;
    private final EventExecutorGroup eventExecutorGroup;
    private final SslContext sslContext;


    public TlsModeHandler(TlsMode tlsMode, SslContext sslContext, EventExecutorGroup eventExecutorGroup) {
        this.tlsMode = tlsMode;
        this.sslContext = sslContext;
        this.eventExecutorGroup = eventExecutorGroup;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {

        // Peek the current read index byte to determine if the content is starting with TLS handshake
        byte b = msg.getByte(msg.readerIndex());

        if (b == HANDSHAKE_MAGIC_CODE) {
            switch (tlsMode) {
                case DISABLED:
                    ctx.close();
                    log.warn("Clients intend to establish an SSL connection while this server is running in SSL disabled mode");
                    throw new UnsupportedOperationException("The NettyRemotingServer in SSL disabled mode doesn't support ssl client");
                case PERMISSIVE:
                case ENFORCING:
                    if (null != sslContext) {
                        ctx.pipeline()
                            .addAfter(eventExecutorGroup, TLS_MODE_HANDLER, TLS_HANDLER_NAME, sslContext.newHandler(ctx.channel().alloc()))
                            .addAfter(eventExecutorGroup, TLS_HANDLER_NAME, FILE_REGION_ENCODER_NAME, new FileRegionEncoder());
                        log.info("Handlers prepended to channel pipeline to establish SSL connection");
                    } else {
                        ctx.close();
                        log.error("Trying to establish an SSL connection but sslContext is null");
                    }
                    break;

                default:
                    log.warn("Unknown TLS mode");
                    break;
            }
        } else if (tlsMode == TlsMode.ENFORCING) {
            ctx.close();
            log.warn("Clients intend to establish an insecure connection while this server is running in SSL enforcing mode");
        }

        try {
            // Remove this handler
            ctx.pipeline().remove(this);
        } catch (NoSuchElementException e) {
            log.error("Error while removing TlsModeHandler", e);
        }

        // Hand over this message to the next .
        ctx.fireChannelRead(msg.retain());
    }
}

