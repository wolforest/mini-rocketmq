package cn.coderule.minimq.domain.domain.meta.topic;

import cn.coderule.minimq.domain.core.enums.message.MessageType;
import cn.coderule.minimq.domain.core.attribute.Attribute;
import cn.coderule.minimq.domain.core.attribute.EnumAttribute;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;

public class TopicAttributes {
    public static final EnumAttribute QUEUE_TYPE_ATTRIBUTE = new EnumAttribute(
        "queue.type",
        false,
        newHashSet("BatchCQ", "SimpleCQ"),
        "SimpleCQ"
    );
    public static final EnumAttribute CLEANUP_POLICY_ATTRIBUTE = new EnumAttribute(
        "cleanup.policy",
        false,
        newHashSet("DELETE", "COMPACTION"),
        "DELETE"
    );
    public static final EnumAttribute TOPIC_MESSAGE_TYPE_ATTRIBUTE = new EnumAttribute(
        "message.type",
        true,
        MessageType.typeSet(),
        MessageType.NORMAL.getValue()
    );

    public static final Map<String, Attribute> ALL;

    static {
        ALL = new HashMap<>();
        ALL.put(QUEUE_TYPE_ATTRIBUTE.getName(), QUEUE_TYPE_ATTRIBUTE);
        ALL.put(CLEANUP_POLICY_ATTRIBUTE.getName(), CLEANUP_POLICY_ATTRIBUTE);
        ALL.put(TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName(), TOPIC_MESSAGE_TYPE_ATTRIBUTE);
    }
}
