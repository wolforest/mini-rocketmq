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
package cn.coderule.minimq.test.apitest.pubsub;

import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.test.apitest.ApiBaseTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import cn.coderule.minimq.test.manager.ClientManager;
import cn.coderule.minimq.test.manager.ConsumerManager;
import cn.coderule.minimq.test.manager.GroupManager;
import cn.coderule.minimq.test.manager.ProducerManager;
import cn.coderule.minimq.test.manager.TopicManager;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Before running OrderlyTest,You should set the fifo property for the consumer-group to True
 */
@Test(groups = {"client"})
public class OrderlyTest extends ApiBaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(OrderlyTest.class);
    private static final String TOPIC = TopicManager.createUniqueTopic();
    private static final String CONSUMER_GROUP = GroupManager.createUniqueGroup();
    private static final String PRODUCE_GROUP = GroupManager.createUniqueGroup();
    private static final String MESSAGE_KEY_PREFIX = "MQM_DL_";
    private static final String MESSAGE_BODY = "orderly message body: ";

    private PushConsumer consumer;
    private Producer producer;

    private final Set<String> messageIdSet = new HashSet<>();
    private int lastKeyIndex = 0;


    @BeforeMethod
    public void beforeMethod() {
        TopicManager.createFIFOTopic(TOPIC);
        GroupManager.createOrderlyGroup(CONSUMER_GROUP);
        createProducer();
        startConsumer();
    }

    @AfterMethod
    public void afterMethod() {
        try {
            stopConsumer();
            stopProducer();
            TopicManager.deleteTopic(TOPIC);
            GroupManager.deleteGroup(CONSUMER_GROUP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendOk() {
        if (producer == null) {
            return;
        }

        for (int i = 0; i < 100; i++) {
            Message message = createMessage(i);

            try {
                SendReceipt sendReceipt = producer.send(message);
                Assert.assertNotNull(sendReceipt);

                String messageId = sendReceipt.getMessageId().toString();
                Assert.assertNotNull(messageId);

                messageIdSet.add(messageId);
                LOG.info("pub message: {}", sendReceipt);
            } catch (Throwable t) {
                LOG.error("Failed to send message: {}", i, t);
            }
        }
    }

    private Map<String, FilterExpression> createFilter() {
        FilterExpression expression = new FilterExpression("*");
        return Collections.singletonMap(TOPIC, expression);
    }

    private void createProducer() {
        producer = ProducerManager.buildProducer(TOPIC);
    }

    private void startConsumer() {
        LOG.info("create consumer");
        consumer = ConsumerManager.buildPushConsumer(CONSUMER_GROUP, createFilter(), createListener());
    }

    private void stopConsumer() throws IOException {
        if (consumer == null) {
            return;
        }

        ThreadUtil.sleep(30000);
        LOG.info("stop consumer");

        consumer.close();
    }

    private void stopProducer() throws IOException {
        if (producer == null) {
            return;
        }

        LOG.info("stop producer");
        producer.close();
    }

    private MessageListener createListener() {
        LOG.info("create consume listener");
        return message -> {
            LOG.info("Consume message={}", message);
            String messageId = message.getMessageId().toString();

            Assert.assertEquals(TOPIC, message.getTopic());
            Assert.assertTrue(messageIdSet.contains(messageId));

            int keyIndex = getKeyIndex(message.getKeys());
            LOG.info("current message index: {}; last message index: {}", keyIndex, lastKeyIndex);

            Assert.assertTrue(keyIndex > lastKeyIndex);

            lastKeyIndex = keyIndex;
            return ConsumeResult.SUCCESS;
        };
    }

    private int getKeyIndex(Collection<String> keys) {
        if (keys.isEmpty()) {
            return -1;
        }

        String key = keys.iterator().next();
        if (StringUtil.isBlank(key)) {
            return -1;
        }

        String num = key.substring(MESSAGE_KEY_PREFIX.length() - 1);
        return Integer.parseInt(num);
    }

    private Message createMessage(int i) {
        return ClientManager.getProvider()
                .newMessageBuilder()
                .setTopic(TOPIC)
                .setKeys(MESSAGE_KEY_PREFIX + i)
                .setBody((MESSAGE_BODY + i).getBytes(StandardCharsets.UTF_8))
                .setMessageGroup(PRODUCE_GROUP)
                .build();
    }

}
