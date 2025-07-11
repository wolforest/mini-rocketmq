/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package cn.coderule.minimq.domain.core.lock.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * lock topic@queueId for assignOffset/increaseOffset
 */
public class EnqueueLock {
    private final int size;
    private final List<Lock> lockList;

    public EnqueueLock() {
        this(32);
    }

    public EnqueueLock(int size) {
        this.size = size;
        this.lockList = new ArrayList<>(size);

        for (int i = 0; i < this.size; i++) {
            this.lockList.add(new ReentrantLock());
        }
    }

    /**
     * lock(topic&queueId) to append commitLog
     *  - mainly for assign and increase consumeQueue offset
     *
     * @param topic topic
     * @param queueId queueId
     */
    public void lock(String topic, int queueId) {
        String topicQueueKey = getTopicKey(topic, queueId);
        int index = (topicQueueKey.hashCode() & 0x7fffffff) % this.size;
        Lock lock = this.lockList.get(index);
        lock.lock();
    }

    public void unlock(String topic, int queueId) {
        String topicQueueKey = getTopicKey(topic, queueId);
        int index = (topicQueueKey.hashCode() & 0x7fffffff) % this.size;
        Lock lock = this.lockList.get(index);
        lock.unlock();
    }

    private String getTopicKey(String topic, int queueId) {
        return topic + "@" + queueId;
    }
}
