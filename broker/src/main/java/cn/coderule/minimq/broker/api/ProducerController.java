package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.domain.producer.Producer;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ProducerController
 *  - accept RequestContext and MessageBO
 *  - return EnqueueResult
 *  - for multi-protocol support
 */
public class ProducerController {
    private final Producer producer;

    public ProducerController(Producer producer) {
        this.producer = producer;
    }

    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        return producer.produce(context, messageBO);
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        return producer.produce(context, messageList);
    }
}
