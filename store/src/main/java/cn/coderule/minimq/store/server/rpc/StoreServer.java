package cn.coderule.minimq.store.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.rpc.common.RpcServer;
import cn.coderule.minimq.rpc.common.config.RpcServerConfig;
import cn.coderule.minimq.rpc.common.netty.NettyServer;
import cn.coderule.minimq.store.server.StoreContext;
import cn.coderule.minimq.store.server.rpc.processor.GroupProcessor;
import cn.coderule.minimq.store.server.rpc.processor.TopicProcessor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreServer implements Lifecycle {
    private final StoreConfig storeConfig;
    private final RpcServerConfig serverConfig;

    private final RpcServer rpcServer;

    public StoreServer(StoreConfig storeConfig, RpcServerConfig serverConfig, ConnectionManager connectionManager) {
        this.storeConfig = storeConfig;
        this.serverConfig = serverConfig;

        this.rpcServer = new NettyServer(serverConfig, connectionManager);
    }

    @Override
    public void start() {
        rpcServer.start();
    }

    @Override
    public void shutdown() {
        rpcServer.shutdown();
    }

    @Override
    public void initialize() {
        rpcServer.registerProcessor(StoreContext.getBean(TopicProcessor.class));
        rpcServer.registerProcessor(StoreContext.getBean(GroupProcessor.class));
    }
}
