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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.coderule.minimq.store.domain.commitlog.vo;

import cn.coderule.minimq.domain.domain.enums.EnqueueStatus;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupCommitRequest implements Serializable {
    private long offset;
    private long nextOffset;
    /**
     * Indicate the GroupCommitRequest result: true or false
     */
    @Builder.Default
    private CompletableFuture<EnqueueStatus> flushOKFuture = new CompletableFuture<>();
    /**
     * slave nums, in controller mode: -1
     */
    @Builder.Default
    private volatile int ackNums = 1;

    private long deadLine;

    public void wakeup(EnqueueStatus status) {
        this.flushOKFuture.complete(status);
    }

    public CompletableFuture<EnqueueStatus> future() {
        return flushOKFuture;
    }
}
