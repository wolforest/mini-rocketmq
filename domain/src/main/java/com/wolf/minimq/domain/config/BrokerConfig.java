package com.wolf.minimq.domain.config;

import com.wolf.common.util.lang.SystemUtil;
import com.wolf.common.util.net.NetworkUtil;
import java.io.Serializable;
import lombok.Data;

@Data
public class BrokerConfig implements Serializable {
    private String serverIp = NetworkUtil.getLocalAddress();
    private Integer serverPort = 8081;

    private boolean enableTrace = false;
    private boolean enableTopicAutoCreation = true;

    private int producerThreadNum = SystemUtil.getProcessorNumber();
    private int producerQueueCapacity = 10000;
    private int consumerThreadNum = SystemUtil.getProcessorNumber();
    private int consumerQueueCapacity = 10000;
}
