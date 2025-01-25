package com.wolf.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import com.wolf.minimq.broker.api.ConsumerController;
import com.wolf.minimq.broker.api.ProducerController;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.Setter;

public class RouteActivity {
    private final ThreadPoolExecutor executor;

    @Setter
    private ProducerController producerController;
    @Setter
    private ConsumerController consumerController;

    public RouteActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void getRoute(QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {
    }

    public void getAssignment(QueryAssignmentRequest request, StreamObserver<QueryAssignmentResponse> responseObserver) {
    }
}
