package com.wolf.minimq.store.domain.dispatcher;

import com.wolf.common.lang.concurrent.ServiceThread;
import com.wolf.minimq.domain.model.dto.CommitLogEvent;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.domain.CommitLogConsumer;
import com.wolf.minimq.domain.service.store.domain.CommitLogDispatcher;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

public class DefaultCommitLogDispatcher extends ServiceThread  implements CommitLogDispatcher {
    /**
     * startOffset (confirmOffset in RocketMQ)
     *
     */
    @Getter @Setter
    private volatile long startOffset = -1L;

    private final ArrayList<CommitLogConsumer> consumerList = new ArrayList<>();

    private final CommitLog commitLog;

    public DefaultCommitLogDispatcher(CommitLog commitLog) {
        this.commitLog = commitLog;
    }

    @Override
    public void registerConsumer(CommitLogConsumer consumer) {
        consumerList.addLast(consumer);
    }

    @Override
    public String getServiceName() {
        return DefaultCommitLogDispatcher.class.getSimpleName();
    }

    @Override
    public void dispatch(CommitLogEvent event) {
        if (consumerList.isEmpty()) {
            return;
        }

        for (CommitLogConsumer consumer : consumerList) {
            consumer.consume(event);
        }
    }

    @Override
    public void run() {

    }
}
