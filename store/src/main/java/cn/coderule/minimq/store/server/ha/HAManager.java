package cn.coderule.minimq.store.server.ha;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.infra.StoreRegister;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.core.ClusterService;

public class HAManager implements Lifecycle {
    private StoreConfig storeConfig;
    private ClusterService clusterService;

    @Override
    public void initialize() throws Exception {
        this.storeConfig = StoreContext.getBean(StoreConfig.class);
        StoreRegister storeRegister = StoreContext.getBean(StoreRegister.class);

        clusterService = new ClusterService(storeConfig, storeRegister);
    }

    @Override
    public void start() throws Exception {
        clusterService.start();
    }

    @Override
    public void shutdown() throws Exception {
        clusterService.shutdown();
    }

}
