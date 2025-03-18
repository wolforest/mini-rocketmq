package cn.coderule.minimq.domain.config;

import cn.coderule.minimq.domain.constant.MQConstants;
import java.io.File;
import java.io.Serializable;
import lombok.Data;

@Data
public class RegistryConfig implements Serializable {
    private String homePath = System.getProperty(MQConstants.ROCKETMQ_HOME_PROPERTY, System.getenv(MQConstants.ROCKETMQ_HOME_ENV));
    private String kvPath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "kvConfig.json";
    private String configPath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "namesrv.properties";

    private String host = "0.0.0.0";
    private Integer port = 9876;



    private int bossThreadNum = 1;
    private int workerThreadNum = 3;
    private int businessThreadNum = 8;
    private int callbackThreadNum = 0;

    private int routeThreadNum = 8;
    private int processThreadNum = 16;

    private int unregisterQueueCapacity = 3000;

    /**
     * If enable this flag, the topics that don't exist in broker registration payload will be deleted from name server.
     *
     * WARNING:
     * 1. Enable this flag and "enableSingleTopicRegister" of broker config meanwhile to avoid losing topic route info unexpectedly.
     * 2. This flag does not support static topic currently.
     */
    private boolean deleteTopicWhileRegistration = false;
    private boolean notifyMinIdChanged = false;
    private long idleScanInterval = 5 * 1000;
}
