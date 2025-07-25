package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;

/**
 * Timer APIs, for M/S
 */
public interface TimerStore {
    void storeCheckpoint(TimerCheckpoint checkpoint);
    TimerCheckpoint loadCheckpoint();

    boolean addTimer(TimerEvent event);
    ScanResult scan(long delayTime);
}
