package com.wolf.minimq.store.domain.commitlog;

import com.wolf.common.util.encrypt.HashUtil;
import com.wolf.minimq.domain.config.CommitLogConfig;
import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.enums.MessageVersion;
import com.wolf.minimq.domain.utils.lock.CommitLogLock;
import com.wolf.minimq.domain.utils.lock.CommitLogReentrantLock;
import com.wolf.minimq.domain.model.Message;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.model.dto.SelectedMappedBuffer;
import com.wolf.minimq.store.domain.commitlog.flush.FlushManager;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * depend on:
 *  - CommitLogConfig
 *  - MappedFileQueue
 *  - FlushManager
 */
public class DefaultCommitLog implements CommitLog {
    private final CommitLogConfig commitLogConfig;
    private final MessageConfig messageConfig;
    private final MappedFileQueue mappedFileQueue;
    private final FlushManager flushManager;

    private final CommitLogLock lock;
    private ThreadLocal<EnqueueThreadLocal> localEncoder;

    public DefaultCommitLog(
        CommitLogConfig commitLogConfig,
        MessageConfig messageConfig,
        MappedFileQueue mappedFileQueue,
        FlushManager flushManager
    ) {
        this.commitLogConfig = commitLogConfig;
        this.messageConfig = messageConfig;

        this.mappedFileQueue = mappedFileQueue;
        this.flushManager = flushManager;

        this.lock = new CommitLogReentrantLock();
        initLocalEncoder();
    }

    @Override
    public CompletableFuture<EnqueueResult> insert(MessageBO messageBO) {
        initAppendMessage(messageBO);

        return null;
    }

    private void initAppendMessage(MessageBO messageBO) {
        messageBO.setStoreTimestamp(System.currentTimeMillis());
        messageBO.setBodyCRC(HashUtil.crc32(messageBO.getBody()));

        messageBO.setVersion(MessageVersion.V1);
        if (messageBO.getTopic().length() > Byte.MAX_VALUE) {
            messageBO.setVersion(MessageVersion.V2);
        }
    }

    @Override
    public SelectedMappedBuffer select(long offset, int size) {
        return null;
    }

    @Override
    public SelectedMappedBuffer select(long offset) {
        return null;
    }

    @Override
    public List<SelectedMappedBuffer> selectAll(long offset, int size) {
        return List.of();
    }

    @Override
    public long getMinOffset() {
        return mappedFileQueue.getMinOffset();
    }

    @Override
    public long getMaxOffset() {
        return mappedFileQueue.getMaxOffset();
    }

    @Override
    public long getFlushedOffset() {
        return mappedFileQueue.getFlushPosition();
    }

    @Override
    public long getUnFlushedSize() {
        return mappedFileQueue.getUnFlushedSize();
    }

    private void initLocalEncoder() {
        localEncoder = ThreadLocal.withInitial(
            () -> new EnqueueThreadLocal(messageConfig)
        );
    }
}
