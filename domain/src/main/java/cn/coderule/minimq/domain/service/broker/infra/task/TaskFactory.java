package cn.coderule.minimq.domain.service.broker.infra.task;

import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;

public interface TaskFactory {
    void create(QueueTask task);
}
