package cn.coderule.minimq.rpc.store.facade;

import cn.coderule.minimq.domain.domain.meta.offset.GroupResult;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetFilter;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.minimq.domain.domain.meta.offset.TopicResult;

public interface ConsumeOffsetFacade {
    OffsetResult getOffset(OffsetRequest request);
    OffsetResult getAndRemove(OffsetRequest request);

    void putOffset(OffsetRequest request);

    void deleteByTopic(OffsetFilter filter);
    void deleteByGroup(OffsetFilter filter);

    TopicResult findTopicByGroup(OffsetFilter filter);
    GroupResult findGroupByTopic(OffsetFilter filter);
}
