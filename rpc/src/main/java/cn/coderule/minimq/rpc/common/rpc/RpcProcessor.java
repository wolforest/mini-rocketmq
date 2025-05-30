package cn.coderule.minimq.rpc.common.rpc;

import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.SystemResponseCode;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public interface RpcProcessor {
    RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException;
    boolean reject();

    default Collection<Integer> getCodeSet() {
        return Set.of();
    }

    default ExecutorService getExecutor() {
        return null;
    }

    default RpcCommand unsupportedCode(RpcContext ctx, RpcCommand request) {
        String error = " request type " + request.getCode() + " not supported";
        return RpcCommand.createResponseCommand(SystemResponseCode.REQUEST_CODE_NOT_SUPPORTED, error);
    }

}
