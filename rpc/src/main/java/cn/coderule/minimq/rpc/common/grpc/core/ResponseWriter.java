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

package cn.coderule.minimq.rpc.common.grpc.core;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseWriter {

    protected static final Object INSTANCE_CREATE_LOCK = new Object();
    protected static volatile ResponseWriter instance;

    public static ResponseWriter getInstance() {
        if (instance != null) {
            return instance;
        }

        synchronized (INSTANCE_CREATE_LOCK) {
            if (instance == null) {
                instance = new ResponseWriter();
            }
        }
        return instance;
    }

    public <T> void write(StreamObserver<T> observer, final T response) {
        if (writeResponse(observer, response)) {
            observer.onCompleted();
        }
    }

    public <T> boolean writeResponse(StreamObserver<T> observer, final T response) {
        if (null == response) {
            return false;
        }
        log.debug("start to write response. response: {}", response);
        if (isCancelled(observer)) {
            log.warn("client has cancelled the request. response to write: {}", response);
            return false;
        }

        try {
            observer.onNext(response);
        } catch (StatusRuntimeException e) {
            if (Status.CANCELLED.equals(e.getStatus())) {
                log.warn("client has cancelled the request. response to write: {}", response);
                return false;
            }
            throw e;
        }
        return true;
    }

    public <T> boolean isCancelled(StreamObserver<T> observer) {
        if (observer instanceof ServerCallStreamObserver<T> serverCallStreamObserver) {
            return serverCallStreamObserver.isCancelled();
        }
        return false;
    }
}

