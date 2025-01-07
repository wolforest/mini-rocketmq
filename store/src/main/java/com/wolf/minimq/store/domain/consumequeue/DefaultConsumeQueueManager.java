package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.minimq.domain.config.ConsumeQueueConfig;
import com.wolf.minimq.domain.service.store.domain.CommitLogDispatcher;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueueStore;
import com.wolf.minimq.domain.service.store.domain.meta.TopicStore;
import com.wolf.minimq.domain.service.store.manager.ConsumeQueueManager;
import com.wolf.minimq.store.server.StoreContext;
import com.wolf.minimq.store.server.StorePath;

public class DefaultConsumeQueueManager implements ConsumeQueueManager {
    ConsumeQueueConfig consumeQueueConfig;
    private ConsumeQueueFlusher flusher;
    private ConsumeQueueLoader loader;

    @Override
    public void initialize() {
        consumeQueueConfig = StoreContext.getBean(ConsumeQueueConfig.class);
        consumeQueueConfig.setRootPath(StorePath.getConsumeQueuePath());

        flusher = new ConsumeQueueFlusher(StoreContext.getCheckPoint(), consumeQueueConfig);
        loader = new ConsumeQueueLoader(consumeQueueConfig);

        ConsumeQueueFactory consumeQueueFactory = initConsumeQueueFactory();
        ConsumeQueueStore consumeQueueStore = new DefaultConsumeQueueStore(consumeQueueFactory);
        StoreContext.register(consumeQueueStore, ConsumeQueueStore.class);

        registerDispatchHandler(consumeQueueStore);
        loader.load();
    }

    @Override
    public void start() {
        flusher.start();
    }

    @Override
    public void shutdown() {
        flusher.shutdown();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void registerDispatchHandler(ConsumeQueueStore consumeQueueStore) {
        CommitLogDispatcher dispatcher = StoreContext.getBean(CommitLogDispatcher.class);
        QueueCommitLogHandler handler = new QueueCommitLogHandler(consumeQueueStore);
        dispatcher.registerHandler(handler);
    }

    private ConsumeQueueFactory initConsumeQueueFactory() {
        TopicStore topicStore = StoreContext.getBean(TopicStore.class);
        ConsumeQueueFactory consumeQueueFactory = new ConsumeQueueFactory(consumeQueueConfig, topicStore, StoreContext.getCheckPoint());

        consumeQueueFactory.addCreateHook(flusher);
        consumeQueueFactory.addCreateHook(loader);

        consumeQueueFactory.createAll();
        return consumeQueueFactory;
    }
}