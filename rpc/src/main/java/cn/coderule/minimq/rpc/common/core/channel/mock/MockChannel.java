
package cn.coderule.minimq.rpc.common.core.channel.mock;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.rpc.common.core.channel.invocation.InvocationContextInterface;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;


/**
 * SimpleChannel is used to handle writeAndFlush situation in processor
 *
 * @see ChannelHandlerContext#writeAndFlush
 * @see Channel#writeAndFlush
 */
@Slf4j
public class MockChannel extends AbstractChannel {

    protected final String remoteAddress;
    protected final String localAddress;

    protected long lastAccessTime;
    protected ChannelHandlerContext channelHandlerContext;

    /**
     * Creates a new instance.
     *
     * @param parent        the parent of this channel. {@code null} if there's no parent.
     * @param remoteAddress Remote address
     * @param localAddress  Local address
     */
    public MockChannel(Channel parent, String remoteAddress, String localAddress) {
        this(parent, null, remoteAddress, localAddress);
    }

    public MockChannel(Channel parent, ChannelId id, String remoteAddress, String localAddress) {
        super(parent, id);
        lastAccessTime = System.currentTimeMillis();
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.channelHandlerContext = new MockContext(this);
    }

    public MockChannel(String remoteAddress, String localAddress) {
        this(null, remoteAddress, localAddress);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return null;
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return false;
    }

    private static SocketAddress parseSocketAddress(String address) {
        if (StringUtil.isBlank(address)) {
            return null;
        }

        String[] segments = address.split(":");
        if (2 == segments.length) {
            return new InetSocketAddress(segments[0], Integer.parseInt(segments[1]));
        }

        return null;
    }

    @Override
    protected SocketAddress localAddress0() {
        return parseSocketAddress(localAddress);
    }

    @Override
    public SocketAddress localAddress() {
        return localAddress0();
    }

    @Override
    public SocketAddress remoteAddress() {
        return remoteAddress0();
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return parseSocketAddress(remoteAddress);
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {

    }

    @Override
    protected void doDisconnect() throws Exception {

    }

    @Override
    public ChannelFuture close() {
        DefaultChannelPromise promise = new DefaultChannelPromise(this, GlobalEventExecutor.INSTANCE);
        promise.setSuccess();
        return promise;
    }

    @Override
    protected void doClose() throws Exception {

    }

    @Override
    protected void doBeginRead() throws Exception {

    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {

    }

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isActive() {
        return (System.currentTimeMillis() - lastAccessTime) <= 120L * 1000;
    }

    @Override
    public ChannelMetadata metadata() {
        return null;
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        DefaultChannelPromise promise = new DefaultChannelPromise(this, GlobalEventExecutor.INSTANCE);
        promise.setSuccess();
        return promise;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    public void updateLastAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    public void registerInvocationContext(int opaque, InvocationContextInterface context) {

    }

    public void eraseInvocationContext(int opaque) {

    }

    public void clearExpireContext() {

    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }
}
