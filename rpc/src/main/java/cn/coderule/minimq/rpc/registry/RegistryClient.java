package cn.coderule.minimq.rpc.registry;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.minimq.rpc.registry.protocol.cluster.BrokerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import java.util.List;

public interface RegistryClient extends Lifecycle {
    List<String> getRegistryList();
    void setRegistryList(List<String> addressList);
    void setRegistryList(String addressConfig);
    String chooseRegistry() throws InterruptedException;

    void registerBroker(BrokerInfo brokerInfo);
    void unregisterBroker(ServerInfo serverInfo);
    void brokerHeartbeat(HeartBeat heartBeat);

    List<RegisterStoreResult> registerStore(StoreInfo storeInfo);
    void unregisterStore(ServerInfo serverInfo);
    void storeHeartbeat(HeartBeat heartBeat);
    void registerTopic(TopicInfo topicInfo);

    ClusterInfo syncClusterInfo(String clusterName) throws Exception;
    GroupInfo syncGroupInfo(String clusterName, String groupName) throws Exception;
    RouteInfo syncRouteInfo(String topicName, long timeout) throws Exception;
}
