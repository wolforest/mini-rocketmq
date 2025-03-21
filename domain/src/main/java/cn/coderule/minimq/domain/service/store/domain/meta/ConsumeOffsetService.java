package cn.coderule.minimq.domain.service.store.domain.meta;

public interface ConsumeOffsetService extends MetaService {
    Long getOffset(String group, String topic, int queueId);
    Long getAndRemove(String group, String topic, int queueId);
    void putOffset(String group, String topic, int queueId, long offset);
}
