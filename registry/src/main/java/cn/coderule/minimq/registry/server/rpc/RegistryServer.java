package cn.coderule.minimq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.processor.ClusterProcessor;
import cn.coderule.minimq.registry.processor.KVProcessor;
import cn.coderule.minimq.registry.processor.PropertyProcessor;
import cn.coderule.minimq.registry.processor.RegistryProcessor;
import cn.coderule.minimq.registry.processor.RouteProcessor;
import cn.coderule.minimq.registry.processor.TopicProcessor;
import cn.coderule.minimq.registry.server.context.RegistryContext;
import cn.coderule.minimq.rpc.common.RpcServer;
import cn.coderule.minimq.rpc.common.config.RpcServerConfig;
import cn.coderule.minimq.rpc.common.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryServer implements Lifecycle {
    private final RegistryConfig registryConfig;

    private final RpcServer rpcServer;

    public RegistryServer(RegistryConfig registryConfig, RpcServerConfig serverConfig, ConnectionCloser connectionCloser) {
        this.registryConfig = registryConfig;

        this.rpcServer = new NettyServer(serverConfig, connectionCloser);
    }

    @Override
    public void initialize() {
        registerProcessor();
    }

    @Override
    public void start() {
        rpcServer.start();
    }

    @Override
    public void shutdown() {
        rpcServer.shutdown();
    }

    private void registerProcessor() {
        rpcServer.registerProcessor(RegistryContext.getBean(KVProcessor.class));
        rpcServer.registerProcessor(RegistryContext.getBean(PropertyProcessor.class));
        rpcServer.registerProcessor(RegistryContext.getBean(ClusterProcessor.class));
        rpcServer.registerProcessor(RegistryContext.getBean(RegistryProcessor.class));
        rpcServer.registerProcessor(RegistryContext.getBean(RouteProcessor.class));
        rpcServer.registerProcessor(RegistryContext.getBean(TopicProcessor.class));
    }
}
