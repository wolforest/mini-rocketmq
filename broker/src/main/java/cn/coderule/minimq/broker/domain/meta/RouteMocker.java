package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.broker.infra.embed.EmbedTopicStore;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.config.TopicConfig;
import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.GroupInfo;
import cn.coderule.minimq.domain.domain.model.cluster.route.QueueInfo;
import cn.coderule.minimq.domain.domain.model.cluster.route.RouteInfo;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteMocker {
    private final BrokerConfig brokerConfig;
    private final TopicConfig topicConfig;
    private final EmbedTopicStore topicStore;

    public RouteMocker(BrokerConfig brokerConfig, TopicConfig topicConfig, EmbedTopicStore topicStore) {
        this.topicConfig = topicConfig;
        this.brokerConfig = brokerConfig;
        this.topicStore = topicStore;
    }

    public RouteInfo getRoute(String topicName) {
        Topic topic = topicStore.getTopic(topicName);
        if (topic == null) {
            topic = createAndSaveTopic(topicName);
        }

        return toRoute(topic);
    }

    private Topic createAndSaveTopic(String topicName) {
        if (!topicConfig.isEnableAutoCreation()) {
            return null;
        }

        try {
            Topic topic = createTopic(topicName);
            topicStore.saveTopic(topic);
            return topic;
        } catch (Exception e) {
            log.error("create topic={} error", topicName, e);
            return null;
        }
    }

    private Topic createTopic(String topicName) {
        return Topic.builder()
            .topicName(topicName)
            .readQueueNums(topicConfig.getDefaultQueueNum())
            .writeQueueNums(topicConfig.getDefaultQueueNum())
            .build();
    }

    private RouteInfo toRoute(Topic topic) {
        RouteInfo result = new RouteInfo();
        if (topic == null) {
            return result;
        }

        result.setTopicName(topic.getTopicName());
        result.setMessageType(topic.getTopicType());

        QueueInfo queueInfo = QueueInfo.from(brokerConfig.getGroup(), topic);
        result.getQueueDatas().add(queueInfo);

        GroupInfo groupInfo = createGroupInfo();
        result.getBrokerDatas().add(groupInfo);

        return result;
    }

    private GroupInfo createGroupInfo() {
        GroupInfo groupInfo = new GroupInfo(brokerConfig.getCluster(), brokerConfig.getName());

        Map<Long, String> addressMap = new HashMap<>();
        String address = brokerConfig.getHost() + ":" + brokerConfig.getPort();
        addressMap.put(brokerConfig.getGroupNo(), address);

        groupInfo.setBrokerAddrs(addressMap);
        return groupInfo;
    }

}
