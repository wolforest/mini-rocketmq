package com.wolf.minimq.store;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.convention.service.LifecycleManager;
import com.wolf.minimq.domain.utils.lock.ApplicationLock;
import com.wolf.minimq.domain.utils.lock.PIDLock;
import com.wolf.minimq.store.server.APIRegister;
import com.wolf.minimq.store.server.ContextInitializer;
import com.wolf.minimq.store.server.ComponentRegister;
import com.wolf.minimq.store.server.StoreArgument;
import com.wolf.minimq.store.server.StoreCheckpoint;
import com.wolf.minimq.store.server.StoreContext;
import com.wolf.minimq.store.server.StorePath;
import lombok.NonNull;

/**
 * gateway of store module
 *  - start()
 *
 *  input:
 *   - StoreConfig
 *   - monitor context
 *  output:
 *   - self
 *   - API context
 */
public class Store implements Lifecycle {
    private final StoreArgument argument;

    private State state = State.INITIALIZING;
    private LifecycleManager componentManager;

    private ApplicationLock applicationLock;
    private PIDLock pidLock;

    public Store(@NonNull StoreArgument argument) {
        this.argument = argument;
    }

    @Override
    public void initialize() {
        this.argument.validate();
        ContextInitializer.init(argument);

        this.componentManager = ComponentRegister.register();
        APIRegister.register();

        applicationLock = new ApplicationLock(StorePath.getLockFile());
        pidLock = new PIDLock(StorePath.getLockFile());

        boolean lastExitOk = !pidLock.isLocked();
        StoreCheckpoint checkpoint = new StoreCheckpoint(StorePath.getCheckpointPath());
        checkpoint.setNormalExit(lastExitOk);
        StoreContext.CHECK_POINT = checkpoint;

        this.componentManager.initialize();
    }

    @Override
    public void cleanup() {
        this.componentManager.cleanup();
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public void start() {
        this.state = State.STARTING;
        applicationLock.lock();
        this.componentManager.start();
        pidLock.lock();
        this.state = State.RUNNING;
    }

    @Override
    public void shutdown() {
        this.state = State.SHUTTING_DOWN;
        this.componentManager.shutdown();
        applicationLock.unlock();
        pidLock.unlock();
        this.state = State.TERMINATED;
    }
}
