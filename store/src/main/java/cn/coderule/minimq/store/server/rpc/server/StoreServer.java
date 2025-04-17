package cn.coderule.minimq.store.server.rpc.server;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.rpc.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.rpc.RpcServer;
import cn.coderule.minimq.rpc.rpc.config.RpcServerConfig;
import cn.coderule.minimq.rpc.rpc.netty.NettyServer;
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
    }

    public void registerProcessor(RpcProcessor processor) {
        rpcServer.registerProcessor(processor);
    }
}
