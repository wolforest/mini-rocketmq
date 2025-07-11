package cn.coderule.minimq.store.server.ha;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.store.server.ha.client.HAClient;
import cn.coderule.minimq.store.server.ha.core.HAContext;
import cn.coderule.minimq.store.server.ha.core.monitor.StateMonitor;
import cn.coderule.minimq.store.server.ha.core.monitor.StateRequest;
import cn.coderule.minimq.store.server.ha.server.HAServer;
import cn.coderule.minimq.store.server.ha.server.processor.SlaveMonitor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HAService implements Lifecycle {
    private final StateMonitor stateMonitor;
    private final SlaveMonitor slaveMonitor;

    private final HAServer haServer;
    private final HAClient haClient;

    public HAService(HAContext context) {
        this.haServer = context.getHaServer();
        this.haClient = context.getHaClient();

        this.stateMonitor = new StateMonitor(
            context.getStoreConfig(),
            context.getHaServer(),
            context.getHaClient()
        );

        this.slaveMonitor = context.getSlaveMonitor();
    }

    public void updateMasterAddress(String addr) {
        if (haClient == null) {
            return;
        }

        haClient.setMasterAddress(addr);
    }

    public void updateMasterHaAddress(String addr) {
        if (haClient == null) {
            return;
        }

        haClient.setMasterHaAddress(addr);
    }

    public void monitorState(StateRequest request) {
        this.stateMonitor.setRequest(request);
    }

    @Override
    public void start() {
        this.stateMonitor.start();
    }

    @Override
    public void shutdown() {
        this.stateMonitor.stop();
    }
}
