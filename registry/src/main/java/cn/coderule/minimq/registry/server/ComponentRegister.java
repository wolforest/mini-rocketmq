package cn.coderule.minimq.registry.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.broker.BrokerManager;
import cn.coderule.minimq.registry.domain.kv.KVManager;
import cn.coderule.minimq.registry.domain.kv.KVService;
import cn.coderule.minimq.registry.domain.property.PropertyService;
import cn.coderule.minimq.registry.domain.store.StoreManager;
import cn.coderule.minimq.registry.domain.store.service.ChannelCloser;
import cn.coderule.minimq.registry.processor.KVProcessor;
import cn.coderule.minimq.registry.processor.PropertyProcessor;
import cn.coderule.minimq.registry.server.context.RegistryContext;
import cn.coderule.minimq.registry.server.rpc.ConnectionCloser;
import cn.coderule.minimq.registry.server.rpc.HaClient;
import cn.coderule.minimq.registry.server.rpc.RegistryServer;
import cn.coderule.minimq.registry.server.rpc.ServerManager;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.config.RpcServerConfig;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();
        RegistryContext.register(register);

        return register.execute();
    }

    public LifecycleManager execute() {
        registerExecutor();
        registerHaClient();

        registerProperty();
        registerKV();
        registerStore();
        registerBroker();

        registerServer();
        return this.manager;
    }

    private void registerExecutor() {
        RegistryConfig config = RegistryContext.getBean(RegistryConfig.class);
        ExecutorFactory factory = new ExecutorFactory(config);

        manager.register(factory);
        RegistryContext.register(factory);
    }

    private void registerHaClient() {
        HaClient haClient = new HaClient(
            RegistryContext.getBean(RegistryConfig.class),
            RegistryContext.getBean(RpcClientConfig.class)
        );

        manager.register(haClient);
        RegistryContext.register(haClient);
    }

    private void registerServer() {
        ServerManager serverManager = new ServerManager();
        manager.register(serverManager);
    }

    private void registerProperty() {
        PropertyService propertyService = new PropertyService(
            RegistryContext.getBean(RegistryConfig.class),
            RegistryContext.getBean(RpcServerConfig.class)
        );

        ExecutorFactory factory = RegistryContext.getBean(ExecutorFactory.class);
        PropertyProcessor processor = new PropertyProcessor(propertyService, factory.getDefaultExecutor());

        RegistryContext.register(propertyService);
        RegistryContext.register(processor);
    }

    private void registerKV() {
        manager.register(new KVManager());
    }

    private void registerBroker() {
        manager.register(new BrokerManager());
    }

    private void registerStore() {
        manager.register(new StoreManager());
    }

}
