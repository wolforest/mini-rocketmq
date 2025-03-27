package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.manager.MetaManager;
import cn.coderule.minimq.store.api.TopicStoreImpl;
import cn.coderule.minimq.store.server.StoreContext;
import cn.coderule.minimq.store.server.bootstrap.StorePath;
import cn.coderule.minimq.store.server.bootstrap.StoreRegister;

public class DefaultMetaManager implements MetaManager {
    @Override
    public void initialize() {
        initTopic();
    }



    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }



    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void initTopic() {
        ConsumeOffsetService offsetService = StoreContext.getBean(ConsumeOffsetService.class);
        StoreRegister storeRegister = StoreContext.getBean(StoreRegister.class);
        TopicService topicStore = new DefaultTopicService(StorePath.getTopicPath(), offsetService, storeRegister);
        topicStore.load();

        TopicStore topicService = new TopicStoreImpl(topicStore);
        StoreContext.registerAPI(topicService, TopicStore.class);
    }

    private void initConsumerOffset() {
        ConsumeOffsetService consumeOffsetStore = new DefaultConsumeOffsetService(StorePath.getConsumerOffsetPath());
    }
}
