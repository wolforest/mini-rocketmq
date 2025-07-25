package cn.coderule.minimq.registry.processor;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.config.server.RegistryConfig;
import cn.coderule.minimq.registry.domain.store.service.TopicService;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.SystemResponseCode;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicList;
import cn.coderule.minimq.rpc.registry.protocol.header.DeleteTopicFromNamesrvRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.GetTopicsByClusterRequestHeader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicProcessor implements RpcProcessor {
    private final RegistryConfig config;
    private final TopicService topicService;

    @Getter
    private final ExecutorService executor;

    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.GET_ALL_TOPIC_LIST_FROM_NAMESERVER,
        RequestCode.DELETE_TOPIC_IN_NAMESRV,
        RequestCode.GET_TOPICS_BY_CLUSTER,
        RequestCode.GET_SYSTEM_TOPIC_LIST_FROM_NS,
        RequestCode.GET_UNIT_TOPIC_LIST,
        RequestCode.GET_HAS_UNIT_SUB_TOPIC_LIST,
        RequestCode.GET_HAS_UNIT_SUB_UNUNIT_TOPIC_LIST
    );

    public TopicProcessor(RegistryConfig config, TopicService topicService, ExecutorService executor) {
        this.config = config;
        this.topicService = topicService;
        this.executor = executor;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return switch (request.getCode()) {
            case RequestCode.GET_ALL_TOPIC_LIST_FROM_NAMESERVER -> this.getTopicList(ctx, request);
            case RequestCode.DELETE_TOPIC_IN_NAMESRV -> this.deleteTopic(ctx, request);
            case RequestCode.GET_TOPICS_BY_CLUSTER -> this.getTopicByCluster(ctx, request);
            case RequestCode.GET_SYSTEM_TOPIC_LIST_FROM_NS -> this.getSystemTopicList(ctx, request);
            case RequestCode.GET_UNIT_TOPIC_LIST -> this.getUnitTopic(ctx, request);
            case RequestCode.GET_HAS_UNIT_SUB_TOPIC_LIST -> this.getSubUnitTopic(ctx, request);
            case RequestCode.GET_HAS_UNIT_SUB_UNUNIT_TOPIC_LIST -> this.getSubAndNoUnitTopic(ctx, request);
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand getTopicList(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        if (!config.isReturnAllTopic()) {
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "returnAllTopic is false");
        }

        TopicList topicList = topicService.getTopicList();
        response.setBody(topicList.encode());

        return response.success();
    }

    private RpcCommand getTopicByCluster(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        if (!config.isReturnTopicByCluster()) {
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "returnTopicByCluster is false");
        }

        GetTopicsByClusterRequestHeader requestHeader = request.decodeHeader(GetTopicsByClusterRequestHeader.class);
        TopicList topicList = topicService.getTopicByCluster(requestHeader.getCluster());
        response.setBody(topicList.encode());

        return response.success();
    }

    private RpcCommand getSystemTopicList(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        TopicList topicList = topicService.getSystemTopicList();
        response.setBody(topicList.encode());

        return response.success();

    }

    private RpcCommand getUnitTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        if (!config.isReturnAllTopic()) {
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "returnAllTopic is false");
        }

        TopicList topicList = topicService.getUnitTopic();
        response.setBody(topicList.encode());

        return response.success();

    }

    private RpcCommand getSubUnitTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        if (!config.isReturnAllTopic()) {
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "returnAllTopic is false");
        }

        TopicList topicList = topicService.getSubUnitTopic();
        response.setBody(topicList.encode());

        return response.success();

    }

    private RpcCommand getSubAndNoUnitTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        if (!config.isReturnAllTopic()) {
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "returnAllTopic is false");
        }

        TopicList topicList = topicService.getSubAndNoUnitTopic();
        response.setBody(topicList.encode());

        return response.success();

    }

    private RpcCommand deleteTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        DeleteTopicFromNamesrvRequestHeader requestHeader = request.decodeHeader(DeleteTopicFromNamesrvRequestHeader.class);

        if (StringUtil.notBlank(requestHeader.getClusterName())) {
            topicService.deleteTopic(requestHeader.getClusterName(), requestHeader.getTopic());
        } else {
            topicService.deleteTopic(requestHeader.getTopic());
        }

        return response.success();
    }

}
