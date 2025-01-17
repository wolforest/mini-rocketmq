package com.wolf.minimq.store.domain.commitlog.flush;

import com.wolf.common.lang.concurrent.ServiceThread;
import com.wolf.minimq.store.domain.commitlog.vo.GroupCommitRequest;

public abstract class Flusher extends ServiceThread {
    protected static final int RETRY_TIMES = 10;
    protected long maxOffset = 0;

    public void setMaxOffset(long maxOffset) {
        if (maxOffset > this.maxOffset) {
            this.maxOffset = maxOffset;
        }
    }

    public void addRequest(GroupCommitRequest request) {

    }
}