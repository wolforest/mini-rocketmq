package cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint;

import com.alibaba.fastjson2.annotation.JSONField;
import java.util.ArrayList;
import java.util.List;

/**
 * state check info for multi-messages pop from consume queue
 */
public class PopCheckPoint implements Comparable<PopCheckPoint> {
    @JSONField(name = "so")
    private long startOffset;

    /**
     * the time when the message is popped
     * It was set by PopMessageProcessor while popping
     */
    @JSONField(name = "pt")
    private long popTime;

    /**
     * the invisible time of messages
     * default is 60s, it can be changed by MQ client
     */
    @JSONField(name = "it")
    private long invisibleTime;
    /**
     * store ack states of messages
     * one byte for each message
     */
    @JSONField(name = "bm")
    private int bitMap;
    /**
     * total number of messages
     */
    @JSONField(name = "n")
    private byte num;
    @JSONField(name = "q")
    private int queueId;
    @JSONField(name = "t")
    private String topic;
    /**
     * consume group
     *
     * @renamed from cid to consumeGroup
     */
    @JSONField(name = "c")
    private String cid;
    /**
     * the consume queue offset of the revive message
     */
    @JSONField(name = "ro")
    private long reviveOffset;
    /**
     *
     */
    @JSONField(name = "d")
    private List<Integer> queueOffsetDiff;
    @JSONField(name = "bn")
    String brokerName;

    public long getReviveOffset() {
        return reviveOffset;
    }

    public void setReviveOffset(long reviveOffset) {
        this.reviveOffset = reviveOffset;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    public void setInvisibleTime(long invisibleTime) {
        this.invisibleTime = invisibleTime;
    }

    public long getPopTime() {
        return popTime;
    }

    public long getInvisibleTime() {
        return invisibleTime;
    }

    public long getReviveTime() {
        return popTime + invisibleTime;
    }

    public int getBitMap() {
        return bitMap;
    }

    public void setBitMap(int bitMap) {
        this.bitMap = bitMap;
    }

    public byte getNum() {
        return num;
    }

    public void setNum(byte num) {
        this.num = num;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getCId() {
        return cid;
    }

    public void setCId(String cid) {
        this.cid = cid;
    }

    public List<Integer> getQueueOffsetDiff() {
        return queueOffsetDiff;
    }

    public void setQueueOffsetDiff(List<Integer> queueOffsetDiff) {
        this.queueOffsetDiff = queueOffsetDiff;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public void addDiff(int diff) {
        if (this.queueOffsetDiff == null) {
            this.queueOffsetDiff = new ArrayList<>(8);
        }
        this.queueOffsetDiff.add(diff);
    }

    public int indexOfAck(long ackOffset) {
        if (ackOffset < startOffset) {
            return -1;
        }

        // old version of checkpoint
        if (queueOffsetDiff == null || queueOffsetDiff.isEmpty()) {

            if (ackOffset - startOffset < num) {
                return (int) (ackOffset - startOffset);
            }

            return -1;
        }

        // new version of checkpoint
        return queueOffsetDiff.indexOf((int) (ackOffset - startOffset));
    }

    public long ackOffsetByIndex(byte index) {
        // old version of checkpoint
        if (queueOffsetDiff == null || queueOffsetDiff.isEmpty()) {
            return startOffset + index;
        }

        return startOffset + queueOffsetDiff.get(index);
    }

    @Override
    public String toString() {
        return "PopCheckPoint [topic=" + topic + ", cid=" + cid + ", queueId=" + queueId + ", startOffset=" + startOffset + ", bitMap=" + bitMap + ", num=" + num + ", reviveTime=" + getReviveTime()
            + ", reviveOffset=" + reviveOffset + ", diff=" + queueOffsetDiff + ", brokerName=" + brokerName + "]";
    }

    @Override
    public int compareTo(PopCheckPoint o) {
        return (int) (this.getStartOffset() - o.getStartOffset());
    }
}
