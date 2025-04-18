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

package cn.coderule.minimq.domain.domain.enums;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Getter;

@Getter
public enum MessageType {
    UNSPECIFIED("UNSPECIFIED"),
    NORMAL("NORMAL"),
    FIFO("FIFO"),
    DELAY("DELAY"),
    TRANSACTION("TRANSACTION"),
    MIXED("MIXED");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public static Set<String> typeSet() {
        return Sets.newHashSet(UNSPECIFIED.value, NORMAL.value, FIFO.value, DELAY.value, TRANSACTION.value, MIXED.value);
    }
}
