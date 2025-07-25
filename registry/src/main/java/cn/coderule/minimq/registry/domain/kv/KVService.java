package cn.coderule.minimq.registry.domain.kv;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.minimq.domain.config.server.RegistryConfig;
import cn.coderule.minimq.rpc.registry.protocol.body.KVTable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class KVService {
    public static final String NAMESPACE_ORDER_TOPIC_CONFIG = "ORDER_TOPIC_CONFIG";

    private final RegistryConfig config;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    // namespace -> key -> value
    private final HashMap<String, HashMap<String, String>> configTable = new HashMap<>();

    public KVService(RegistryConfig config) {
        this.config = config;
    }

    public void load() {
        String content = null;
        try {
            content = FileUtil.fileToString(this.config.getKvPath());
        } catch (Exception e) {
            log.warn("Load KV config table exception", e);
        }

        if (content == null) {
            return;
        }

        KVWrapper kvWrapper = KVWrapper.fromJson(content, KVWrapper.class);
        if (null != kvWrapper) {
            this.configTable.putAll(kvWrapper.getConfigTable());
            log.info("load KV config table OK");
        }
    }

    public void putKVConfig(final String namespace, final String key, final String value) {
        try {
            this.lock.writeLock().lockInterruptibly();

            try {
                HashMap<String, String> kvTable = this.configTable.get(namespace);
                if (null == kvTable) {
                    kvTable = new HashMap<>();
                    this.configTable.put(namespace, kvTable);
                    log.info("putKVConfig create new Namespace {}", namespace);
                }

                final String prev = kvTable.put(key, value);
                if (null != prev) {
                    log.info("putKVConfig update config item, Namespace: {} Key: {} Value: {}",
                        namespace, key, value);
                } else {
                    log.info("putKVConfig create new config item, Namespace: {} Key: {} Value: {}",
                        namespace, key, value);
                }
            } finally {
                this.lock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("putKVConfig InterruptedException", e);
        }

        this.persist();
    }

    public void persist() {
        try {
            this.lock.readLock().lockInterruptibly();

            try {
                KVWrapper kvWrapper = new KVWrapper();
                kvWrapper.setConfigTable(this.configTable);

                String content = kvWrapper.toJson();

                if (null != content) {
                    FileUtil.stringToFile(content, this.config.getKvPath());
                }
            } catch (Exception e) {
                log.error("persist kv Exception, {}", this.config.getKvPath(), e);
            } finally {
                this.lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("persist InterruptedException", e);
        }
    }

    public void deleteKVConfig(final String namespace, final String key) {
        try {
            this.lock.writeLock().lockInterruptibly();
            try {
                HashMap<String, String> kvTable = this.configTable.get(namespace);
                if (null != kvTable) {
                    String value = kvTable.remove(key);
                    log.info("deleteKVConfig delete a config item, Namespace: {} Key: {} Value: {}",
                        namespace, key, value);
                }
            } finally {
                this.lock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("deleteKVConfig InterruptedException", e);
        }

        this.persist();
    }

    public byte[] getKVListByNamespace(final String namespace) {
        try {
            this.lock.readLock().lockInterruptibly();

            try {
                HashMap<String, String> kvTable = this.configTable.get(namespace);
                if (null == kvTable) {
                    return null;
                }

                KVTable table = new KVTable();
                table.setTable(kvTable);
                return table.encode();
            } finally {
                this.lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("getKVListByNamespace InterruptedException", e);
        }

        return null;
    }

    public String getKVConfig(final String namespace, final String key) {
        try {
            this.lock.readLock().lockInterruptibly();
            try {
                HashMap<String, String> kvTable = this.configTable.get(namespace);
                if (null != kvTable) {
                    return kvTable.get(key);
                }
            } finally {
                this.lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("getKVConfig InterruptedException", e);
        }

        return null;
    }

    public void logStatus() {
        try {
            this.lock.readLock().lockInterruptibly();
            try {
                log.info("--------------------------------------------------------");

                log.info("configTable SIZE: {}", this.configTable.size());
                for (Entry<String, HashMap<String, String>> next : this.configTable.entrySet()) {
                    for (Entry<String, String> nextSub : next.getValue().entrySet()) {
                        log.info("configTable NS: {} Key: {} Value: {}",
                            next.getKey(), nextSub.getKey(), nextSub.getValue());
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("printAllPeriodically InterruptedException", e);
        }
    }
}
