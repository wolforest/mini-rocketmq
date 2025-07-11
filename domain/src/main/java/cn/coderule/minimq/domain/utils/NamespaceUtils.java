package cn.coderule.minimq.domain.utils;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.domain.meta.topic.TopicValidator;

public class NamespaceUtils {
    public static final char NAMESPACE_SEPARATOR = '%';
    public static final String STRING_BLANK = "";
    public static final int RETRY_PREFIX_LENGTH = MQConstants.RETRY_GROUP_TOPIC_PREFIX.length();
    public static final int DLQ_PREFIX_LENGTH = MQConstants.DLQ_GROUP_TOPIC_PREFIX.length();

    /**
     * Unpack namespace from resource, just like:
     * (1) MQ_INST_XX%Topic_XXX --> Topic_XXX
     * (2) %RETRY%MQ_INST_XX%GID_XXX --> %RETRY%GID_XXX
     *
     * @param resourceWithNamespace, topic/groupId with namespace.
     * @return topic/groupId without namespace.
     */
    public static String withoutNamespace(String resourceWithNamespace) {
        if (StringUtil.isEmpty(resourceWithNamespace) || isSystemResource(resourceWithNamespace)) {
            return resourceWithNamespace;
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (isRetryTopic(resourceWithNamespace)) {
            stringBuilder.append(MQConstants.RETRY_GROUP_TOPIC_PREFIX);
        }
        if (isDLQTopic(resourceWithNamespace)) {
            stringBuilder.append(MQConstants.DLQ_GROUP_TOPIC_PREFIX);
        }

        String resourceWithoutRetryAndDLQ = withOutRetryAndDLQ(resourceWithNamespace);
        int index = resourceWithoutRetryAndDLQ.indexOf(NAMESPACE_SEPARATOR);
        if (index > 0) {
            String resourceWithoutNamespace = resourceWithoutRetryAndDLQ.substring(index + 1);
            return stringBuilder.append(resourceWithoutNamespace).toString();
        }

        return resourceWithNamespace;
    }

    /**
     * If resource contains the namespace, unpack namespace from resource, just like:
     * (1) (MQ_INST_XX1%Topic_XXX1, MQ_INST_XX1) --> Topic_XXX1
     * (2) (MQ_INST_XX2%Topic_XXX2, NULL) --> MQ_INST_XX2%Topic_XXX2
     * (3) (%RETRY%MQ_INST_XX1%GID_XXX1, MQ_INST_XX1) --> %RETRY%GID_XXX1
     * (4) (%RETRY%MQ_INST_XX2%GID_XXX2, MQ_INST_XX3) --> %RETRY%MQ_INST_XX2%GID_XXX2
     *
     * @param resourceWithNamespace, topic/groupId with namespace.
     * @param namespace, namespace to be unpacked.
     * @return topic/groupId without namespace.
     */
    public static String withoutNamespace(String resourceWithNamespace, String namespace) {
        if (StringUtil.isEmpty(resourceWithNamespace) || StringUtil.isEmpty(namespace)) {
            return resourceWithNamespace;
        }

        String resourceWithoutRetryAndDLQ = withOutRetryAndDLQ(resourceWithNamespace);
        if (resourceWithoutRetryAndDLQ.startsWith(namespace + NAMESPACE_SEPARATOR)) {
            return withoutNamespace(resourceWithNamespace);
        }

        return resourceWithNamespace;
    }

    public static String wrapNamespace(String namespace, String resourceWithOutNamespace) {
        if (StringUtil.isEmpty(namespace) || StringUtil.isEmpty(resourceWithOutNamespace)) {
            return resourceWithOutNamespace;
        }

        if (isSystemResource(resourceWithOutNamespace) || isAlreadyWithNamespace(resourceWithOutNamespace, namespace)) {
            return resourceWithOutNamespace;
        }

        String resourceWithoutRetryAndDLQ = withOutRetryAndDLQ(resourceWithOutNamespace);
        StringBuilder stringBuilder = new StringBuilder();

        if (isRetryTopic(resourceWithOutNamespace)) {
            stringBuilder.append(MQConstants.RETRY_GROUP_TOPIC_PREFIX);
        }

        if (isDLQTopic(resourceWithOutNamespace)) {
            stringBuilder.append(MQConstants.DLQ_GROUP_TOPIC_PREFIX);
        }

        return stringBuilder.append(namespace).append(NAMESPACE_SEPARATOR).append(resourceWithoutRetryAndDLQ).toString();
    }

    public static boolean isAlreadyWithNamespace(String resource, String namespace) {
        if (StringUtil.isEmpty(namespace) || StringUtil.isEmpty(resource) || isSystemResource(resource)) {
            return false;
        }

        String resourceWithoutRetryAndDLQ = withOutRetryAndDLQ(resource);
        return resourceWithoutRetryAndDLQ.startsWith(namespace + NAMESPACE_SEPARATOR);
    }

    public static String wrapNamespaceAndRetry(String namespace, String consumerGroup) {
        if (StringUtil.isEmpty(consumerGroup)) {
            return null;
        }

        return MQConstants.RETRY_GROUP_TOPIC_PREFIX + wrapNamespace(namespace, consumerGroup);
    }

    public static String getNamespaceFromResource(String resource) {
        if (StringUtil.isEmpty(resource) || isSystemResource(resource)) {
            return STRING_BLANK;
        }
        String resourceWithoutRetryAndDLQ = withOutRetryAndDLQ(resource);
        int index = resourceWithoutRetryAndDLQ.indexOf(NAMESPACE_SEPARATOR);

        return index > 0 ? resourceWithoutRetryAndDLQ.substring(0, index) : STRING_BLANK;
    }

    public static String withOutRetryAndDLQ(String originalResource) {
        if (StringUtil.isEmpty(originalResource)) {
            return STRING_BLANK;
        }
        if (isRetryTopic(originalResource)) {
            return originalResource.substring(RETRY_PREFIX_LENGTH);
        }

        if (isDLQTopic(originalResource)) {
            return originalResource.substring(DLQ_PREFIX_LENGTH);
        }

        return originalResource;
    }

    private static boolean isSystemResource(String resource) {
        if (StringUtil.isEmpty(resource)) {
            return false;
        }

        return TopicValidator.isSystemTopic(resource)
            || MQConstants.isSysConsumerGroup(resource);
    }

    public static boolean isRetryTopic(String resource) {
        return StringUtil.notBlank(resource)
            && resource.startsWith(MQConstants.RETRY_GROUP_TOPIC_PREFIX);
    }

    public static boolean isDLQTopic(String resource) {
        return StringUtil.notBlank(resource)
            && resource.startsWith(MQConstants.DLQ_GROUP_TOPIC_PREFIX);
    }
}
