package cn.coderule.minimq.broker.server.rpc.processor;

import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;

public class ConsumerProcessor implements RpcProcessor {
    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
    }

    @Override
    public boolean reject() {
        return false;
    }
}
