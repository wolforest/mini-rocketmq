package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.common.ds.Pair;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.exception.AbortProcessException;
import cn.coderule.minimq.rpc.common.RpcHook;
import cn.coderule.minimq.rpc.common.core.invoke.RequestTask;
import cn.coderule.minimq.rpc.common.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyDispatcher {

    /**
     * response map
     * { opaque : ResponseFuture }
     */
    private final ConcurrentMap<Integer, ResponseFuture> responseMap = new ConcurrentHashMap<>(256);
    /**
     * processor map
     * { requestCode: [RpcProcessor, ExecutorService] }
     */
    private final HashMap<Integer, Pair<RpcProcessor, ExecutorService>> processorMap = new HashMap<>(64);
    private final List<RpcHook> rpcHooks = new ArrayList<>();
    private final AtomicBoolean stopping = new AtomicBoolean(false);

    private Pair<RpcProcessor, ExecutorService> defaultProcessor;

    public NettyDispatcher() {
    }

    public void start() {
        this.stopping.set(false);
    }

    public void shutdown() {
        this.stopping.set(true);
    }

    public void dispatch(RpcContext ctx, RpcCommand command) {
        if (command == null) {
            return;
        }

        switch (command.getType()) {
            case REQUEST_COMMAND:
                processRequest(ctx, command);
                break;
            case RESPONSE_COMMAND:
                processResponse(ctx, command);
                break;
            default:
                break;
        }
    }

    public void registerProcessor(Collection<Integer> codes, @NonNull RpcProcessor processor, @NonNull ExecutorService executor) {
        if (CollectionUtil.isEmpty(codes)) {
            return;
        }


        Pair<RpcProcessor, ExecutorService> pair = Pair.of(processor, executor);
        for (Integer code : codes) {
            processorMap.put(code, pair);
        }
    }

    public void registerProcessor(int requestCode, @NonNull RpcProcessor processor, @NonNull ExecutorService executor) {
        Pair<RpcProcessor, ExecutorService> pair = Pair.of(processor, executor);
        processorMap.put(requestCode, pair);
    }

    public void registerDefaultProcessor(RpcProcessor processor, ExecutorService executor) {
        this.defaultProcessor = Pair.of(processor, executor);
    }

    public void registerRpcHook(RpcHook rpcHook) {
        if (rpcHook != null && !rpcHooks.contains(rpcHook)) {
            rpcHooks.add(rpcHook);
        }
    }

    public void clearRpcHook() {
        rpcHooks.clear();
    }

    public void invokePreHooks(RpcContext ctx, RpcCommand request) {
        if (rpcHooks.isEmpty()) {
            return;
        }

        for (RpcHook rpcHook : rpcHooks) {
            rpcHook.onRequestStart(ctx, request);
        }

    }

    public void invokePostHooks(RpcContext ctx, RpcCommand request, RpcCommand response) {
        if (rpcHooks.isEmpty()) {
            return;
        }

        for (RpcHook rpcHook : rpcHooks) {
            rpcHook.onResponseComplete(ctx, request, response);
        }

    }

    private void processRequest(RpcContext ctx, RpcCommand request) {
        if (stopping.get()) {
            rejectByServer(ctx, request);
            return;
        }

        Pair<RpcProcessor, ExecutorService> processor = processorMap.get(request.getCode());
        if (processor == null && defaultProcessor != null) {
            processor = defaultProcessor;
        }

        if (processor == null) {
            illegalRequestCode(ctx, request);
            return;
        }

        if (processor.getLeft().reject()) {
            rejectByBusiness(ctx, request);
            return;
        }

        try {
            RequestTask task = createRequestTask(ctx, request, processor.getLeft());
            processor.getRight().submit(task);
        } catch (RejectedExecutionException e) {
            flowControl(ctx, request);
        } catch (Throwable t) {
            requestFailed(ctx, request, t);
        }
    }

    private void processResponse(RpcContext ctx, RpcCommand command) {

    }

    private void illegalRequestCode(RpcContext ctx, RpcCommand command) {

    }

    private void rejectByServer(RpcContext ctx, RpcCommand command) {

    }

    private void rejectByBusiness(RpcContext ctx, RpcCommand command) {

    }

    private void flowControl(RpcContext ctx, RpcCommand command) {

    }

    private void writeResponse(RpcContext ctx, RpcCommand request, RpcCommand response) {

    }

    private void requestFailed(RpcContext ctx, RpcCommand command, Throwable t) {

    }

    private void abortProcess(RpcContext ctx, RpcCommand command, Throwable t) {

    }

    private void processFailed(RpcContext ctx, RpcCommand command, Throwable t) {

    }

    private Runnable createProcessTask(RpcContext ctx, RpcCommand request, RpcProcessor processor) {
        return () -> {
            try {
                invokePreHooks(ctx, request);
                RpcCommand response = processor.process(ctx, request);
                invokePostHooks(ctx, request, response);

                writeResponse(ctx, request, response);
            } catch (AbortProcessException e) {
                abortProcess(ctx, request, e);
            } catch (Throwable e) {
                processFailed(ctx, request, e);
            }
        };
    }

    private RequestTask createRequestTask(RpcContext ctx, RpcCommand command, RpcProcessor processor) {
        Runnable task = createProcessTask(ctx, command, processor);
        return new RequestTask(task, ctx.channel(), command);
    }

}
