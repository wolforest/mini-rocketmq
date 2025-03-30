package cn.coderule.minimq.domain.model.meta;

import cn.coderule.minimq.domain.model.DataVersion;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.utils.attribute.AttributeUtil;
import cn.coderule.minimq.domain.utils.topic.TopicAttributes;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class TopicMap implements Serializable {
    private ConcurrentHashMap<String, Topic> topicTable = new ConcurrentHashMap<>();
    private DataVersion dataVersion = new DataVersion();

    public boolean exists(String topicName) {
        return topicTable.containsKey(topicName);
    }

    public Topic getTopic(String topicName) {
        return topicTable.get(topicName);
    }

    public void putTopic(Topic topic) {
        Topic old = topicTable.put(topic.getTopicName(), topic);
        if (old == null) {
            log.info("New Topic: {}", topic);
        } else  {
            log.info("Topic changed, Old: {}; NEW: {}", old, topic);
        }
    }

    public void saveTopic(@NonNull Topic topic, long stateVersion) {
        Map<String, String> newAttributes = getNewAttributes(topic);
        Map<String, String> oldAttributes = getOldAttributes(topic.getTopicName());

        Map<String, String> attributes = AttributeUtil.alterCurrentAttributes(
            exists(topic.getTopicName()),
            TopicAttributes.ALL,
            ImmutableMap.copyOf(oldAttributes),
            ImmutableMap.copyOf(newAttributes)
        );

        topic.setAttributes(attributes);
        putTopic(topic);

        dataVersion.nextVersion(stateVersion);
    }

    private Map<String, String> getNewAttributes(Topic topic) {
        return null == topic.getAttributes()
            ? new HashMap<>()
            : topic.getAttributes();
    }

    private Map<String, String> getOldAttributes(String topicName) {
        Topic oldTopic = topicTable.get(topicName);
        if (oldTopic == null) {
            return new HashMap<>();
        }

        return null != oldTopic.getAttributes()
            ? oldTopic.getAttributes()
            : new HashMap<>();
    }



    public void deleteTopic(String topicName, long stateVersion) {
        Topic old = topicTable.remove(topicName);
        if (old == null) {
            log.info("fail to delete topic, topicName: {}", topicName);
            return;
        }

        log.info("Topic deleted, topic: {}", old);
        dataVersion.nextVersion(stateVersion);
    }


}
