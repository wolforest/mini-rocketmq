
package cn.coderule.minimq.rpc.broker.rpc.protocol.body;

import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.core.enums.consume.ConsumeType;
import cn.coderule.minimq.domain.domain.consumer.running.ConsumeStatus;
import cn.coderule.minimq.domain.domain.consumer.running.PopProcessQueueInfo;
import cn.coderule.minimq.domain.domain.consumer.running.ProcessQueueInfo;
import cn.coderule.minimq.domain.domain.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;


public class ConsumerRunningInfo extends RpcSerializable {
    public static final String PROP_NAMESERVER_ADDR = "PROP_NAMESERVER_ADDR";
    public static final String PROP_THREADPOOL_CORE_SIZE = "PROP_THREADPOOL_CORE_SIZE";
    public static final String PROP_CONSUME_ORDERLY = "PROP_CONSUMEORDERLY";
    public static final String PROP_CONSUME_TYPE = "PROP_CONSUME_TYPE";
    public static final String PROP_CLIENT_VERSION = "PROP_CLIENT_VERSION";
    public static final String PROP_CONSUMER_START_TIMESTAMP = "PROP_CONSUMER_START_TIMESTAMP";

    private Properties properties = new Properties();

    private TreeSet<SubscriptionData> subscriptionSet = new TreeSet<>();

    private TreeMap<MessageQueue, ProcessQueueInfo> mqTable = new TreeMap<>();

    private TreeMap<MessageQueue, PopProcessQueueInfo> mqPopTable = new TreeMap<>();

    private TreeMap<String/* Topic */, ConsumeStatus> statusTable = new TreeMap<>();

    private final TreeMap<String, String> userConsumerInfo = new TreeMap<>();

    private String jstack;

    public static boolean analyzeSubscription(final TreeMap<String/* clientId */, ConsumerRunningInfo> criTable) {
        ConsumerRunningInfo prev = criTable.firstEntry().getValue();

        boolean push = isPushType(prev);

        boolean startForAWhile = false;
        {

            String property = prev.getProperties().getProperty(ConsumerRunningInfo.PROP_CONSUMER_START_TIMESTAMP);
            if (property == null) {
                property = String.valueOf(prev.getProperties().get(ConsumerRunningInfo.PROP_CONSUMER_START_TIMESTAMP));
            }
            startForAWhile = (System.currentTimeMillis() - Long.parseLong(property)) > (1000 * 60 * 2);
        }

        if (push && startForAWhile) {

            {
                for (Entry<String, ConsumerRunningInfo> next : criTable.entrySet()) {
                    ConsumerRunningInfo current = next.getValue();
                    boolean equals = current.getSubscriptionSet().equals(prev.getSubscriptionSet());

                    if (!equals) {
                        // Different subscription in the same group of consumer
                        return false;
                    }

                    prev = next.getValue();
                }

                // after consumer.unsubscribe , SubscriptionSet is Empty
                //if (prev != null) {
                //
                //    if (prev.getSubscriptionSet().isEmpty()) {
                //        // Subscription empty!
                //        return false;
                //    }
                //}
            }
        }

        return true;
    }

    public static boolean isPushType(ConsumerRunningInfo consumerRunningInfo) {
        String property = consumerRunningInfo.getProperties().getProperty(ConsumerRunningInfo.PROP_CONSUME_TYPE);

        if (property == null) {
            property = ((ConsumeType) consumerRunningInfo.getProperties().get(ConsumerRunningInfo.PROP_CONSUME_TYPE)).name();
        }
        return ConsumeType.valueOf(property) == ConsumeType.CONSUME_PASSIVELY;
    }

    public static boolean analyzeRebalance(final TreeMap<String/* clientId */, ConsumerRunningInfo> criTable) {
        return true;
    }

    public static String analyzeProcessQueue(final String clientId, ConsumerRunningInfo info) {
        StringBuilder sb = new StringBuilder();
        boolean push = false;
        {
            String property = info.getProperties().getProperty(ConsumerRunningInfo.PROP_CONSUME_TYPE);

            if (property == null) {
                property = ((ConsumeType) info.getProperties().get(ConsumerRunningInfo.PROP_CONSUME_TYPE)).name();
            }
            push = ConsumeType.valueOf(property) == ConsumeType.CONSUME_PASSIVELY;
        }

        boolean orderMsg = false;
        {
            String property = info.getProperties().getProperty(ConsumerRunningInfo.PROP_CONSUME_ORDERLY);
            orderMsg = Boolean.parseBoolean(property);
        }

        if (push) {
            Iterator<Entry<MessageQueue, ProcessQueueInfo>> it = info.getMqTable().entrySet().iterator();
            while (it.hasNext()) {
                Entry<MessageQueue, ProcessQueueInfo> next = it.next();
                MessageQueue mq = next.getKey();
                ProcessQueueInfo pq = next.getValue();

                if (orderMsg) {

                    if (!pq.isLocked()) {
                        sb.append(String.format("%s %s can't lock for a while, %dms%n",
                            clientId,
                            mq,
                            System.currentTimeMillis() - pq.getLastLockTimestamp()));
                    } else {
                        if (pq.isDroped() && pq.getTryUnlockTimes() > 0) {
                            sb.append(String.format("%s %s unlock %d times, still failed%n",
                                clientId,
                                mq,
                                pq.getTryUnlockTimes()));
                        }
                    }

                } else {
                    long diff = System.currentTimeMillis() - pq.getLastConsumeTimestamp();

                    if (diff > (1000 * 60) && pq.getCachedMsgCount() > 0) {
                        sb.append(String.format("%s %s can't consume for a while, maybe blocked, %dms%n",
                            clientId,
                            mq,
                            diff));
                    }
                }
            }
        }

        return sb.toString();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public TreeSet<SubscriptionData> getSubscriptionSet() {
        return subscriptionSet;
    }

    public void setSubscriptionSet(TreeSet<SubscriptionData> subscriptionSet) {
        this.subscriptionSet = subscriptionSet;
    }

    public TreeMap<MessageQueue, ProcessQueueInfo> getMqTable() {
        return mqTable;
    }

    public void setMqTable(TreeMap<MessageQueue, ProcessQueueInfo> mqTable) {
        this.mqTable = mqTable;
    }

    public TreeMap<String, ConsumeStatus> getStatusTable() {
        return statusTable;
    }

    public void setStatusTable(TreeMap<String, ConsumeStatus> statusTable) {
        this.statusTable = statusTable;
    }

    public TreeMap<String, String> getUserConsumerInfo() {
        return userConsumerInfo;
    }

    public String formatString() {
        StringBuilder sb = new StringBuilder();

        {
            sb.append("#Consumer Properties#\n");
            Iterator<Entry<Object, Object>> it = this.properties.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Object, Object> next = it.next();
                String item = String.format("%-40s: %s%n", next.getKey().toString(), next.getValue().toString());
                sb.append(item);
            }
        }

        {
            sb.append("\n\n#Consumer Subscription#\n");

            Iterator<SubscriptionData> it = this.subscriptionSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                SubscriptionData next = it.next();
                String item = String.format("%03d Topic: %-40s ClassFilter: %-8s SubExpression: %s%n",
                    ++i,
                    next.getTopic(),
                    next.isClassFilterMode(),
                    next.getSubString());

                sb.append(item);
            }
        }

        {
            sb.append("\n\n#Consumer Offset#\n");
            sb.append(String.format("%-64s  %-32s  %-4s  %-20s%n",
                "#Topic",
                "#Broker Name",
                "#QID",
                "#Consumer Offset"
            ));

            Iterator<Entry<MessageQueue, ProcessQueueInfo>> it = this.mqTable.entrySet().iterator();
            while (it.hasNext()) {
                Entry<MessageQueue, ProcessQueueInfo> next = it.next();
                String item = String.format("%-32s  %-32s  %-4d  %-20d%n",
                    next.getKey().getTopicName(),
                    next.getKey().getGroupName(),
                    next.getKey().getQueueId(),
                    next.getValue().getCommitOffset());

                sb.append(item);
            }
        }

        {
            sb.append("\n\n#Consumer MQ Detail#\n");
            sb.append(String.format("%-64s  %-32s  %-4s  %-20s%n",
                "#Topic",
                "#Broker Name",
                "#QID",
                "#ProcessQueueInfo"
            ));

            for (Entry<MessageQueue, ProcessQueueInfo> next : this.mqTable.entrySet()) {
                String item = String.format("%-64s  %-32s  %-4d  %s%n",
                    next.getKey().getTopicName(),
                    next.getKey().getGroupName(),
                    next.getKey().getQueueId(),
                    next.getValue().toString());

                sb.append(item);
            }
        }

        {
            sb.append("\n\n#Consumer Pop Detail#\n");
            sb.append(String.format("%-32s  %-32s  %-4s  %-20s%n",
                "#Topic",
                "#Broker Name",
                "#QID",
                "#ProcessQueueInfo"
            ));

            for (Entry<MessageQueue, PopProcessQueueInfo> next : this.mqPopTable.entrySet()) {
                String item = String.format("%-32s  %-32s  %-4d  %s%n",
                    next.getKey().getTopicName(),
                    next.getKey().getGroupName(),
                    next.getKey().getQueueId(),
                    next.getValue().toString());

                sb.append(item);
            }
        }

        {
            sb.append("\n\n#Consumer RT&TPS#\n");
            sb.append(String.format("%-64s  %14s %14s %14s %14s %18s %25s%n",
                "#Topic",
                "#Pull RT",
                "#Pull TPS",
                "#Consume RT",
                "#ConsumeOK TPS",
                "#ConsumeFailed TPS",
                "#ConsumeFailedMsgsInHour"
            ));

            for (Entry<String, ConsumeStatus> next : this.statusTable.entrySet()) {
                String item = String.format("%-32s  %14.2f %14.2f %14.2f %14.2f %18.2f %25d%n",
                    next.getKey(),
                    next.getValue().getPullRT(),
                    next.getValue().getPullTPS(),
                    next.getValue().getConsumeRT(),
                    next.getValue().getConsumeOKTPS(),
                    next.getValue().getConsumeFailedTPS(),
                    next.getValue().getConsumeFailedMsgs()
                );

                sb.append(item);
            }
        }

        if (this.userConsumerInfo != null) {
            sb.append("\n\n#User Consume Info#\n");
            for (Entry<String, String> next : this.userConsumerInfo.entrySet()) {
                String item = String.format("%-40s: %s%n", next.getKey(), next.getValue());
                sb.append(item);
            }
        }

        if (this.jstack != null) {
            sb.append("\n\n#Consumer jstack#\n");
            sb.append(this.jstack);
        }

        return sb.toString();
    }

    public String getJstack() {
        return jstack;
    }

    public void setJstack(String jstack) {
        this.jstack = jstack;
    }

    public TreeMap<MessageQueue, PopProcessQueueInfo> getMqPopTable() {
        return mqPopTable;
    }

    public void setMqPopTable(
        TreeMap<MessageQueue, PopProcessQueueInfo> mqPopTable) {
        this.mqPopTable = mqPopTable;
    }
}
