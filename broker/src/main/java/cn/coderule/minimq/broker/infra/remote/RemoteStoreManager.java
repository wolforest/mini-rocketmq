package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.rpc.registry.route.RouteLoader;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class RemoteStoreManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private RemoteLoadBalance loadBalance;

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        if (!this.brokerConfig.isEnableRemoteStore()) {
            return;
        }

        RouteLoader routeLoader = BrokerContext.getBean(RouteLoader.class);
        this.loadBalance = new RemoteLoadBalance(this.brokerConfig, routeLoader);


    }

    @Override
    public void start() throws Exception {
        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }
    }

}
