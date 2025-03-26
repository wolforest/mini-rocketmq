package cn.coderule.minimq.store.server.rpc.processor;

import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;

public class GroupProcessor implements RpcProcessor {
    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return switch (request.getCode()) {
            case RequestCode.GET_ALL_TOPIC_CONFIG -> this.getTopic(ctx, request);
            case RequestCode.DELETE_TOPIC_IN_BROKER -> this.deleteTopic(ctx, request);
            case RequestCode.UPDATE_AND_CREATE_TOPIC -> this.saveTopic(ctx, request);
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand saveTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        return response.success();
    }

    private RpcCommand getTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        return response.success();
    }

    private RpcCommand deleteTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        return response.success();
    }
}
