package cn.coderule.minimq.store.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.message.TopicConfig;
import cn.coderule.minimq.domain.service.store.api.meta.SubscriptionStore;
import cn.coderule.minimq.domain.service.store.api.meta.TopicStore;
import cn.coderule.minimq.rpc.common.rpc.config.RpcServerConfig;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.rpc.processor.SubscriptionProcessor;
import cn.coderule.minimq.store.server.rpc.processor.TopicProcessor;
import cn.coderule.minimq.store.server.rpc.server.ConfigConverter;
import cn.coderule.minimq.store.server.rpc.server.ConnectionManager;
import cn.coderule.minimq.store.server.rpc.server.ExecutorManager;
import cn.coderule.minimq.store.server.rpc.server.StoreServer;
import java.util.concurrent.ExecutorService;

public class RpcManager implements Lifecycle {
    private StoreConfig storeConfig;
    private ExecutorManager executorManager;
    private StoreServer storeServer;

    @Override
    public void initialize() throws Exception {
        storeConfig = StoreContext.getBean(StoreConfig.class);

        initExecutor();
        initServer();
        initProcessor();
    }

    @Override
    public void start() throws Exception {
        if (null != executorManager) executorManager.start();
        if (null != storeServer) storeServer.start();
    }

    @Override
    public void shutdown() throws Exception {
        if (null != executorManager) executorManager.shutdown();
        if (null != storeServer) storeServer.shutdown();
    }


    private void initExecutor() {
        executorManager = new ExecutorManager(storeConfig);
        try {
            executorManager.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initServer() {
        ConnectionManager connectionManager = new ConnectionManager();
        RpcServerConfig serverConfig = ConfigConverter.toServerConfig(storeConfig);
        storeServer = new StoreServer(storeConfig, serverConfig, connectionManager);
    }

    private void initProcessor() {
        initTopicProcessor();
        initSubscriptionProcessor();
    }

    private void initTopicProcessor() {
        TopicConfig topicConfig = StoreContext.getBean(TopicConfig.class);
        TopicStore topicStore = StoreContext.getBean(TopicStore.class);
        ExecutorService executor = executorManager.getAdminExecutor();

        TopicProcessor topicProcessor = new TopicProcessor(topicConfig, topicStore, executor);
        storeServer.registerProcessor(topicProcessor);
    }

    private void initSubscriptionProcessor() {
        SubscriptionStore subscriptionStore = StoreContext.getBean(SubscriptionStore.class);
        ExecutorService executor = executorManager.getAdminExecutor();

        SubscriptionProcessor subscriptionProcessor = new SubscriptionProcessor(subscriptionStore, executor);
        storeServer.registerProcessor(subscriptionProcessor);
    }

}
