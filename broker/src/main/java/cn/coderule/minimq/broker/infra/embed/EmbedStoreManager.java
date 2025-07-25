package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.store.Store;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.bootstrap.StoreArgument;

public class EmbedStoreManager implements Lifecycle {
    private Store store;

    @Override
    public void initialize() throws Exception {
        StoreArgument storeArgument = new StoreArgument();
        store = new Store(storeArgument);
        store.initialize();
    }

    @Override
    public void start() throws Exception {
        store.start();

        ApplicationContext storeApi = StoreContext.API;
        BrokerContext.registerContext(storeApi);
    }

    @Override
    public void shutdown() throws Exception {
        store.shutdown();
    }

    @Override
    public void cleanup() throws Exception {
        store.cleanup();
    }

}
