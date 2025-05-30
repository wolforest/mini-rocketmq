
package cn.coderule.minimq.rpc.common.core.channel.mock;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import java.net.SocketAddress;
import org.apache.commons.lang3.NotImplementedException;

public class MockContext implements ChannelHandlerContext {

    private final Channel channel;

    public MockContext(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public EventExecutor executor() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String name() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandler handler() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public ChannelHandlerContext fireChannelRegistered() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandlerContext fireChannelUnregistered() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandlerContext fireChannelActive() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandlerContext fireChannelInactive() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandlerContext fireUserEventTriggered(Object evt) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandlerContext fireChannelRead(Object msg) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandlerContext fireChannelReadComplete() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandlerContext fireChannelWritabilityChanged() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture disconnect() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture close() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture deregister() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandlerContext read() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture write(Object msg) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelHandlerContext flush() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return channel.writeAndFlush(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return channel.writeAndFlush(msg);
    }

    @Override
    public ChannelPipeline pipeline() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ByteBufAllocator alloc() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelPromise newPromise() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChannelPromise voidPromise() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> attributeKey) {
        return false;
    }
}
