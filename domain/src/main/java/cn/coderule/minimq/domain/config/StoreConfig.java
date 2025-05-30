package cn.coderule.minimq.domain.config;

import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.domain.constant.PermName;
import java.io.File;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StoreConfig extends ServerIdentity {
    private String host = NetworkUtil.getLocalAddress();
    private int port = 6888;
    private int haPort = 10912;

    private boolean enableMasterElection = false;

    /**
     * broker permission
     * default: Readable and writable
     */
    private int permission = PermName.PERM_READ | PermName.PERM_WRITE;

    private boolean refreshMasterAddress = false;
    private boolean refreshHaAddress = false;

    private String masterAddress;
    private String haAddress;
    private String registryAddress = "127.0.0.1:9876";
    // private String registryAddress = System.getProperty(RegistryUtils.NAMESRV_ADDR_PROPERTY, System.getenv(RegistryUtils.NAMESRV_ADDR_ENV));

    private boolean fetchRegistryAddressByDns = false;
    private boolean fetchRegistryAddressByHttp = false;
    private boolean enableRegistryHeartbeat = false;
    private int fetchRegistryAddressInterval = 60 * 1000;
    private int registryTimeout = 24_000;
    private int registryHeartbeatInterval = 1_000;
    private int registryHeartbeatTimeout = 1_000;


    private int bossThreadNum = 1;
    private int workerThreadNum = 3;
    private int businessThreadNum = 8;
    private int callbackThreadNum = 0;

    private int enqueueThreadNum = Math.min(4, SystemUtil.getProcessorNumber());
    private int enqueueQueueCapacity = 10000;
    private int pullThreadNum = SystemUtil.getProcessorNumber() * 2;
    private int pullQueueCapacity = 10000;
    private int adminThreadNum = Math.min(4, SystemUtil.getProcessorNumber());
    private int adminQueueCapacity = 10000;

    private int syncFlushTimeout = 5 * 1000;
    private String rootDir = System.getProperty("user.home") + File.separator + "mq";

    private int schedulerPoolSize = 1;
    private int schedulerShutdownTimeout = 3;

    private boolean enableTransientPool = false;
    private int transientPoolSize = 5;
    private int transientFileSize = 100 * 1024 * 1024;
    private boolean fastFailIfNotExistInTransientPool = true;

    private MessageConfig messageConfig;
    private TopicConfig topicConfig;
    private TimerConfig timerConfig;
}
