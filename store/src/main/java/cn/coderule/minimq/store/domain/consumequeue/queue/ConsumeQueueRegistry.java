package cn.coderule.minimq.store.domain.consumequeue.queue;

import cn.coderule.minimq.domain.service.store.domain.ConsumeQueue;

public interface ConsumeQueueRegistry {
    void register(ConsumeQueue queue);
}
