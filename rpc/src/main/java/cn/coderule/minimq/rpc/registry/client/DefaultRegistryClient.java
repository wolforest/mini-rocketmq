package cn.coderule.minimq.rpc.registry.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.netty.NettyClient;
import cn.coderule.minimq.rpc.common.netty.service.ChannelWrapper;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.cluster.BrokerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultRegistryClient implements RegistryClient, Lifecycle {
    private final NettyClient nettyClient;

    private final AtomicReference<List<String>> addressList;
    private final AtomicReference<String> activeAddress;

    public DefaultRegistryClient(RpcClientConfig config, String addressConfig) {
        nettyClient = new NettyClient(config);

        this.addressList = new AtomicReference<>();
        this.activeAddress = new AtomicReference<>();

        setRegistryList(addressConfig);
    }

    @Override
    public void setRegistryList(List<String> addrs) {
        if (CollectionUtil.isEmpty(addrs)) {
            return;
        }

        List<String> preList = this.addressList.get();
        if (!CollectionUtil.isDifferent(preList, addrs)) {
            return;
        }

        Collections.shuffle(addrs);
        this.addressList.set(addrs);
        log.info("set registry address list, pre: {}; new: {}", preList, addrs);

        closeActiveChannel(addrs);
    }

    @Override
    public void setRegistryList(String addressConfig) {
        if (StringUtil.isBlank(addressConfig)) {
            return;
        }

        String[] arr = addressConfig.split(";");
        if (arr.length == 0) {
            return;
        }
        setRegistryList(List.of(arr));
    }

    @Override
    public List<String> getRegistryList() {
        return List.of();
    }

    @Override
    public void registerBroker(BrokerInfo brokerInfo) {

    }

    @Override
    public void unregisterBroker(ServerInfo serverInfo) {

    }

    @Override
    public void brokerHeartbeat(HeartBeat heartBeat) {

    }

    @Override
    public void registerStore(StoreInfo storeInfo) {

    }

    @Override
    public void unregisterStore(ServerInfo serverInfo) {

    }

    @Override
    public void storeHeartbeat(HeartBeat heartBeat) {

    }

    @Override
    public void registerTopic(TopicInfo topicInfo) {

    }

    @Override
    public ClusterInfo syncClusterInfo(String clusterName) {
        return null;
    }

    @Override
    public GroupInfo syncGroupInfo(String clusterName, String groupName) {
        return null;
    }

    @Override
    public RouteInfo syncRouteInfo(String topicName, long timeout) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    private void closeActiveChannel(List<String> addrs) {
        String activeAddr = this.activeAddress.get();
        if (null == activeAddr || addrs.contains(activeAddr)) {
            return;
        }

        nettyClient.closeChannel(activeAddr);

//        for (String addr: this.addressMap.keySet()) {
//            if (!addr.contains(activeAddr)) {
//                continue;
//            }
//
//            ChannelWrapper channel = this.addressMap.get(addr);
//            if (channel != null) {
//                channel.close();
//            }
//        }
    }
}
