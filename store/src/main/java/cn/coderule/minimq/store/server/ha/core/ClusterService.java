package cn.coderule.minimq.store.server.ha.core;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.infra.StoreRegister;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * cluster service:
 * - communicate with registry
 *   - register master
 *   - register slave if no master registered
 *   - registry heartbeat
 * - initialize M/S
 *   - send ha info
 *   - handshake
 *   - syn metadata
 */
public class ClusterService implements Lifecycle {
    private final StoreConfig storeConfig;
    private final StoreRegister storeRegister;
    private final ScheduledExecutorService heartbeatScheduler;

    private boolean shouldRegister = true;

    public ClusterService(StoreConfig storeConfig, StoreRegister storeRegister) {
        this.storeConfig = storeConfig;
        this.storeRegister = storeRegister;

        heartbeatScheduler = ThreadUtil.newScheduledThreadPool(
            1,
            new DefaultThreadFactory("StoreHeartbeatThread_")
        );
    }

    @Override
    public void initialize() throws Exception {
    }

    @Override
    public void start() throws Exception {
        shouldRegister = storeConfig.isMaster();
        registerStore();
        startHeartbeat();
    }

    @Override
    public void shutdown() throws Exception {
        heartbeatScheduler.shutdown();
        unregisterStore();
    }

    private void registerStore() {
        if (!shouldRegister) {
            return;
        }

        storeRegister.registerStore(true, false, true);
    }

    private void unregisterStore() {
        if (!shouldRegister) {
            return;
        }

        storeRegister.unregisterStore();
    }

    private void startHeartbeat() {
        if (!shouldRegister) {
            return;
        }

        heartbeatScheduler.scheduleAtFixedRate(
            storeRegister::heartbeat,
            1000,
            storeConfig.getRegistryHeartbeatInterval(),
            TimeUnit.MILLISECONDS
        );
    }


}
