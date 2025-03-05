package cn.coderule.minimq.rpc.common.netty;

import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.common.core.RpcCallback;
import cn.coderule.minimq.rpc.common.core.RpcCommand;
import cn.coderule.minimq.rpc.common.core.RpcHook;
import cn.coderule.minimq.rpc.common.core.RpcListener;
import cn.coderule.minimq.rpc.common.core.RpcPipeline;
import cn.coderule.minimq.rpc.common.core.RpcProcessor;
import cn.coderule.minimq.rpc.config.RpcClientConfig;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class NettyClient extends NettyService implements RpcClient {
    private final RpcClientConfig config;

    public NettyClient(RpcClientConfig config) {
        super(config.getOnewaySemaphorePermits(), config.getAsyncSemaphorePermits());
        this.config = config;
    }

    @Override
    public RpcListener getRpcListener() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void registerRpcHook(RpcHook rpcHook) {

    }

    @Override
    public void clearRpcHook() {

    }

    @Override
    public void setRpcPipeline(RpcPipeline pipeline) {

    }

    @Override
    public boolean isChannelWritable(String addr) {
        return false;
    }

    @Override
    public boolean isAddressReachable(String addr) {
        return false;
    }

    @Override
    public void closeChannels(List<String> addrList) {

    }

    @Override
    public void registerProcessor(int requestCode, RpcProcessor processor, ExecutorService executor) {

    }

    @Override
    public RpcCommand invokeSync(String addr, RpcCommand request,
        long timeoutMillis) throws Exception {
        return null;
    }

    @Override
    public void invokeAsync(String addr, RpcCommand request, long timeoutMillis,
        RpcCallback invokeCallback) throws Exception {

    }

    @Override
    public void invokeOneway(String addr, RpcCommand request, long timeoutMillis) throws Exception {

    }
}
