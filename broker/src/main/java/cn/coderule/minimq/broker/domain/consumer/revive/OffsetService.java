package cn.coderule.minimq.broker.domain.consumer.revive;

import cn.coderule.minimq.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.revive.ReviveBuffer;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.store.facade.ConsumeOffsetFacade;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OffsetService {
    private final String reviveTopic;
    private final int queueId;

    @Getter @Setter
    private long reviveOffset;

    @Getter @Setter
    private long reviveTimestamp = -1;
    @Setter
    private volatile boolean skipRevive = false;

    private final MQFacade mqFacade;
    private final ConsumeOffsetFacade consumeOffsetFacade;

    public OffsetService(ReviveContext context, int queueId) {
        this.reviveTopic = context.getReviveTopic();
        this.queueId = queueId;

        this.mqFacade = context.getMqFacade();
        this.consumeOffsetFacade = context.getConsumeOffsetFacade();
    }

    public long getReviveDelayTime() {
        if (reviveTimestamp <= 0) {
            return 0;
        }

        QueueRequest request = QueueRequest.builder()
            .topic(reviveTopic)
            .queueId(queueId)
            .build();

        long maxOffset = mqFacade.getMaxOffset(request).getMaxOffset();
        if (maxOffset > reviveOffset + 1) {
            long now = System.currentTimeMillis();
            return Math.max(now, reviveTimestamp);
        }

        return 0;
    }

    public long getReviveDelayNumber() {
        if (reviveTimestamp <= 0) {
            return 0;
        }

        QueueRequest request = QueueRequest.builder()
            .topic(reviveTopic)
            .queueId(queueId)
            .build();

        long maxOffset = mqFacade.getMaxOffset(request).getMaxOffset();
        long diff = maxOffset - reviveOffset;
        return Math.max(diff, 0);
    }

    public void initOffset() {
        log.info("start revive topic={}; reviveQueueId={}",
            reviveTopic, queueId);

        OffsetRequest request = OffsetRequest.builder()
            .consumerGroup(PopConstants.REVIVE_GROUP)
            .topicName(reviveTopic)
            .queueId(queueId)
            .build();

        reviveOffset = consumeOffsetFacade.
            getOffset(request)
            .getOffset();
    }

    public void resetOffset(ReviveBuffer buffer) {
        if (skipRevive) {
            return;
        }

        reviveOffset = buffer.getOffset();

        if (buffer.getOffset() <= buffer.getInitialOffset()) {
            return;
        }

        commitOffset(reviveOffset);
    }

    private void commitOffset(long offset) {
        OffsetRequest request = OffsetRequest.builder()
            .consumerGroup(PopConstants.REVIVE_GROUP)
            .topicName(reviveTopic)
            .queueId(queueId)
            .newOffset(offset)
            .build();

        consumeOffsetFacade.putOffset(request);
    }

}
