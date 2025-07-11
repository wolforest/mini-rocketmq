
package cn.coderule.minimq.domain.domain.consumer.receipt;

import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import java.util.Arrays;
import java.util.List;


public class ReceiptHandle {
    private static final String SEPARATOR = MessageConst.KEY_SEPARATOR;
    public static final String NORMAL_TOPIC = "0";
    public static final String RETRY_TOPIC = "1";

    public static final String RETRY_TOPIC_V2 = "2";
    private final long startOffset;
    private final long retrieveTime;
    private final long invisibleTime;
    private final long nextVisibleTime;
    private final int reviveQueueId;
    private final String topicType;
    private final String brokerName;
    private final int queueId;
    private final long offset;
    private final long commitLogOffset;
    private final String receiptHandle;

    public String encode() {
        return startOffset + SEPARATOR + retrieveTime + SEPARATOR + invisibleTime + SEPARATOR + reviveQueueId
            + SEPARATOR + topicType + SEPARATOR + brokerName + SEPARATOR + queueId + SEPARATOR + offset + SEPARATOR
            + commitLogOffset;
    }

    public boolean isExpired() {
        return nextVisibleTime <= System.currentTimeMillis();
    }

    public static ReceiptHandle decode(String receiptHandle) {
        List<String> dataList = Arrays.asList(receiptHandle.split(SEPARATOR));
        if (dataList.size() < 8) {
            throw new IllegalArgumentException("Parse failed, dataList size " + dataList.size());
        }
        long startOffset = Long.parseLong(dataList.get(0));
        long retrieveTime = Long.parseLong(dataList.get(1));
        long invisibleTime = Long.parseLong(dataList.get(2));
        int reviveQueueId = Integer.parseInt(dataList.get(3));
        String topicType = dataList.get(4);
        String brokerName = dataList.get(5);
        int queueId = Integer.parseInt(dataList.get(6));
        long offset = Long.parseLong(dataList.get(7));
        long commitLogOffset = -1L;
        if (dataList.size() >= 9) {
            commitLogOffset = Long.parseLong(dataList.get(8));
        }

        return new ReceiptHandleBuilder()
            .startOffset(startOffset)
            .retrieveTime(retrieveTime)
            .invisibleTime(invisibleTime)
            .reviveQueueId(reviveQueueId)
            .topicType(topicType)
            .brokerName(brokerName)
            .queueId(queueId)
            .offset(offset)
            .commitLogOffset(commitLogOffset)
            .receiptHandle(receiptHandle).build();
    }

    ReceiptHandle(final long startOffset, final long retrieveTime, final long invisibleTime, final long nextVisibleTime,
        final int reviveQueueId, final String topicType, final String brokerName, final int queueId, final long offset,
        final long commitLogOffset, final String receiptHandle) {
        this.startOffset = startOffset;
        this.retrieveTime = retrieveTime;
        this.invisibleTime = invisibleTime;
        this.nextVisibleTime = nextVisibleTime;
        this.reviveQueueId = reviveQueueId;
        this.topicType = topicType;
        this.brokerName = brokerName;
        this.queueId = queueId;
        this.offset = offset;
        this.commitLogOffset = commitLogOffset;
        this.receiptHandle = receiptHandle;
    }

    public static class ReceiptHandleBuilder {
        private long startOffset;
        private long retrieveTime;
        private long invisibleTime;
        private int reviveQueueId;
        private String topicType;
        private String brokerName;
        private int queueId;
        private long offset;
        private long commitLogOffset;
        private String receiptHandle;

        ReceiptHandleBuilder() {
        }

        public ReceiptHandleBuilder startOffset(final long startOffset) {
            this.startOffset = startOffset;
            return this;
        }

        public ReceiptHandleBuilder retrieveTime(final long retrieveTime) {
            this.retrieveTime = retrieveTime;
            return this;
        }

        public ReceiptHandleBuilder invisibleTime(final long invisibleTime) {
            this.invisibleTime = invisibleTime;
            return this;
        }

        public ReceiptHandleBuilder reviveQueueId(final int reviveQueueId) {
            this.reviveQueueId = reviveQueueId;
            return this;
        }

        public ReceiptHandleBuilder topicType(final String topicType) {
            this.topicType = topicType;
            return this;
        }

        public ReceiptHandleBuilder brokerName(final String brokerName) {
            this.brokerName = brokerName;
            return this;
        }

        public ReceiptHandleBuilder queueId(final int queueId) {
            this.queueId = queueId;
            return this;
        }

        public ReceiptHandleBuilder offset(final long offset) {
            this.offset = offset;
            return this;
        }

        public ReceiptHandleBuilder commitLogOffset(final long commitLogOffset) {
            this.commitLogOffset = commitLogOffset;
            return this;
        }

        public ReceiptHandleBuilder receiptHandle(final String receiptHandle) {
            this.receiptHandle = receiptHandle;
            return this;
        }

        public ReceiptHandle build() {
            return new ReceiptHandle(this.startOffset, this.retrieveTime, this.invisibleTime, this.retrieveTime + this.invisibleTime,
                this.reviveQueueId, this.topicType, this.brokerName, this.queueId, this.offset, this.commitLogOffset, this.receiptHandle);
        }

        @Override
        public String toString() {
            return "ReceiptHandle.ReceiptHandleBuilder(startOffset=" + this.startOffset + ", retrieveTime=" + this.retrieveTime + ", invisibleTime=" + this.invisibleTime + ", reviveQueueId=" + this.reviveQueueId + ", topic=" + this.topicType + ", brokerName=" + this.brokerName + ", queueId=" + this.queueId + ", offset=" + this.offset + ", commitLogOffset=" + this.commitLogOffset + ", receiptHandle=" + this.receiptHandle + ")";
        }
    }

    public static ReceiptHandleBuilder builder() {
        return new ReceiptHandleBuilder();
    }

    public long getStartOffset() {
        return this.startOffset;
    }

    public long getRetrieveTime() {
        return this.retrieveTime;
    }

    public long getInvisibleTime() {
        return this.invisibleTime;
    }

    public long getNextVisibleTime() {
        return this.nextVisibleTime;
    }

    public int getReviveQueueId() {
        return this.reviveQueueId;
    }

    public String getTopicType() {
        return this.topicType;
    }

    public String getBrokerName() {
        return this.brokerName;
    }

    public int getQueueId() {
        return this.queueId;
    }

    public long getOffset() {
        return this.offset;
    }

    public long getCommitLogOffset() {
        return commitLogOffset;
    }

    public String getReceiptHandle() {
        return this.receiptHandle;
    }

    public boolean isRetryTopic() {
        return RETRY_TOPIC.equals(topicType) || RETRY_TOPIC_V2.equals(topicType);
    }

    public String getRealTopic(String topic, String groupName) {
        if (RETRY_TOPIC.equals(topicType)) {
            return KeyBuilder.buildPopRetryTopicV1(topic, groupName);
        }
        if (RETRY_TOPIC_V2.equals(topicType)) {
            return KeyBuilder.buildPopRetryTopicV2(topic, groupName);
        }
        return topic;
    }
}
