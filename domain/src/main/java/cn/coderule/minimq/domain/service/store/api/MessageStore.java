package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.GetResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Message pub/sub APIs
 */
public interface MessageStore {
    EnqueueResult enqueue(MessageBO messageBO);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO);

    GetResult get(String topic, int queueId, long offset);
    GetResult get(String topic, int queueId, long offset, int num);
    GetResult get(GetRequest request);

    MessageBO getMessage(String topic, int queueId, long offset);
    List<MessageBO> getMessage(String topic, int queueId, long offset, int num);
}
