package cn.coderule.minimq.rpc.store.facade;

import cn.coderule.minimq.domain.domain.consumer.ack.store.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import java.util.concurrent.CompletableFuture;

public interface MQFacade {
    EnqueueResult enqueue(EnqueueRequest request);
    CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request);

    DequeueResult dequeue(DequeueRequest request);
    CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request);

    DequeueResult get(DequeueRequest request);
    MessageResult getMessage(MessageRequest request);

    void addCheckPoint(CheckPointRequest request);
    void ack(AckRequest request);
    long getBufferedOffset(OffsetRequest request);

    QueueResult getMinOffset(QueueRequest request);
    QueueResult getMaxOffset(QueueRequest request);
}
