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
package cn.coderule.minimq.broker.domain.transaction.monitor;


import cn.coderule.minimq.domain.domain.meta.DataVersion;
import cn.coderule.minimq.domain.domain.meta.topic.TopicValidator;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import com.google.common.io.Files;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TransactionMetrics {

    private ConcurrentMap<String, Metric> transactionCounts =
            new ConcurrentHashMap<>(1024);

    private DataVersion dataVersion = new DataVersion();

    private final String configPath;

    public TransactionMetrics(String configPath) {
        this.configPath = configPath;
    }

    public long addAndGet(String topic, int value) {
        Metric pair = getTopicPair(topic);
        getDataVersion().nextVersion();
        pair.setTimeStamp(System.currentTimeMillis());
        return pair.getCount().addAndGet(value);
    }

    public Metric getTopicPair(String topic) {
        Metric pair = transactionCounts.get(topic);
        if (null != pair) {
            return pair;
        }
        pair = new Metric();
        final Metric previous = transactionCounts.putIfAbsent(topic, pair);
        if (null != previous) {
            return previous;
        }
        return pair;
    }
    public long getTransactionCount(String topic) {
        Metric pair = transactionCounts.get(topic);
        if (null == pair) {
            return 0;
        } else {
            return pair.getCount().get();
        }
    }

    public Map<String, Metric> getTransactionCounts() {
        return transactionCounts;
    }
    public void setTransactionCounts(ConcurrentMap<String, Metric> transactionCounts) {
        this.transactionCounts = transactionCounts;
    }

    protected void write0(Writer writer) {
        TransactionMetricsSerializeWrapper wrapper = new TransactionMetricsSerializeWrapper();
        wrapper.setTransactionCount(transactionCounts);
        wrapper.setDataVersion(dataVersion);
    }

    public String encode() {
        return encode(false);
    }

    public String configFilePath() {
        return configPath;
    }

    public void decode(String jsonString) {
        if (jsonString != null) {
            TransactionMetricsSerializeWrapper transactionMetricsSerializeWrapper =
                    TransactionMetricsSerializeWrapper.fromJson(jsonString, TransactionMetricsSerializeWrapper.class);
            if (transactionMetricsSerializeWrapper != null) {
                this.transactionCounts.putAll(transactionMetricsSerializeWrapper.getTransactionCount());
                this.dataVersion.assign(transactionMetricsSerializeWrapper.getDataVersion());
            }
        }
    }

    public String encode(boolean prettyFormat) {
        TransactionMetricsSerializeWrapper metricsSerializeWrapper = new TransactionMetricsSerializeWrapper();
        metricsSerializeWrapper.setDataVersion(this.dataVersion);
        metricsSerializeWrapper.setTransactionCount(this.transactionCounts);
        return metricsSerializeWrapper.toJson(prettyFormat);
    }

    public DataVersion getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(DataVersion dataVersion) {
        this.dataVersion = dataVersion;
    }

    public void cleanMetrics(Set<String> topics) {
        if (topics == null || topics.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<String, Metric>> iterator = transactionCounts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Metric> entry = iterator.next();
            final String topic = entry.getKey();
            if (topic.startsWith(TopicValidator.SYSTEM_TOPIC_PREFIX)) {
                continue;
            }
            if (!topics.contains(topic)) {
                continue;
            }
            // in the input topics set, then remove it.
            iterator.remove();
        }
    }

    public static class TransactionMetricsSerializeWrapper extends RpcSerializable {
        private ConcurrentMap<String, Metric> transactionCount =
                new ConcurrentHashMap<>(1024);
        private DataVersion dataVersion = new DataVersion();

        public ConcurrentMap<String, Metric> getTransactionCount() {
            return transactionCount;
        }

        public void setTransactionCount(
                ConcurrentMap<String, Metric> transactionCount) {
            this.transactionCount = transactionCount;
        }

        public DataVersion getDataVersion() {
            return dataVersion;
        }

        public void setDataVersion(DataVersion dataVersion) {
            this.dataVersion = dataVersion;
        }
    }

    public synchronized void persist() {
        String config = configFilePath();
        String temp = config + ".tmp";
        String backup = config + ".bak";
        BufferedWriter bufferedWriter = null;
        try {
            File tmpFile = new File(temp);
            File parentDirectory = tmpFile.getParentFile();
            if (!parentDirectory.exists()) {
                if (!parentDirectory.mkdirs()) {
                    log.error("Failed to create directory: {}", parentDirectory.getCanonicalPath());
                    return;
                }
            }

            if (!tmpFile.exists()) {
                if (!tmpFile.createNewFile()) {
                    log.error("Failed to create file: {}", tmpFile.getCanonicalPath());
                    return;
                }
            }
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile, false),
                    StandardCharsets.UTF_8));
            write0(bufferedWriter);
            bufferedWriter.flush();
            bufferedWriter.close();
            log.debug("Finished writing tmp file: {}", temp);

            File configFile = new File(config);
            if (configFile.exists()) {
                Files.copy(configFile, new File(backup));
                configFile.delete();
            }

            tmpFile.renameTo(configFile);
        } catch (IOException e) {
            log.error("Failed to persist {}", temp, e);
        } finally {
            if (null != bufferedWriter) {
                try {
                    bufferedWriter.close();
                } catch (IOException ignore) {
                }
            }
        }
    }


}
