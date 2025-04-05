package cn.coderule.minimq.rpc.registry.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.netty.NettyClient;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterBrokerBody;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigAndMappingSerializeWrapper;
import cn.coderule.minimq.rpc.registry.protocol.cluster.BrokerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import cn.coderule.minimq.rpc.registry.service.RegistryManager;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultRegistryClient implements RegistryClient, Lifecycle {
    private final RpcClientConfig config;
    private final NettyClient nettyClient;
    private final RegistryManager registryManager;

    public DefaultRegistryClient(RpcClientConfig config, String addressConfig) {
        this.config = config;
        this.nettyClient = new NettyClient(config);

        registryManager = new RegistryManager(config, addressConfig, nettyClient);
    }

    @Override
    public void start() {
        this.nettyClient.start();
        this.registryManager.start();
    }

    @Override
    public void shutdown() {
        try {
            this.nettyClient.shutdown();
            this.registryManager.shutdown();
        } catch (Exception e) {
            log.error("shutdown client error", e);
        }
    }

    @Override
    public void setRegistryList(List<String> addrs) {
        registryManager.setRegistryList(addrs);
    }

    @Override
    public void setRegistryList(String addressConfig) {
        registryManager.setRegistryList(addressConfig);
    }

    @Override
    public String chooseRegistry() throws InterruptedException {
        return registryManager.chooseRegistry();
    }

    @Override
    public List<String> getRegistryList() {
        return registryManager.getRegistryList();
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

    private RegisterBrokerRequestHeader createRegisterStoreRequestHeader(StoreInfo storeInfo) {
        RegisterBrokerRequestHeader requestHeader = new RegisterBrokerRequestHeader();
        requestHeader.setBrokerAddr(storeInfo.getAddress());
        requestHeader.setBrokerName(storeInfo.getGroupName());
        requestHeader.setBrokerId(storeInfo.getGroupNo());
        requestHeader.setClusterName(storeInfo.getClusterName());
        requestHeader.setHaServerAddr(storeInfo.getHaAddress());
        requestHeader.setHeartbeatTimeoutMillis(storeInfo.getHeartbeatTimeout().longValue());
        requestHeader.setEnableActingMaster(storeInfo.isEnableMasterElection());

        return requestHeader;
    }

    private RegisterBrokerBody createRegisterStoreRequestBody(StoreInfo storeInfo) {
        RegisterBrokerBody requestBody = new RegisterBrokerBody();
        requestBody.setFilterServerList(storeInfo.getFilterList());
        requestBody.setTopicConfigSerializeWrapper(
            TopicConfigAndMappingSerializeWrapper.from(storeInfo.getTopicInfo())
        );

        return requestBody;
    }

    private RpcCommand createRegisterStoreRequest(StoreInfo storeInfo) {
        RegisterBrokerRequestHeader requestHeader = createRegisterStoreRequestHeader(storeInfo);
        RegisterBrokerBody requestBody = createRegisterStoreRequestBody(storeInfo);

        byte[] body = requestBody.encode(storeInfo.isCompressed());
        int crc32 = HashUtil.crc32(body);
        requestHeader.setBodyCrc32(crc32);

        RpcCommand request = RpcCommand.createRequestCommand(RequestCode.REGISTER_BROKER, requestHeader);
        request.setBody(body);

        return request;
    }

    private RegisterStoreResult registerStore(StoreInfo storeInfo, String registryAddress, RpcCommand request) {
        RegisterBrokerRequestHeader requestHeader = (RegisterBrokerRequestHeader) request.readCustomHeader();
        return null;
    }

    @Override
    public List<RegisterStoreResult>  registerStore(StoreInfo storeInfo) {
        List<RegisterStoreResult> results = new CopyOnWriteArrayList<>();
        Set<String> registrySet = registryManager.getAvailableRegistry();
        if (CollectionUtil.isEmpty(registrySet)) {
            return results;
        }

        RpcCommand request = createRegisterStoreRequest(storeInfo);
        for (String addr : registrySet) {
            RegisterStoreResult result = registerStore(storeInfo, addr, request);
            results.add(result);
        }

        return results;
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
        try {
            String registryAddress = registryManager.chooseRegistry();
        } catch (Exception e) {
            log.error("sync cluster info error", e);
        }

        return null;
    }

    @Override
    public GroupInfo syncGroupInfo(String clusterName, String groupName) {
        try {
            String registryAddress = registryManager.chooseRegistry();
        } catch (Exception e) {
            log.error("sync group info error", e);
        }

        return null;
    }

    @Override
    public RouteInfo syncRouteInfo(String topicName, long timeout) {
        try {
            String registryAddress = registryManager.chooseRegistry();
        } catch (Exception e) {
            log.error("sync route info error", e);
        }
        return null;
    }

}
