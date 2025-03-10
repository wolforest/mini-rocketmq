package cn.coderule.minimq.rpc.common.netty;

import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.minimq.rpc.common.RpcServer;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.netty.codec.NettyDecoder;
import cn.coderule.minimq.rpc.common.netty.codec.NettyEncoder;
import cn.coderule.minimq.rpc.common.netty.event.NettyEventExecutor;
import cn.coderule.minimq.rpc.common.netty.event.RpcListener;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.netty.handler.NettyServerHandler;
import cn.coderule.minimq.rpc.common.netty.handler.RequestCodeCounter;
import cn.coderule.minimq.rpc.common.netty.handler.ServerConnectionHandler;
import cn.coderule.minimq.rpc.common.netty.service.NettyMonitor;
import cn.coderule.minimq.rpc.common.netty.service.NettyService;
import cn.coderule.minimq.rpc.common.config.RpcServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer extends NettyService implements RpcServer {
    private final RpcServerConfig config;

    private final ServerBootstrap bootstrap;
    private final DefaultEventExecutorGroup eventExecutorGroup;
    private final NettyEventExecutor nettyEventExecutor;
    private final NettyMonitor monitor;

    // sharable handlers
    private NettyEncoder encoder;
    private ServerConnectionHandler connectionHandler;
    private NettyServerHandler serverHandler;
    private RequestCodeCounter requestCodeCounter;

    public NettyServer(RpcServerConfig config) {
        this(config, null);
    }

    public NettyServer(RpcServerConfig config, RpcListener rpcListener) {
        super(config.getOnewaySemaphorePermits(), config.getAsyncSemaphorePermits(), config.getCallbackThreadNum());
        this.config = config;
        this.bootstrap = new ServerBootstrap();

        this.nettyEventExecutor = new NettyEventExecutor(rpcListener);
        this.eventExecutorGroup = buildEventExecutorGroup();

        initBootstrap();
        this.monitor = new NettyMonitor(config, requestCodeCounter);
    }

    @Override
    public void start() {
        startServer();

        nettyEventExecutor.start();
        invoker.start();
        dispatcher.start();

        monitor.start();
    }

    @Override
    public void shutdown() {

    }

    @Override
    public RpcCommand invokeSync(Channel channel, RpcCommand request, long timeoutMillis) throws Exception {
        return null;
    }

    @Override
    public void invokeAsync(Channel channel, RpcCommand request, long timeoutMillis,
        RpcCallback callback) throws Exception {

    }

    @Override
    public void invokeOneway(Channel channel, RpcCommand request, long timeoutMillis) throws Exception {

    }

    private void startServer() {
        try {
            ChannelFuture future = bootstrap.bind().sync();
            InetSocketAddress addr = (InetSocketAddress) future.channel().localAddress();
            if (0 == config.getPort()) {
                config.setPort(addr.getPort());
            }
            log.info("server start success, listen at {}:{}", config.getAddress(), config.getPort());
        } catch (Exception e) {
            throw new IllegalStateException("server start failed");
        }
    }

    private DefaultEventExecutorGroup buildEventExecutorGroup() {
        return new DefaultEventExecutorGroup(
            config.getBusinessThreadNum(), new DefaultThreadFactory("NettyWorker_")
        );
    }

    private boolean useEpoll() {
        return SystemUtil.isLinux()
            && config.isUseEpoll()
            && Epoll.isAvailable();
    }

    private void initBootstrap() {
        initHandlers();

        bootstrap.group(buildBossGroup(), buildWorkerGroup())
            .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .option(ChannelOption.SO_REUSEADDR, true)
            .childOption(ChannelOption.SO_KEEPALIVE, false)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .localAddress(new InetSocketAddress(config.getAddress(), config.getPort()))
            .childHandler(buildChildHandler())
            ;

        addCustomConfig();
    }

    private void initHandlers() {
        encoder = new NettyEncoder();
        connectionHandler = new ServerConnectionHandler(nettyEventExecutor);
        serverHandler = new NettyServerHandler(dispatcher);
        requestCodeCounter = new RequestCodeCounter();
    }

    private void addCustomConfig() {
        if (config.getSendBufferSize() > 0) {
            log.info("server set SO_SNDBUF to {}", config.getSendBufferSize());
            bootstrap.childOption(ChannelOption.SO_SNDBUF, config.getSendBufferSize());
        }
        if (config.getReceiveBufferSize() > 0) {
            log.info("server set SO_RCVBUF to {}", config.getReceiveBufferSize());
            bootstrap.childOption(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize());
        }
        if (config.getWriteBufferLowWater() > 0 && config.getWriteBufferHighWater() > 0) {
            log.info("server set netty WRITE_BUFFER_WATER_MARK to {},{}",
                config.getWriteBufferLowWater(), config.getWriteBufferHighWater());
            bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
                config.getWriteBufferLowWater(), config.getWriteBufferHighWater()));
        }

        if (config.isEnableNettyPool()) {
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
    }

    private ChannelInitializer<SocketChannel> buildChildHandler() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline().addLast(
                    eventExecutorGroup,
                    encoder,
                    new NettyDecoder(),
                    requestCodeCounter,
                    new IdleStateHandler(0, 0, config.getMaxChannelIdle()),
                    connectionHandler,
                    serverHandler
                );
            }
        };
    }

    private EventLoopGroup buildBossGroup() {
        if (useEpoll()) {
            return new EpollEventLoopGroup(config.getBossThreadNum(), new DefaultThreadFactory("NettyEpollBoss_"));
        } else {
            return new NioEventLoopGroup(config.getBossThreadNum(), new DefaultThreadFactory("NettyNioBoss_"));
        }
    }

    private EventLoopGroup buildWorkerGroup() {
        if (useEpoll()) {
            return new EpollEventLoopGroup(config.getWorkerThreadNum(), new DefaultThreadFactory("NettyEpollBoss_"));
        } else {
            return new NioEventLoopGroup(config.getWorkerThreadNum(), new DefaultThreadFactory("NettyNioBoss_"));
        }
    }
}
