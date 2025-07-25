

/**
 * $Id: TopicRouteData.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.domain.domain.cluster.route;

import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.core.enums.message.MessageType;
import cn.coderule.minimq.domain.domain.cluster.cluster.GroupInfo;
import cn.coderule.minimq.domain.domain.meta.statictopic.TopicQueueMappingInfo;
import com.alibaba.fastjson2.JSONWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Getter;
import lombok.Setter;

/**
 * Topic route data register to name server
 * with the header: RegisterTopicRequestHeader{topic, ...}
 */
@Setter @Getter
public class RouteInfo implements Serializable {
    // not compatible with rocketmq
    private String topicName;
    // not compatible with rocketmq
    private MessageType messageType;
    // not compatible with rocketmq
    private boolean consumeOrderly = false;

    private String orderTopicConf;
    /**
     * Queue data list
     *  - brokerName
     *  - perm
     *  - topicSysFlag
     *  - readQueueNums
     *  - writeQueueNums
     */
    private List<QueueInfo> queueDatas;

    /**
     * Broker data list
     *  - cluster
     *  - brokerName
     *  - brokerAddrs
     *      - brokerId
     *      - brokerAddr
     *  - zoneName
     *  - random
     *  - enableActingMaster
     */
    private List<GroupInfo> brokerDatas;

    /**
     * Filter server table
     *  - brokerAddr
     *  - Filter Server
     */
    private HashMap<String/* brokerAddr */, List<String>/* Filter Server */> filterServerTable;
    //It could be null or empty
    /**
     * Topic queue mapping info
     *  - brokerName
     *  - TopicQueueMappingInfo
     *      - topic
     *      - scope
     *      - totalQueues
     *      - brokerName
     *      - epoch
     *      - dirty(isDirty)
     *      - currIdMap
     *          - logicalId
     *          - physicalId
     */
    private Map<String/*brokerName*/, TopicQueueMappingInfo> topicQueueMappingByBroker;

    public RouteInfo() {
        queueDatas = new ArrayList<>();
        brokerDatas = new ArrayList<>();
        filterServerTable = new HashMap<>();
    }

    public RouteInfo(RouteInfo routeInfo) {
        this.queueDatas = new ArrayList<>();
        this.brokerDatas = new ArrayList<>();
        this.filterServerTable = new HashMap<>();
        this.orderTopicConf = routeInfo.orderTopicConf;

        if (routeInfo.queueDatas != null) {
            this.queueDatas.addAll(routeInfo.queueDatas);
        }

        if (routeInfo.brokerDatas != null) {
            this.brokerDatas.addAll(routeInfo.brokerDatas);
        }

        if (routeInfo.filterServerTable != null) {
            this.filterServerTable.putAll(routeInfo.filterServerTable);
        }

        if (routeInfo.topicQueueMappingByBroker != null) {
            this.topicQueueMappingByBroker = new HashMap<>(routeInfo.topicQueueMappingByBroker);
        }

        this.topicName = routeInfo.topicName;
        this.messageType = routeInfo.messageType;
    }

    public boolean isQueueMappingEmpty() {
        return topicQueueMappingByBroker == null || topicQueueMappingByBroker.isEmpty();
    }

    public boolean isValid() {
        return null != queueDatas
            && !queueDatas.isEmpty()
            && null != brokerDatas
            && !brokerDatas.isEmpty();
    }

    public byte[] encode() {
        return encode(false);
    }

    public byte[] encode(boolean withFeatures) {
        if (!withFeatures) {
            return JSONUtil.encode(this);
        }

        return JSONUtil.encode(
            this,
            JSONWriter.Feature.BrowserCompatible,
            JSONWriter.Feature.SortMapEntriesByKeys
        );
    }

    public static RouteInfo decode(final byte[] data) {
        return JSONUtil.decode(data, RouteInfo.class);
    }

    public RouteInfo cloneRouteInfo() {
        RouteInfo routeInfo = new RouteInfo();
        routeInfo.setQueueDatas(new ArrayList<>());
        routeInfo.setBrokerDatas(new ArrayList<>());
        routeInfo.setFilterServerTable(new HashMap<>());
        routeInfo.setOrderTopicConf(this.orderTopicConf);

        routeInfo.getQueueDatas().addAll(this.queueDatas);
        routeInfo.getBrokerDatas().addAll(this.brokerDatas);
        routeInfo.getFilterServerTable().putAll(this.filterServerTable);
        if (this.topicQueueMappingByBroker != null) {
            Map<String, TopicQueueMappingInfo> cloneMap = new HashMap<>(this.topicQueueMappingByBroker);
            routeInfo.setTopicQueueMappingByBroker(cloneMap);
        }
        return routeInfo;
    }

    public RouteInfo deepClone() {
        RouteInfo routeInfo = new RouteInfo();

        routeInfo.setOrderTopicConf(this.orderTopicConf);

        for (final QueueInfo queueInfo : this.queueDatas) {
            routeInfo.getQueueDatas().add(new QueueInfo(queueInfo));
        }

        for (final GroupInfo brokerData : this.brokerDatas) {
            routeInfo.getBrokerDatas().add(new GroupInfo(brokerData));
        }

        for (final Map.Entry<String, List<String>> listEntry : this.filterServerTable.entrySet()) {
            routeInfo.getFilterServerTable().put(listEntry.getKey(),
                new ArrayList<>(listEntry.getValue()));
        }
        if (this.topicQueueMappingByBroker != null) {
            Map<String, TopicQueueMappingInfo> cloneMap = new HashMap<>(this.topicQueueMappingByBroker.size());
            for (final Map.Entry<String, TopicQueueMappingInfo> entry : this.getTopicQueueMappingByBroker().entrySet()) {
                TopicQueueMappingInfo topicQueueMappingInfo = new TopicQueueMappingInfo(entry.getValue().getTopic(), entry.getValue().getTotalQueues(), entry.getValue().getBname(), entry.getValue().getEpoch());
                topicQueueMappingInfo.setDirty(entry.getValue().isDirty());
                topicQueueMappingInfo.setScope(entry.getValue().getScope());
                ConcurrentMap<Integer, Integer> concurrentMap = new ConcurrentHashMap<>(entry.getValue().getCurrIdMap());
                topicQueueMappingInfo.setCurrIdMap(concurrentMap);
                cloneMap.put(entry.getKey(), topicQueueMappingInfo);
            }
            routeInfo.setTopicQueueMappingByBroker(cloneMap);
        }

        return routeInfo;
    }

    public boolean isChanged(RouteInfo oldData) {
        if (oldData == null)
            return true;
        RouteInfo old = new RouteInfo(oldData);
        RouteInfo now = new RouteInfo(this);
        Collections.sort(old.getQueueDatas());
        Collections.sort(old.getBrokerDatas());
        Collections.sort(now.getQueueDatas());
        Collections.sort(now.getBrokerDatas());
        return !old.equals(now);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((brokerDatas == null) ? 0 : brokerDatas.hashCode());
        result = prime * result + ((orderTopicConf == null) ? 0 : orderTopicConf.hashCode());
        result = prime * result + ((queueDatas == null) ? 0 : queueDatas.hashCode());
        result = prime * result + ((filterServerTable == null) ? 0 : filterServerTable.hashCode());
        result = prime * result + ((topicQueueMappingByBroker == null) ? 0 : topicQueueMappingByBroker.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RouteInfo other = (RouteInfo) obj;
        if (brokerDatas == null) {
            if (other.brokerDatas != null)
                return false;
        } else if (!brokerDatas.equals(other.brokerDatas))
            return false;
        if (orderTopicConf == null) {
            if (other.orderTopicConf != null)
                return false;
        } else if (!orderTopicConf.equals(other.orderTopicConf))
            return false;
        if (queueDatas == null) {
            if (other.queueDatas != null)
                return false;
        } else if (!queueDatas.equals(other.queueDatas))
            return false;
        if (filterServerTable == null) {
            if (other.filterServerTable != null)
                return false;
        } else if (!filterServerTable.equals(other.filterServerTable))
            return false;
        if (topicQueueMappingByBroker == null) {
            return other.topicQueueMappingByBroker == null;
        } else
            return topicQueueMappingByBroker.equals(other.topicQueueMappingByBroker);
    }

    @Override
    public String toString() {
        return "TopicRouteData [orderTopicConf=" + orderTopicConf + ", queueDatas=" + queueDatas
            + ", brokerDatas=" + brokerDatas + ", filterServerTable=" + filterServerTable + ", topicQueueMappingInfoTable=" + topicQueueMappingByBroker + "]";
    }
}
