package com.wolf.minimq.store.domain.commitlog.flush;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.minimq.domain.config.CommitLogConfig;
import com.wolf.minimq.domain.enums.FlushType;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.dto.FlushResult;
import com.wolf.minimq.domain.model.dto.InsertResult;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.store.domain.commitlog.vo.GroupCommitRequest;
import com.wolf.minimq.store.server.StoreCheckpoint;
import java.util.concurrent.CompletableFuture;

/**
 * depend on:
 *  - CommitLogConfig
 *  - MappedFileQueue
 *  - StoreCheckPoint
 */
public class FlushManager implements Lifecycle {
    private State state = State.INITIALIZING;

    private final CommitLogConfig commitLogConfig;
    private final MappedFileQueue mappedFileQueue;
    private final StoreCheckpoint storeCheckPoint;

    private final FlushService commitService;
    private final FlushService flushService;
    private final FlushWatcher flushWatcher;

    public FlushManager(
        CommitLogConfig commitLogConfig,
        MappedFileQueue mappedFileQueue,
        StoreCheckpoint storeCheckpoint) {
        this.commitLogConfig = commitLogConfig;
        this.mappedFileQueue = mappedFileQueue;
        this.storeCheckPoint = storeCheckpoint;

        this.flushWatcher = new FlushWatcher();
        this.commitService = new GroupCommitService();

        if (FlushType.SYNC.equals(commitLogConfig.getFlushType())) {
            this.flushService = new RealTimeFlushService();
        } else {
            this.flushService = new GroupFlushService();
        }
    }

    public FlushResult flush(InsertResult insertResult, MessageBO messageBO) {
        if (FlushType.SYNC.equals(commitLogConfig.getFlushType())) {
            return syncFlush(insertResult, messageBO);
        }

        return asyncFlush(insertResult);
    }

    private FlushResult syncFlush(InsertResult insertResult, MessageBO messageBO) {
        if (!messageBO.isWaitStore()) {
            flushService.wakeup();
            return FlushResult.success(insertResult);
        }

        GroupCommitRequest request = createGroupCommitRequest(insertResult);
        GroupCommitService service = (GroupCommitService) flushService;

        service.addRequest(request);
        flushWatcher.addRequest(request);

        return formatResult(insertResult, request);
    }

    private FlushResult formatResult(InsertResult insertResult, GroupCommitRequest request) {
        CompletableFuture<EnqueueResult> result = request.future()
            .thenApplyAsync(
                flushStatus -> EnqueueResult.builder()
                    .status(flushStatus)
                    .insertResult(insertResult)
                    .build()
            );

        return FlushResult.builder()
            .insertResult(insertResult)
            .flushFuture(result)
            .build();
    }

    private GroupCommitRequest createGroupCommitRequest(InsertResult insertResult) {
        long nextOffset = insertResult.getWroteOffset() + insertResult.getWroteBytes();
        long deadLine = System.nanoTime() + commitLogConfig.getFlushTimeout();

        return GroupCommitRequest.builder()
            .nextOffset(nextOffset)
            .deadLine(deadLine)
            .build();
    }

    private FlushResult asyncFlush(InsertResult insertResult) {
        if (commitLogConfig.isEnableWriteCache()) {
            commitService.wakeup();
        } else {
            flushService.wakeup();
        }
        return FlushResult.success(insertResult);
    }


    @Override
    public void initialize() {
    }

    @Override
    public void start() {
        this.state = State.STARTING;

        this.flushService.start();

        this.flushWatcher.setDaemon(true);
        this.flushWatcher.start();

        if (commitLogConfig.isEnableWriteCache()) {
            this.commitService.start();
        }

        this.state = State.RUNNING;
    }

    @Override
    public void shutdown() {
        this.state = State.SHUTTING_DOWN;

        this.flushService.shutdown();
        this.flushWatcher.shutdown();

        if (commitLogConfig.isEnableWriteCache()) {
            this.commitService.shutdown();
        }

        this.state = State.TERMINATED;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return state;
    }
}
