
package cn.coderule.minimq.broker.server.grpc.interceptor;

import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueResponse;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.SendMessageRequest;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private final static Map<String, Integer> REQUEST_MAP = new HashMap<String, Integer>() {
        {
            // v2
            put(QueryRouteRequest.getDescriptor().getFullName(), RequestCode.GET_ROUTEINFO_BY_TOPIC);
            put(HeartbeatRequest.getDescriptor().getFullName(), RequestCode.HEART_BEAT);
            put(SendMessageRequest.getDescriptor().getFullName(), RequestCode.SEND_MESSAGE_V2);
            put(QueryAssignmentRequest.getDescriptor().getFullName(), RequestCode.GET_ROUTEINFO_BY_TOPIC);
            put(ReceiveMessageRequest.getDescriptor().getFullName(), RequestCode.PULL_MESSAGE);
            put(AckMessageRequest.getDescriptor().getFullName(), RequestCode.UPDATE_CONSUMER_OFFSET);
            put(ForwardMessageToDeadLetterQueueResponse.getDescriptor().getFullName(), RequestCode.CONSUMER_SEND_MSG_BACK);
            put(EndTransactionRequest.getDescriptor().getFullName(), RequestCode.END_TRANSACTION);
            put(NotifyClientTerminationRequest.getDescriptor().getFullName(), RequestCode.UNREGISTER_CLIENT);
            put(ChangeInvisibleDurationRequest.getDescriptor().getFullName(), RequestCode.CONSUMER_SEND_MSG_BACK);
        }
    };

    public static int map(String rpcFullName) {
        if (REQUEST_MAP.containsKey(rpcFullName)) {
            return REQUEST_MAP.get(rpcFullName);
        }
        return RequestCode.HEART_BEAT;
    }
}
