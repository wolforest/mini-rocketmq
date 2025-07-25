package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.cluster.store.CommitEvent;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventHandler;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;

public class QueueCommitEventHandler implements CommitEventHandler {
    private final ConsumeQueueGateway consumeQueueGateway;

    public QueueCommitEventHandler(ConsumeQueueGateway consumeQueueGateway) {
        this.consumeQueueGateway = consumeQueueGateway;
    }

    @Override
    public void handle(CommitEvent event) {
        consumeQueueGateway.enqueue(event);
    }
}
