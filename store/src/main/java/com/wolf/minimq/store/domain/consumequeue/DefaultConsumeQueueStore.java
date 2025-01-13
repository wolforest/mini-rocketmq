package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueueStore;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import java.util.List;

public class DefaultConsumeQueueStore implements ConsumeQueueStore {
    private final ConsumeQueueFactory consumeQueueFactory;

    public DefaultConsumeQueueStore(ConsumeQueueFactory consumeQueueFactory) {
        this.consumeQueueFactory = consumeQueueFactory;
    }

    @Override
    public void enqueue(CommitLogEvent event) {
        String topic = event.getMessageBO().getTopic();
        int queueId = event.getMessageBO().getQueueId();

        getQueueStore(topic, queueId).enqueue(event);
    }

    @Override
    public QueueUnit fetch(String topic, int queueId, long offset) {
        return getQueueStore(topic, queueId).fetch(offset);
    }

    @Override
    public List<QueueUnit> fetch(String topic, int queueId, long offset, int num) {
        return getQueueStore(topic, queueId).fetch(offset, num);
    }

    @Override
    public void assignOffset(MessageBO messageBO) {

    }

    @Override
    public void increaseOffset(MessageBO messageBO) {

    }

    @Override
    public long getMinOffset(String topic, int queueId) {
        return getQueueStore(topic, queueId).getMinOffset();
    }

    @Override
    public long getMaxOffset(String topic, int queueId) {
        return getQueueStore(topic, queueId).getMaxOffset();
    }

    private ConsumeQueue getQueueStore(String topic, int queueId) {
        return consumeQueueFactory.getOrCreate(topic, queueId);
    }
}
