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
package cn.coderule.minimq.test.apitest.transaction;

import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.test.apitest.ApiBaseTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
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
import org.apache.rocketmq.client.apis.producer.Transaction;
import org.apache.rocketmq.client.apis.producer.TransactionChecker;
import org.apache.rocketmq.client.apis.producer.TransactionResolution;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {"client"})
public class TransactionFailureTest extends ApiBaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionFailureTest.class);
    private static final String TOPIC = TopicManager.createUniqueTopic();
    private static final String CONSUMER_GROUP = GroupManager.createUniqueGroup();
    private static final String MESSAGE_PREFIX = "MQ_TRX_";
    private static final String MESSAGE_BODY = "transaction message body: ";

    private PushConsumer consumer;
    private Producer producer;

    @BeforeMethod
    public void beforeMethod() {
        TopicManager.createTransactionalTopic(TOPIC);
        GroupManager.createGroup(CONSUMER_GROUP);
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
                Transaction transaction = producer.beginTransaction();

                SendReceipt sendReceipt = producer.send(message, transaction);
                Assert.assertNotNull(sendReceipt);

                String messageId = sendReceipt.getMessageId().toString();
                Assert.assertNotNull(messageId);
                LOG.info("pub message: {}", sendReceipt);

                transaction.rollback();
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
        producer = ProducerManager.buildTransactionalProducer(createChecker(), TOPIC);
    }

    private void startConsumer() {
        LOG.info("create consumer");
        consumer = ConsumerManager.buildPushConsumer(CONSUMER_GROUP, createFilter(), createListener());
    }

    private void stopConsumer() throws IOException {
        if (consumer == null) {
            return;
        }

        ThreadUtil.sleep(10000);
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

    private TransactionChecker createChecker() {
        return message -> {
            LOG.info("check message: {}", message);
            return TransactionResolution.COMMIT;
        };
    }

    private MessageListener createListener() {
        LOG.info("create consume listener");
        return message -> {
            LOG.info("Consume message={}", message);
            Assert.fail("consumer received unexpected messages");

            return ConsumeResult.SUCCESS;
        };
    }

    private Message createMessage(int i) {
        return ClientManager.getProvider()
            .newMessageBuilder()
            .setTopic(TOPIC)
            .setKeys(MESSAGE_PREFIX + i)
            .setBody((MESSAGE_BODY + i).getBytes(StandardCharsets.UTF_8))
            .build();
    }

}
