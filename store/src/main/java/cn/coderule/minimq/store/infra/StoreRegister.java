package cn.coderule.minimq.store.infra;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.constant.PermName;
import cn.coderule.minimq.domain.model.DataVersion;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.model.meta.TopicMap;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.client.DefaultRegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigSerializeWrapper;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import cn.coderule.minimq.store.server.StoreContext;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreRegister implements Lifecycle {
    private final StoreConfig storeConfig;
    private final RegistryClient registryClient;
    private final ScheduledExecutorService heartbeatScheduler;


    public StoreRegister(StoreConfig storeConfig) {
        this.registryClient = new DefaultRegistryClient(
            new RpcClientConfig(),
            storeConfig.getRegistryAddress()
        );
        this.storeConfig = storeConfig;

        heartbeatScheduler = ThreadUtil.newScheduledThreadPool(
            1,
            new DefaultThreadFactory("StoreHeartbeatThread_")
        );
    }

    @Override
    public void start() {
        registerStore();
        startHeartbeat();
    }

    @Override
    public void shutdown() {
        unregisterStore();
        heartbeatScheduler.shutdown();
    }

    private void registerStore() {
        TopicService topicService = StoreContext.getBean(TopicService.class);
        TopicMap topicMap = topicService.getTopicMap();

        TopicConfigSerializeWrapper topicInfo = new TopicConfigSerializeWrapper();
        topicInfo.setTopicConfigTable(topicMap.getTopicTable());
        topicInfo.setDataVersion(topicMap.getVersion());

        StoreInfo storeInfo = StoreInfo.builder()
            .clusterName(storeConfig.getCluster())
            .groupName(storeConfig.getGroup())
            .groupNo(storeConfig.getGroupNo())
            .enableMasterElection(storeConfig.isEnableMasterElection())
            .address(storeConfig.getHost() + ":" + storeConfig.getPort())
            .haAddress(storeConfig.getHost() + ":" + storeConfig.getHaPort())
            .topicInfo(topicInfo)
            .filterList(List.of())
            .build();


        registryClient.registerStore(storeInfo);
    }

    private void unregisterStore() {
        ServerInfo serverInfo = ServerInfo.builder()
            .clusterName(storeConfig.getCluster())
            .groupName(storeConfig.getGroup())
            .groupNo(storeConfig.getGroupNo())
            .address(storeConfig.getHost() + ":" + storeConfig.getPort())
            .build();

        registryClient.unregisterStore(serverInfo);
    }

    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(
            this::heartbeat,
            1000,
            storeConfig.getRegistryHeartbeatInterval(),
            TimeUnit.MILLISECONDS
        );
    }

    private void heartbeat() {
        if (!storeConfig.isEnableMasterElection()) {
            return;
        }

        TopicService topicService = StoreContext.getBean(TopicService.class);
        DataVersion version = topicService.getTopicMap().getVersion();

        HeartBeat heartBeat = HeartBeat.builder()
            .clusterName(storeConfig.getCluster())
            .groupName(storeConfig.getGroup())
            .groupNo(storeConfig.getGroupNo())
            .address(storeConfig.getHost() + ":" + storeConfig.getPort())
            .heartbeatInterval(storeConfig.getRegistryHeartbeatInterval())
            .heartbeatTimeout(storeConfig.getRegistryHeartbeatTimeout())
            .inContainer(storeConfig.isInContainer())
            .version(version)
            .build();

        try {
            registryClient.storeHeartbeat(heartBeat);
        } catch (Exception e) {
            log.error("store registry heartbeat error", e);
        }
    }

    public void registerTopic(Topic topic) {
        Topic registerTopic = topic;
        if (!PermName.isWriteable(storeConfig.getPermission())
            || !PermName.isReadable(storeConfig.getPermission())) {
            registerTopic = new Topic(topic);
            registerTopic.setPerm(topic.getPerm() & storeConfig.getPermission());
        }

        TopicInfo topicInfo = TopicInfo.builder()
                .groupName(storeConfig.getGroup())
                .topic(registerTopic)
                .build();

        registryClient.registerTopic(topicInfo);
    }


}
