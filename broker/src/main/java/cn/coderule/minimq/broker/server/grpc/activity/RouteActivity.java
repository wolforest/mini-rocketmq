package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.activity.ActivityHelper;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import lombok.Setter;

public class RouteActivity {
    private final ThreadPoolExecutor executor;

    @Setter
    private RouteController routeController;

    public RouteActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void getRoute(RequestContext context, QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {
        ActivityHelper<QueryRouteRequest, QueryRouteResponse> helper = getRouteHelper(context, request, responseObserver);

        try {
            Runnable task = () -> getRouteAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public void getAssignment(RequestContext context, QueryAssignmentRequest request, StreamObserver<QueryAssignmentResponse> responseObserver) {
        ActivityHelper<QueryAssignmentRequest, QueryAssignmentResponse> helper = getAssignmentHelper(context, request, responseObserver);

        try {
            Runnable task = () -> getAssignmentAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    private ActivityHelper<QueryAssignmentRequest, QueryAssignmentResponse> getAssignmentHelper(
        RequestContext context,
        QueryAssignmentRequest request,
        StreamObserver<QueryAssignmentResponse> responseObserver
    ) {
        Function<Status, QueryAssignmentResponse> statusToResponse = assignmentStatueToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private ActivityHelper<QueryRouteRequest, QueryRouteResponse> getRouteHelper(
        RequestContext context,
        QueryRouteRequest request,
        StreamObserver<QueryRouteResponse> responseObserver
    ) {
        Function<Status, QueryRouteResponse> statusToResponse = routeStatueToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private CompletableFuture<QueryRouteResponse> getAssignmentAsync(RequestContext context, QueryAssignmentRequest request) {
        //routeController.getRoute(context, request.getTopic().getName());
        return CompletableFuture.completedFuture(null);
    }


    private CompletableFuture<QueryRouteResponse> getRouteAsync(RequestContext context, QueryRouteRequest request) {
        //routeController.getRoute(context, request.getTopic().getName());
        return CompletableFuture.completedFuture(null);
    }

    private Function<Status, QueryRouteResponse> routeStatueToResponse() {
        return status -> QueryRouteResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    private Function<Status, QueryAssignmentResponse> assignmentStatueToResponse() {
        return status -> QueryAssignmentResponse.newBuilder()
            .setStatus(status)
            .build();
    }
}
