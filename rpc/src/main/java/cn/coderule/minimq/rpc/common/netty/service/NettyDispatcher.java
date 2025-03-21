package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.common.ds.Pair;
import cn.coderule.common.util.lang.ExceptionUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.exception.AbortProcessException;
import cn.coderule.minimq.rpc.common.RpcHook;
import cn.coderule.minimq.rpc.common.core.invoke.RequestTask;
import cn.coderule.minimq.rpc.common.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.protocol.code.ResponseCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
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
    private final ExecutorService callbackExecutor;

    public NettyDispatcher(ExecutorService callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
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
        } catch (Throwable throwable) {
            requestFailed(ctx, request, throwable);
        }
    }

    private void processResponse(RpcContext ctx, RpcCommand response) {
        int opaque = response.getOpaque();
        ResponseFuture future = responseMap.get(opaque);
        if (future == null) {
            log.warn("receive response, but not matched any request, opaque: {}; remoteAddr: {}",
                response.getOpaque(),
                NettyHelper.getRemoteAddr(ctx.channel())
            );
            return;
        }

        future.setResponse(response);
        responseMap.remove(response.getOpaque());

        if (null != future.getInvokeCallback()) {
            executeInvokeCallback(future);
            return;
        }

        future.putResponse(response);
        future.release();
    }

    /**
     * Execute callback in callback executor. If callback executor is null, run directly in current thread
     */
    private void executeInvokeCallback(final ResponseFuture responseFuture) {
        ExecutorService executor = this.callbackExecutor;
        boolean runInThisThread = executor == null || executor.isShutdown();

        if (!runInThisThread) {
            runInThisThread = submitInvokeCallback(responseFuture, executor);
        }

        if (runInThisThread) {
            callInvokeCallback(responseFuture);
        }
    }

    private boolean submitInvokeCallback(final ResponseFuture responseFuture, ExecutorService executor) {
        boolean runInThisThread = false;

        try {
            executor.submit(() -> {
                callInvokeCallback(responseFuture);
            });
        } catch (Exception e) {
            runInThisThread = true;
            log.warn("execute callback in executor exception, maybe executor busy", e);
        }

        return runInThisThread;
    }

    private void callInvokeCallback(final ResponseFuture responseFuture) {
        try {
            responseFuture.executeRpcCallback();
        } catch (Throwable e) {
            log.warn("executeInvokeCallback Exception", e);
        } finally {
            responseFuture.release();
        }
    }


    private void illegalRequestCode(RpcContext ctx, RpcCommand request) {
        String error = "illegal request code: " + request.getCode();
        RpcCommand response = RpcCommand.createResponseCommand(ResponseCode.REQUEST_CODE_NOT_SUPPORTED, error);
        response.setOpaque(request.getOpaque());

        writeResponse(ctx, request, response);
        log.error("{}, remoteAddr: {}", error, NettyHelper.getRemoteAddr(ctx.channel()));
    }

    private void rejectByServer(RpcContext ctx, RpcCommand request) {
        String error = "Server is shutting down, Request is rejected.";
        RpcCommand response = RpcCommand.createResponseCommand(ResponseCode.GO_AWAY, error);
        response.setOpaque(request.getOpaque());

        writeResponse(ctx, request, response);
    }

    private void rejectByBusiness(RpcContext ctx, RpcCommand request) {
        String error = "[REJECTREQUEST]system busy, start flow control for a while";
        RpcCommand response = RpcCommand.createResponseCommand(ResponseCode.SYSTEM_BUSY, error);
        response.setOpaque(request.getOpaque());

        writeResponse(ctx, request, response);
    }

    private void flowControl(RpcContext ctx, RpcCommand request) {
        if (System.currentTimeMillis() % 1000 == 0) {
            log.warn("[OVERLOAD]system busy, get RejectedExecutionException, request code: {}, remote addr: {}",
                request.getCode(),
                NettyHelper.getRemoteAddr(ctx.channel())
            );
        }

        String error = "[OVERLOAD]system busy, start flow control for a while";
        RpcCommand response = RpcCommand.createResponseCommand(ResponseCode.SYSTEM_BUSY, error);
        response.setOpaque(request.getOpaque());

        writeResponse(ctx, request, response);

    }

    private void writeResponse(RpcContext ctx, RpcCommand request, RpcCommand response) {
        NettyHelper.writeResponse(ctx.channel(), request, response);
    }

    private void requestFailed(RpcContext ctx, RpcCommand request, Throwable t) {
        log.error("request failed. request code: {}", request.getCode(), t);
    }

    private void abortProcess(RpcContext ctx, RpcCommand request, AbortProcessException e) {
        RpcCommand response = RpcCommand.createResponseCommand((int)e.getCode(), e.getMessage());
        response.setOpaque(request.getOpaque());
        writeResponse(ctx, request, response);
    }

    private void processFailed(RpcContext ctx, RpcCommand request, Throwable t) {
        log.error("process request failed, request: {}", request, t);

        if (request.isOnewayRPC()) {
            return;
        }

        RpcCommand response = RpcCommand.createResponseCommand(ResponseCode.SYSTEM_ERROR, ExceptionUtil.getName(t));
        response.setOpaque(request.getOpaque());
        writeResponse(ctx, request, response);
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
            } catch (Throwable t) {
                processFailed(ctx, request, t);
            }
        };
    }

    private RequestTask createRequestTask(RpcContext ctx, RpcCommand command, RpcProcessor processor) {
        Runnable task = createProcessTask(ctx, command, processor);
        return new RequestTask(task, ctx.channel(), command);
    }

}
