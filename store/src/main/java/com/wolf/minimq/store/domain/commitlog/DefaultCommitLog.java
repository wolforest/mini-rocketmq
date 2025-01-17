package com.wolf.minimq.store.domain.commitlog;

import com.wolf.common.util.encrypt.HashUtil;
import com.wolf.minimq.domain.config.CommitLogConfig;
import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import com.wolf.minimq.domain.enums.MessageVersion;
import com.wolf.minimq.domain.exception.EnqueueException;
import com.wolf.minimq.domain.model.dto.InsertFuture;
import com.wolf.minimq.domain.model.dto.InsertResult;
import com.wolf.minimq.domain.model.dto.SelectedMappedBuffer;
import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.utils.MessageDecoder;
import com.wolf.minimq.domain.utils.lock.CommitLogLock;
import com.wolf.minimq.domain.utils.lock.CommitLogReentrantLock;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.store.domain.commitlog.flush.FlushManager;
import com.wolf.minimq.store.domain.commitlog.vo.EnqueueThreadLocal;
import com.wolf.minimq.store.domain.commitlog.vo.InsertContext;
import com.wolf.minimq.store.infra.memory.CLibrary;
import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * depend on:
 *  - CommitLogConfig
 *  - MappedFileQueue
 *  - FlushManager
 */
@Slf4j
public class DefaultCommitLog implements CommitLog {
    private final CommitLogConfig commitLogConfig;
    private final MessageConfig messageConfig;
    private final FlushManager flushManager;

    @Getter
    private final MappedFileQueue mappedFileQueue;
    private final CommitLogLock commitLogLock;
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

        this.commitLogLock = new CommitLogReentrantLock();
        initLocalEncoder();
    }

    @Override
    public InsertFuture insert(MessageBO messageBO) {
        InsertContext context = initContext(messageBO);

        this.commitLogLock.lock();
        try {
            MappedFile mappedFile = getMappedFile(context.getEncoder().getMessageLength());
            assignOffset(messageBO, mappedFile);

            ByteBuffer messageBuffer = context.getEncoder().encode();
            InsertResult insertResult = mappedFile.insert(messageBuffer);
            handleInsertError(insertResult);

            return flushManager.flush(insertResult, messageBO);
        } catch (EnqueueException messageException) {
            return InsertFuture.failure(messageException.getStatus());
        } finally {
            this.commitLogLock.unlock();
        }
    }

    @Override
    public InsertResult insert(long offset, byte[] data, int start, int size) {
        this.commitLogLock.lock();
        try {
            MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
            if (mappedFile == null) {
                return InsertResult.failure();
            }

            return mappedFile.insert(data, start, size);
        } catch (Exception e) {
            log.error("commitLog insert error", e);
            return InsertResult.failure();
        } finally {
            this.commitLogLock.unlock();
        }
    }

    @Override
    public MessageBO select(long offset, int size) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (mappedFile == null) {
            return null;
        }

        int position = (int) (offset % commitLogConfig.getFileSize());
        SelectedMappedBuffer buffer = mappedFile.select(position, size);
        if (buffer == null) {
            return null;
        }

        MessageBO messageBO = MessageDecoder.decode(buffer.getByteBuffer());
        buffer.release();

        return messageBO;
    }

    @Override
    public MessageBO select(long offset) {
        SelectedMappedBuffer buffer = selectBuffer(offset);
        if (buffer == null) {
            return null;
        }

        int size = buffer.getByteBuffer().getInt();
        buffer.getByteBuffer().limit(size);

        MessageBO messageBO = MessageDecoder.decode(buffer.getByteBuffer());
        buffer.release();

        return messageBO;
    }

    @Override
    public SelectedMappedBuffer selectBuffer(long offset) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (mappedFile == null) {
            return null;
        }

        int position = (int) (offset % commitLogConfig.getFileSize());
        return mappedFile.select(position);
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

    private InsertContext initContext(MessageBO messageBO) {
        initMessage(messageBO);

        long now = System.currentTimeMillis();
        messageBO.setStoreTimestamp(now);

        return InsertContext.builder()
            .now(now)
            .messageBO(messageBO)
            .encoder(localEncoder.get().getEncoder(messageBO))
            .build();
    }

    private void initMessage(MessageBO messageBO) {
        messageBO.setBodyCRC(HashUtil.crc32(messageBO.getBody()));

        messageBO.setVersion(MessageVersion.V1);
        if (messageBO.getTopic().length() > Byte.MAX_VALUE) {
            messageBO.setVersion(MessageVersion.V2);
        }
    }

    private MappedFile getMappedFile(int size) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileForSize(size);
        mappedFile.setFileMode(CLibrary.MADV_RANDOM);

        return mappedFile;
    }

    private void assignOffset(MessageBO messageBO, MappedFile mappedFile) {
        long commitLogOffset = mappedFile.getMinOffset() + mappedFile.getInsertPosition();
        messageBO.setCommitLogOffset(commitLogOffset);
    }

    private void handleInsertError(InsertResult insertResult) {
        switch (insertResult.getStatus()) {
            case END_OF_FILE -> throw new EnqueueException(EnqueueStatus.END_OF_FILE);
            case MESSAGE_SIZE_EXCEEDED, PROPERTIES_SIZE_EXCEEDED -> throw new EnqueueException(EnqueueStatus.MESSAGE_ILLEGAL);
            default -> throw new EnqueueException(EnqueueStatus.UNKNOWN_ERROR);
        }
    }

}
