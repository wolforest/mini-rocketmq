package cn.coderule.minimq.broker.server.grpc;

import cn.coderule.minimq.broker.server.grpc.service.message.MessageManager;
import cn.coderule.minimq.broker.server.grpc.service.message.MessageService;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.server.GrpcConfig;

public class GrpcManager implements Lifecycle {
    private GrpcConfig grpcConfig;
    private MessageManager messageManager;
    private MessageService messageService;
    private GrpcServer grpcServer;

    @Override
    public void initialize() {
        initConfig();
        initMessageService();
        initGrpcServer();
    }

    @Override
    public void start() {
        this.messageManager.start();
        this.grpcServer.start();
    }

    @Override
    public void shutdown() {
        this.messageManager.shutdown();
        this.grpcServer.shutdown();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void initConfig() {
        this.grpcConfig = BrokerContext.getBean(GrpcConfig.class);
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        this.grpcConfig.setPort(brokerConfig.getPort());
    }

    private void initMessageService() {
        this.messageManager = new MessageManager(grpcConfig);
        messageManager.initialize();
        this.messageService = this.messageManager.getMessageService();
    }

    private void initGrpcServer() {
        this.grpcServer = new GrpcServer(grpcConfig, messageService);
        this.grpcServer.initialize();
    }
}
