package cn.coderule.minimq.registry.server;

public class ContextInitializer {
    private final String[] args;

    public static void init(String[] args) {
        ContextInitializer initializer = new ContextInitializer(args);
        initializer.initialize();
    }

    public ContextInitializer(String[] args) {
        this.args = args;
    }

    public void initialize() {
    }

}
