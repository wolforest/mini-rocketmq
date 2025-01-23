package com.wolf.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueRequest;
import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueResponse;
import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SendMessageResponse;
import apache.rocketmq.v2.Status;
import com.wolf.minimq.broker.api.ProducerController;
import com.wolf.minimq.broker.server.vo.RequestContext;
import com.wolf.minimq.domain.model.bo.MessageBO;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import lombok.Setter;

public class ProducerActivity {
    private ThreadPoolExecutor executor;
    /**
     * inject by GrpcManager , while starting
     *  All controllers will be registered in BrokerContext
     *      after related component initialized
     *  GrpcManager will get controllers in BrokerContext
     */
    @Setter
    private ProducerController producerController;

    public ProducerActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void produce(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        Function<Status, SendMessageResponse> statusToResponse = statusToResponse();
        Runnable task = getTask(context, request, responseObserver);

        try {
            ActivityHelper.submit(context, request, responseObserver, executor, task, statusToResponse);
        } catch (Throwable t) {
            ActivityHelper.writeResponse(context, request, null, executor, t, responseObserver, statusToResponse);
        }
    }

    public void moveToDeadLetterQueue(ForwardMessageToDeadLetterQueueRequest request, StreamObserver<ForwardMessageToDeadLetterQueueResponse> responseObserver) {

    }

    private Runnable getTask(RequestContext context, SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        return () -> {
            MessageBO messageBO = new MessageBO();
            producerController.produce(context, messageBO)
                .whenComplete((response, throwable) -> {
                    ActivityHelper.writeResponse(context, request, response, executor, throwable, responseObserver, statusToResponse());
                });
        };
    }

    private Function<Status, SendMessageResponse> statusToResponse() {
        return status -> SendMessageResponse.newBuilder()
            .setStatus(status)
            .build();
    }

}
