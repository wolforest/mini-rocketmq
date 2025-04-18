package cn.coderule.minimq.registry.processor;

import cn.coderule.minimq.registry.domain.kv.KVService;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.SystemResponseCode;
import cn.coderule.minimq.rpc.registry.protocol.header.DeleteKVConfigRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.GetKVConfigRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.GetKVConfigResponseHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.PutKVConfigRequestHeader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KVProcessor implements RpcProcessor {
    private final KVService kvService;

    @Getter
    private final ExecutorService executor;
    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.PUT_KV_CONFIG,
        RequestCode.GET_KV_CONFIG,
        RequestCode.DELETE_KV_CONFIG
    );

    public KVProcessor(KVService kvService, ExecutorService executor) {
        this.kvService = kvService;
        this.executor = executor;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return switch (request.getCode()) {
            case RequestCode.PUT_KV_CONFIG -> this.putKVConfig(ctx, request);
            case RequestCode.GET_KV_CONFIG -> this.getKVConfig(ctx, request);
            case RequestCode.DELETE_KV_CONFIG -> this.deleteKVConfig(ctx, request);
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand putKVConfig(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        PutKVConfigRequestHeader requestHeader = request.decodeHeader(PutKVConfigRequestHeader.class);

        if (null == requestHeader.getNamespace() || null == requestHeader.getKey()) {
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "namespace or key is null");
        }

        kvService.putKVConfig(requestHeader.getNamespace(), requestHeader.getKey(), requestHeader.getValue());

        return response.success();
    }

    private RpcCommand getKVConfig(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(GetKVConfigResponseHeader.class);
        GetKVConfigResponseHeader responseHeader = (GetKVConfigResponseHeader) response.readCustomHeader();
        GetKVConfigRequestHeader requestHeader = request.decodeHeader(GetKVConfigRequestHeader.class);

        String value = kvService.getKVConfig(requestHeader.getNamespace(), requestHeader.getKey());
        if (value == null) {
            return response.setCodeAndRemark(
                ResponseCode.QUERY_NOT_FOUND,
                "No config found, namespace: " + requestHeader.getNamespace() + ", key: " + requestHeader.getKey()
            );
        }

        responseHeader.setValue(value);
        return response.success();
    }

    private RpcCommand deleteKVConfig(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        DeleteKVConfigRequestHeader requestHeader = request.decodeHeader(DeleteKVConfigRequestHeader.class);

        kvService.deleteKVConfig(requestHeader.getNamespace(), requestHeader.getKey());

        return response.success();
    }

}
