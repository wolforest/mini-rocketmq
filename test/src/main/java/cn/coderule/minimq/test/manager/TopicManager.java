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
package cn.coderule.minimq.test.manager;

import cn.coderule.common.util.lang.string.StringUtil;
import java.util.HashMap;
import java.util.Map;
import org.apache.rocketmq.common.domain.topic.TopicAttributes;
import org.apache.rocketmq.common.domain.topic.TopicConfig;
import org.apache.rocketmq.common.domain.topic.TopicMessageType;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;

public class TopicManager {
    private static final String TOPIC_PREFIX = "MQT_";

    public static TopicConfig findTopic(String topic) {
        ClusterInfo clusterInfo = BrokerManager.getClusterInfo();
        if (clusterInfo == null) {
            return null;
        }

        try {
            TopicConfig topicConfig = null;
            for (String addr: clusterInfo.getAllAddr()) {
                topicConfig = ClientManager.getClient().getTopicConfig(addr, topic);
                if (topicConfig == null) {
                    return null;
                }
            }

            return topicConfig;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteTopic(String topic) {
        ClusterInfo clusterInfo = BrokerManager.getClusterInfo();
        if (clusterInfo == null) {
            return;
        }

        try {
            for (String addr: clusterInfo.getAllAddr()) {
                ClientManager.getClient().deleteTopicInBroker(addr, topic);
            }

            String nameAddr = ConfigManager.getConfig().getString("nameAddr");
            ClientManager.getClient().deleteTopicInNameServer(nameAddr, topic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean createTopic(String topic) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("+" + TopicAttributes.TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName(), TopicMessageType.NORMAL.getValue());
        return createTopic(topic, attributes);
    }

    public static boolean createFIFOTopic(String topic) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("+" + TopicAttributes.TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName(), TopicMessageType.FIFO.getValue());
        return createTopic(topic, attributes);
    }

    public static boolean createDelayTopic(String topic) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("+" + TopicAttributes.TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName(), TopicMessageType.DELAY.getValue());
        return createTopic(topic, attributes);
    }

    public static boolean createTransactionalTopic(String topic) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("+" + TopicAttributes.TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName(), TopicMessageType.TRANSACTION.getValue());
        return createTopic(topic, attributes);
    }

    public static boolean createTopic(String topic, Map<String, String> attributes) {
        ClusterInfo clusterInfo = BrokerManager.getClusterInfo();
        if (clusterInfo == null) {
            return false;
        }
        try {
            TopicConfig topicConfig = new TopicConfig(topic);
            topicConfig.setAttributes(attributes);

            for (String addr: clusterInfo.getAllAddr()) {
                ClientManager.getClient().createTopic(addr, topicConfig);
            }

            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

    public static String createUniqueTopic() {
        return TOPIC_PREFIX + StringUtil.uuid();
    }
}
