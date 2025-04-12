package cn.coderule.minimq.broker.domain.route;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.broker.domain.route.model.PublishInfo;
import cn.coderule.minimq.broker.domain.route.model.RouteCache;
import cn.coderule.minimq.broker.infra.BrokerRegister;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.exception.RpcException;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.utils.NamespaceUtil;
import cn.coderule.minimq.rpc.common.protocol.code.ResponseCode;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteLoader implements Lifecycle {
    private final BrokerConfig brokerConfig;

    private final BrokerRegister brokerRegister;
    private final RouteCache route;

    private ScheduledExecutorService scheduler;

    public RouteLoader(BrokerConfig brokerConfig, RouteCache route, BrokerRegister brokerRegister) {
        this.brokerConfig = brokerConfig;

        this.route = route;
        this.brokerRegister = brokerRegister;

        this.scheduler = ThreadUtil.newSingleScheduledThreadExecutor(
            new DefaultThreadFactory("BrokerRouteScheduler")
        );
    }

    @Override
    public void start() {
        this.scheduler.scheduleAtFixedRate(
            RouteLoader.this::load,
            1000,
            brokerConfig.getSyncRouteInterval(),
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void shutdown() {
        this.scheduler.shutdown();
    }

    public void updateRouteInfo(String topicName, boolean updatePubInfo, boolean updateSubInfo) {
        if (!route.tryLock()) {
            return;
        }

        try {
            RouteInfo routeInfo = brokerRegister.syncRouteInfo(topicName, brokerConfig.getSyncRouteTimeout());
            if (routeInfo == null) {
                log.warn("Load route info error, topic: {}", topicName);
                return;
            }

            if (updatePubInfo) {
                route.updateRoute(topicName, routeInfo);
            }

            if (updateSubInfo) {
                route.updateSubscription(topicName, routeInfo);
            }
        } catch (Exception e) {
            handleRouteUpdateException(e, topicName);
        } finally {
            route.unlock();
        }
    }

    public PublishInfo getPublishInfo(String topicName) {
        return null;
    }

    public Set<MessageQueue> getSubscriptionInfo(String topicName) {
        return null;
    }

    public String getAddressInPublish(String groupName) {
        return null;
    }

    public Set<String> getAddressInSubscription(String groupName, long groupNo, boolean inGroup) {
        return null;
    }


    private void handleRouteUpdateException(Exception e, String topicName) {
        log.error("Load route info Exception", e);

        if (!(e instanceof RpcException rpcException)) {
            return;
        }

        if (NamespaceUtil.isRetryTopic(topicName)) {
            return;
        }

        if (ResponseCode.TOPIC_NOT_EXIST != rpcException.getCode()) {
            return;
        }

        route.removeSubscription(topicName);
    }

    private void load() {
        try {
            Set<String> topicSet = route.getTopicSet();
            for (String topicName : topicSet) {
                boolean updatePubInfo = route.containsRoute(topicName);
                boolean updateSubInfo = route.containsSubscription(topicName);
                updateRouteInfo(topicName, updatePubInfo, updateSubInfo);
            }
        } catch (Exception e) {
            log.error("Load route info exception", e);
        }
    }


}
