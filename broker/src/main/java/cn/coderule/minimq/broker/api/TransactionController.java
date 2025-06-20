package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.domain.transaction.Transaction;
import cn.coderule.minimq.domain.domain.dto.request.CommitRequest;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class TransactionController {
    private final Transaction transaction;

    public TransactionController(Transaction transaction) {
        this.transaction = transaction;
    }

    public void subscribe(RequestContext context, String topicName, String groupName) {
        transaction.subscribe(context, topicName, groupName);
    }
    public CompletableFuture<Object> commit(CommitRequest request) {
        return transaction.commit(request);
    }
}
