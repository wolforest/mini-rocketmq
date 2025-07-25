package cn.coderule.minimq.store.server.ha.server;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.server.processor.CommitLogSynchronizer;
import java.util.concurrent.atomic.AtomicLong;

public class SlaveOffsetCounter {
    private final StoreConfig storeConfig;
    private final ConnectionPool connectionPool;
    private final AtomicLong maxOffset;
    private final AtomicLong minOffset;

    private CommitLogSynchronizer commitLogSynchronizer;

    public SlaveOffsetCounter(StoreConfig storeConfig, ConnectionPool connectionPool) {
        this.storeConfig = storeConfig;
        this.connectionPool = connectionPool;
        maxOffset = new AtomicLong(0);
        minOffset = new AtomicLong(0);
    }

    public long getMaxOffset() {
        return maxOffset.get();
    }

    public boolean isSlaveOk(long masterOffset) {
        boolean status = connectionPool.getConnectionCount() > 0;
        int maxGap = storeConfig.getMaxSlaveGap();
        status = status && masterOffset - maxOffset.get() < maxGap;
        return status;
    }

    public void update(long slaveOffset) {
        setMaxOffset(slaveOffset);
        // setMinOffset(slaveOffset);
    }

    public void setMaxOffset(long slaveOffset) {
        for (long value = maxOffset.get(); slaveOffset > value; ) {
            if (maxOffset.compareAndSet(value, slaveOffset)) {
                break;
            }

            value = maxOffset.get();
        }
    }

    public void setMinOffset(long slaveOffset) {
        for (long value = minOffset.get(); slaveOffset < value; ) {
            if (minOffset.compareAndSet(value, slaveOffset)) {
                break;
            }

            value = minOffset.get();
        }
    }


}
