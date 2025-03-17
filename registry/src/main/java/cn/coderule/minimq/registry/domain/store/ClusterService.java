package cn.coderule.minimq.registry.domain.store;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.domain.constant.PermName;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.registry.domain.store.model.StoreHealthInfo;
import cn.coderule.minimq.rpc.common.protocol.DataVersion;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.registry.protocol.body.BrokerMemberGroup;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterService {
    private static final long DEFAULT_BROKER_CHANNEL_EXPIRED_TIME = 1000 * 60 * 2;

    private final RegistryConfig config;
    private final Route route;

    public ClusterService(RegistryConfig config, Route route) {
        this.route = route;
        this.config = config;
    }

    public DataVersion getStoreVersion(StoreInfo store) {
        return route.getHealthVersion(store);
    }

    public ClusterInfo getClusterInfo() {
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setBrokerAddrTable(route.getGroupMap());
        clusterInfo.setClusterAddrTable(route.getClusterMap());
        return clusterInfo;
    }

    public BrokerMemberGroup getGroupInfo(String clusterName, String groupName) {
        BrokerMemberGroup memberGroup = new BrokerMemberGroup(clusterName, groupName);

        try {
            route.lockRead();
            GroupInfo groupInfo = route.getGroup(groupName);
            if (groupInfo != null) {
                memberGroup.setBrokerAddrs(groupInfo.getBrokerAddrs());
            }
        } catch (Exception e) {
            log.error("get group info error", e);
        } finally {
            route.unlockRead();
        }

        return memberGroup;
    }

    public void flushStoreUpdateTime(String clusterName, String address) {
        StoreInfo store = new StoreInfo(clusterName, address);
        StoreHealthInfo healthInfo = route.getHealthInfo(store);
        if (healthInfo == null) {
            return;
        }

        healthInfo.setLastUpdateTimestamp(System.currentTimeMillis());
    }

    public int removeGroupWritePermission(String groupName) {
        try {
            route.lockWrite();
            return operateGroupPermission(groupName, RequestCode.WIPE_WRITE_PERM_OF_BROKER);
        } catch (Exception e) {
            log.error("remove group write permission error", e);
        } finally {
            route.unlockWrite();
        }

        return 0;
    }

    public int addGroupWritePermission(String groupName) {
        try {
            route.lockWrite();
            return operateGroupPermission(groupName, RequestCode.ADD_WRITE_PERM_OF_BROKER);
        } catch (Exception e) {
            log.error("add group write permission error", e);
        } finally {
            route.unlockWrite();
        }
        return 0;
    }

    private int operateGroupPermission(final String brokerName, final int requestCode) {
        int topicCnt = 0;

        for (Map.Entry<String, Map<String, Topic>> entry : route.getTopicMap().entrySet()) {
            Map<String, Topic> qdMap = entry.getValue();

            final Topic qd = qdMap.get(brokerName);
            if (qd == null) {
                continue;
            }
            int perm = qd.getPerm();
            switch (requestCode) {
                case RequestCode.WIPE_WRITE_PERM_OF_BROKER:
                    perm &= ~PermName.PERM_WRITE;
                    break;
                case RequestCode.ADD_WRITE_PERM_OF_BROKER:
                    perm = PermName.PERM_READ | PermName.PERM_WRITE;
                    break;
            }
            qd.setPerm(perm);
            topicCnt++;
        }
        return topicCnt;
    }
}
