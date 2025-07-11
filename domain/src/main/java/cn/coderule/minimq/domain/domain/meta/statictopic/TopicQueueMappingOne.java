package cn.coderule.minimq.domain.domain.meta.statictopic;

import java.io.Serializable;
import java.util.List;

public class TopicQueueMappingOne implements Serializable {

    String topic; // redundant field
    String bname;  //identify the hosted broker name
    Integer globalId;
    List<LogicQueueMappingItem> items;
    TopicQueueMappingDetail mappingDetail;

    public TopicQueueMappingOne(TopicQueueMappingDetail mappingDetail, String topic, String bname, Integer globalId, List<LogicQueueMappingItem> items) {
        this.mappingDetail =  mappingDetail;
        this.topic = topic;
        this.bname = bname;
        this.globalId = globalId;
        this.items = items;
    }

    public String getTopic() {
        return topic;
    }

    public String getBname() {
        return bname;
    }

    public Integer getGlobalId() {
        return globalId;
    }

    public List<LogicQueueMappingItem> getItems() {
        return items;
    }

    public TopicQueueMappingDetail getMappingDetail() {
        return mappingDetail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TopicQueueMappingOne))
            return false;

        TopicQueueMappingOne that = (TopicQueueMappingOne) o;

        if (topic != null ? !topic.equals(that.topic) : that.topic != null)
            return false;
        if (bname != null ? !bname.equals(that.bname) : that.bname != null)
            return false;
        if (globalId != null ? !globalId.equals(that.globalId) : that.globalId != null)
            return false;
        if (items != null ? !items.equals(that.items) : that.items != null)
            return false;
        return mappingDetail != null ? mappingDetail.equals(that.mappingDetail) : that.mappingDetail == null;
    }

    @Override
    public int hashCode() {
        int result = topic != null ? topic.hashCode() : 0;
        result = 31 * result + (bname != null ? bname.hashCode() : 0);
        result = 31 * result + (globalId != null ? globalId.hashCode() : 0);
        result = 31 * result + (items != null ? items.hashCode() : 0);
        result = 31 * result + (mappingDetail != null ? mappingDetail.hashCode() : 0);
        return result;
    }
}
