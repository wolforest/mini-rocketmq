package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.domain.timer.context.TimerContext;
import cn.coderule.minimq.broker.infra.store.TimerStore;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerTaskSaver extends ServiceThread {
    private static final int BATCH_SIZE = 10;

    private final TimerContext timerContext;
    private final TimerConfig timerConfig;
    private final TimerQueue timerQueue;
    private final TimerState timerState;
    private final TimerStore timerStore;

    private QueueTask queueTask;

    public TimerTaskSaver(TimerContext context) {
        this.timerContext = context;
        BrokerConfig brokerConfig = context.getBrokerConfig();
        this.timerConfig = brokerConfig.getTimerConfig();
        this.timerQueue = context.getTimerQueue();
        this.timerState = context.getTimerState();
        this.timerStore = context.getTimerStore();
    }

    @Override
    public String getServiceName() {
        return TimerTaskSaver.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());
        if (!loadQueueTask()) {
            return;
        }

        while (!this.isStopped() || !timerQueue.isConsumeQueueEmpty()) {
            try {
                save();
            } catch (Throwable t) {
                log.error("{} service has exception. ", this.getServiceName(), t);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    private boolean loadQueueTask() {
        log.debug("load queue task");

        try {
            queueTask = timerContext.getOrWaitQueueTask();
        } catch (Exception e) {
            log.error("load queue task error", e);
            return false;
        }

        return true;
    }

    private void save() throws Exception {
        long tmpOffset = timerState.getTimerQueueOffset();
        List<TimerEvent> eventList = pullTimerEvent();
        if (CollectionUtil.isEmpty(eventList)) {
            timerState.setCommitSaveTime(tmpOffset);
            timerState.tryMoveSaveTime();
            return;
        }

        while (!timerState.isRunning()) {
            boolean success = save(eventList);
            if (success) {
                break;
            }
        }

        TimerEvent last = eventList.get(eventList.size() - 1);
        timerState.setCommitSaveTime(last.getMessageBO().getQueueOffset());
        timerState.tryMoveSaveTime();
    }

    private boolean save(List<TimerEvent> eventList) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(eventList.size());
        for (TimerEvent event : eventList) {
            event.setLatch(latch);
            save(event);
        }

        timerContext.awaitLatch(latch);
        boolean success = eventList.stream()
            .allMatch(TimerEvent::isSuccess);

        if (success) {
            return true;
        }

        ThreadUtil.sleep(50);
        return false;
    }

    private void save(TimerEvent event) {
        try {
            boolean shouldFire = event.getDelayTime() < timerState.getLastSaveTime();
            if (timerState.isEnableScan() && shouldFire) {
                event.setEnqueueTime(Long.MAX_VALUE);
                timerQueue.putProduceEvent(event);
                return;
            }

            event.setStoreGroup(queueTask.getStoreGroup());
            boolean success = timerStore.addTimer(event);

            boolean status = success || timerConfig.isSkipUnknownError();
            event.idempotentRelease(status);
        } catch (Throwable t) {
            handleSaveException(t, event);
        }
    }

    private void handleSaveException(Throwable t, TimerEvent timerEvent) {
        log.error("Unknown error", t);
        if (timerConfig.isSkipUnknownError()) {
            timerEvent.idempotentRelease(true);
        } else {
            ThreadUtil.sleep(50);
        }
    }

    private List<TimerEvent> pullTimerEvent() throws InterruptedException {
        TimerEvent firstReq = timerQueue.pollConsumeEvent(10);
        if (null == firstReq) {
            return null;
        }

        List<TimerEvent> eventList = new ArrayList<>(16);
        eventList.add(firstReq);
        pullMoreEvents(eventList);

        return eventList;
    }

    private void pullMoreEvents(List<TimerEvent> timerEventList) throws InterruptedException {
        while (true) {
            TimerEvent tmpReq = timerQueue.pollConsumeEvent(3);
            if (null == tmpReq) {
                break;
            }

            timerEventList.add(tmpReq);

            if (timerEventList.size() > BATCH_SIZE) {
                break;
            }
        }
    }
}
