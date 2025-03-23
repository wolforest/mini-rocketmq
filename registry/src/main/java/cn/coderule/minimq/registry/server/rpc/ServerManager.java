package cn.coderule.minimq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.store.service.ChannelCloser;
import cn.coderule.minimq.registry.server.context.RegistryContext;
import cn.coderule.minimq.rpc.common.config.RpcServerConfig;

public class ServerManager implements Lifecycle {
    private RegistryServer server;

    @Override
    public void initialize() {
        ChannelCloser channelCloser = RegistryContext.getBean(ChannelCloser.class);
        ConnectionCloser connectionCloser = new ConnectionCloser(channelCloser);

        RegistryConfig registryConfig = RegistryContext.getBean(RegistryConfig.class);
        RpcServerConfig serverConfig = RegistryContext.getBean(RpcServerConfig.class);
        server = new RegistryServer(registryConfig, serverConfig, connectionCloser);
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void shutdown() {
        server.shutdown();
    }

}
