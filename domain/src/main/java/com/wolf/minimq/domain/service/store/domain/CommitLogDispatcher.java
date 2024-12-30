package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.dto.CommitLogEvent;

public interface CommitLogDispatcher {
    long getStartOffset();
    void setStartOffset(long offset);

    void registerHandler(CommitLogHandler handler);
    void dispatch(CommitLogEvent event);
}
