package cn.coderule.minimq.domain.domain.meta.topic;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

public class TopicValidator {

    public static final String AUTO_CREATE_TOPIC_KEY_TOPIC = "TBW102"; // Will be created at broker when isAutoCreateTopicEnable
    public static final String RMQ_SYS_SCHEDULE_TOPIC = "SCHEDULE_TOPIC_XXXX";
    public static final String RMQ_SYS_BENCHMARK_TOPIC = "BenchmarkTest";
    public static final String RMQ_SYS_TRACE_TOPIC = "RMQ_SYS_TRACE_TOPIC";

    /**
     * transaction related topics:
     * 1, RMQ_SYS_TRANS_HALF_TOPIC:
     *      store prepare message,
     *      by default, there is only one queue for this topic
     * 2, RMQ_SYS_TRANS_OP_HALF_TOPIC:
     *      store commit/rollback message
     *      by default, there is only one queue for this topic
     * 3, RMQ_SYS_TRANS_CHECK_MAX_TIME_TOPIC:
     *      store discard message, which is checked too many times
     *      by default, there is only one queue for this topic
     */
    public static final String RMQ_SYS_TRANS_HALF_TOPIC = "RMQ_SYS_TRANS_HALF_TOPIC";
    public static final String RMQ_SYS_TRANS_OP_HALF_TOPIC = "RMQ_SYS_TRANS_OP_HALF_TOPIC";
    public static final String RMQ_SYS_TRANS_CHECK_MAX_TIME_TOPIC = "TRANS_CHECK_MAX_TIME_TOPIC";

    public static final String RMQ_SYS_SELF_TEST_TOPIC = "SELF_TEST_TOPIC";
    public static final String RMQ_SYS_OFFSET_MOVED_EVENT = "OFFSET_MOVED_EVENT";
    public static final String RMQ_SYS_ROCKSDB_OFFSET_TOPIC = "CHECKPOINT_TOPIC";

    public static final String SYSTEM_TOPIC_PREFIX = "rmq_sys_";
    public static final String SYNC_BROKER_MEMBER_GROUP_PREFIX = SYSTEM_TOPIC_PREFIX + "SYNC_BROKER_MEMBER_";

    public static final boolean[] VALID_CHAR_BIT_MAP = new boolean[128];
    private static final int TOPIC_MAX_LENGTH = 127;

    private static final Set<String> SYSTEM_TOPIC_SET = new HashSet<>();

    /**
     * Topics'set which client can not send msg!
     */
    private static final Set<String> NOT_ALLOWED_SEND_TOPIC_SET = new HashSet<>();

    static {
        SYSTEM_TOPIC_SET.add(AUTO_CREATE_TOPIC_KEY_TOPIC);
        SYSTEM_TOPIC_SET.add(RMQ_SYS_SCHEDULE_TOPIC);
        SYSTEM_TOPIC_SET.add(RMQ_SYS_BENCHMARK_TOPIC);
        SYSTEM_TOPIC_SET.add(RMQ_SYS_TRANS_HALF_TOPIC);
        SYSTEM_TOPIC_SET.add(RMQ_SYS_TRACE_TOPIC);
        SYSTEM_TOPIC_SET.add(RMQ_SYS_TRANS_OP_HALF_TOPIC);
        SYSTEM_TOPIC_SET.add(RMQ_SYS_TRANS_CHECK_MAX_TIME_TOPIC);
        SYSTEM_TOPIC_SET.add(RMQ_SYS_SELF_TEST_TOPIC);
        SYSTEM_TOPIC_SET.add(RMQ_SYS_OFFSET_MOVED_EVENT);
        SYSTEM_TOPIC_SET.add(RMQ_SYS_ROCKSDB_OFFSET_TOPIC);

        NOT_ALLOWED_SEND_TOPIC_SET.add(RMQ_SYS_SCHEDULE_TOPIC);
        NOT_ALLOWED_SEND_TOPIC_SET.add(RMQ_SYS_TRANS_HALF_TOPIC);
        NOT_ALLOWED_SEND_TOPIC_SET.add(RMQ_SYS_TRANS_OP_HALF_TOPIC);
        NOT_ALLOWED_SEND_TOPIC_SET.add(RMQ_SYS_TRANS_CHECK_MAX_TIME_TOPIC);
        NOT_ALLOWED_SEND_TOPIC_SET.add(RMQ_SYS_SELF_TEST_TOPIC);
        NOT_ALLOWED_SEND_TOPIC_SET.add(RMQ_SYS_OFFSET_MOVED_EVENT);

        // regex: ^[%|a-zA-Z0-9_-]+$
        // %
        VALID_CHAR_BIT_MAP['%'] = true;
        // -
        VALID_CHAR_BIT_MAP['-'] = true;
        // _
        VALID_CHAR_BIT_MAP['_'] = true;
        // |
        VALID_CHAR_BIT_MAP['|'] = true;
        for (int i = 0; i < VALID_CHAR_BIT_MAP.length; i++) {
            if (i >= '0' && i <= '9') {
                // 0-9
                VALID_CHAR_BIT_MAP[i] = true;
            } else if (i >= 'A' && i <= 'Z') {
                // A-Z
                VALID_CHAR_BIT_MAP[i] = true;
            } else if (i >= 'a' && i <= 'z') {
                // a-z
                VALID_CHAR_BIT_MAP[i] = true;
            }
        }
    }

    public static boolean isTopicOrGroupIllegal(String str) {
        int strLen = str.length();
        int len = VALID_CHAR_BIT_MAP.length;
        for (int i = 0; i < strLen; i++) {
            char ch = str.charAt(i);
            if (ch >= len || !VALID_CHAR_BIT_MAP[ch]) {
                return true;
            }
        }
        return false;
    }

    public static void validateTopic(String topicName) {
        if (StringUtil.isBlank(topicName)) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is blank");
        }

        if (topicName.length() > TOPIC_MAX_LENGTH) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is too long");
        }

        if (TopicValidator.isTopicOrGroupIllegal(topicName)) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is illegal");
        }

        if (TopicValidator.isSystemTopic(topicName)) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is system topic");
        }
    }

    public static ValidateTopicResult validateAndReturn(String topic) {

        if (StringUtil.isBlank(topic)) {
            return new ValidateTopicResult(false, "The specified topic is blank.");
        }

        if (isTopicOrGroupIllegal(topic)) {
            return new ValidateTopicResult(false, "The specified topic contains illegal characters, allowing only ^[%|a-zA-Z0-9_-]+$");
        }

        if (topic.length() > TOPIC_MAX_LENGTH) {
            return new ValidateTopicResult(false, "The specified topic is longer than topic max length.");
        }

        return new ValidateTopicResult(true, "");
    }

    public static class ValidateTopicResult {
        private final boolean valid;
        private final String remark;

        public ValidateTopicResult(boolean valid, String remark) {
            this.valid = valid;
            this.remark = remark;
        }

        public boolean isValid() {
            return valid;
        }

        public String getRemark() {
            return remark;
        }
    }

    public static boolean isSystemTopic(String topic) {
        return SYSTEM_TOPIC_SET.contains(topic) || topic.startsWith(SYSTEM_TOPIC_PREFIX);
    }

    public static boolean isNotAllowedSendTopic(String topic) {
        return NOT_ALLOWED_SEND_TOPIC_SET.contains(topic);
    }

    public static void addSystemTopic(String systemTopic) {
        SYSTEM_TOPIC_SET.add(systemTopic);
    }

    public static Set<String> getSystemTopicSet() {
        return SYSTEM_TOPIC_SET;
    }

    public static Set<String> getNotAllowedSendTopicSet() {
        return NOT_ALLOWED_SEND_TOPIC_SET;
    }
}
