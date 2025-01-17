package com.wolf.minimq.store.domain.meta;

import com.wolf.common.util.io.FileUtil;
import com.wolf.common.util.lang.JSONUtil;
import com.wolf.common.util.lang.StringUtil;
import com.wolf.minimq.domain.model.meta.ConsumeOffset;
import com.wolf.minimq.domain.service.store.domain.meta.ConsumeOffsetStore;

public class DefaultConsumeOffsetStore implements ConsumeOffsetStore {
    private final String storePath;
    private ConsumeOffset consumeOffset;

    public DefaultConsumeOffsetStore(String storePath) {
        this.storePath = storePath;
    }

    @Override
    public Long getOffset(String group, String topic, int queueId) {
        return consumeOffset.getOffset(group, topic, queueId);
    }

    @Override
    public Long getAndRemove(String group, String topic, int queueId) {
        return consumeOffset.getAndRemove(group, topic, queueId);
    }

    @Override
    public void putOffset(String group, String topic, int queueId, long offset) {
        consumeOffset.putOffset(group, topic, queueId, offset);
    }

    @Override
    public void load() {
        if (!FileUtil.exists(storePath)) {
            init();
            return;
        }

        String data = FileUtil.fileToString(storePath);
        decode(data);
    }

    @Override
    public void store() {
        String data = JSONUtil.toJSONString(consumeOffset);
        FileUtil.stringToFile(data, storePath);
    }

    private void init() {
        if (consumeOffset != null) {
            return;
        }

        this.consumeOffset = new ConsumeOffset();
    }

    private void decode(String data) {
        if (StringUtil.isBlank(data)) {
            init();
            return;
        }

        this.consumeOffset = JSONUtil.parse(data, ConsumeOffset.class);
    }
}
