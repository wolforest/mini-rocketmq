package cn.coderule.minimq.domain.domain.meta.statictopic;

import cn.coderule.minimq.domain.core.constant.MQConstants;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class TopicQueueMappingInfo implements Serializable {
    public static final int LEVEL_0 = 0;

    String topic; // redundant field
    String scope = MQConstants.METADATA_SCOPE_GLOBAL;
    int totalQueues;
    String bname;  //identify the hosted broker name
    long epoch; //important to fence the old dirty data
    boolean dirty; //indicate if the data is dirty
    //register to broker to construct the route
    protected ConcurrentMap<Integer/*logicId*/, Integer/*physicalId*/> currIdMap = new ConcurrentHashMap<>();

    public TopicQueueMappingInfo() {

    }

    public TopicQueueMappingInfo(String topic, int totalQueues, String bname, long epoch) {
        this.topic = topic;
        this.totalQueues = totalQueues;
        this.bname = bname;
        this.epoch = epoch;
        this.dirty = false;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public int getTotalQueues() {
        return totalQueues;
    }


    public String getBname() {
        return bname;
    }

    public String getTopic() {
        return topic;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public void setTotalQueues(int totalQueues) {
        this.totalQueues = totalQueues;
    }

    public ConcurrentMap<Integer, Integer> getCurrIdMap() {
        return currIdMap;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }

    public void setCurrIdMap(ConcurrentMap<Integer, Integer> currIdMap) {
        this.currIdMap = currIdMap;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicQueueMappingInfo)) return false;

        TopicQueueMappingInfo info = (TopicQueueMappingInfo) o;

        if (totalQueues != info.totalQueues) return false;
        if (epoch != info.epoch) return false;
        if (dirty != info.dirty) return false;
        if (topic != null ? !topic.equals(info.topic) : info.topic != null) return false;
        if (scope != null ? !scope.equals(info.scope) : info.scope != null) return false;
        if (bname != null ? !bname.equals(info.bname) : info.bname != null) return false;
        return currIdMap != null ? currIdMap.equals(info.currIdMap) : info.currIdMap == null;
    }

    @Override
    public int hashCode() {
        int result = topic != null ? topic.hashCode() : 0;
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        result = 31 * result + totalQueues;
        result = 31 * result + (bname != null ? bname.hashCode() : 0);
        result = 31 * result + (int) (epoch ^ (epoch >>> 32));
        result = 31 * result + (dirty ? 1 : 0);
        result = 31 * result + (currIdMap != null ? currIdMap.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TopicQueueMappingInfo{" +
                "topic='" + topic + '\'' +
                ", scope='" + scope + '\'' +
                ", totalQueues=" + totalQueues +
                ", bname='" + bname + '\'' +
                ", epoch=" + epoch +
                ", dirty=" + dirty +
                ", currIdMap=" + currIdMap +
                '}';
    }
}
