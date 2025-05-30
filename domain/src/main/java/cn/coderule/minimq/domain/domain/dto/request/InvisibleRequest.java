package cn.coderule.minimq.domain.domain.dto.request;

import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.consumer.receipt.ReceiptHandle;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvisibleRequest implements Serializable {
    private RequestContext requestContext;
    private ReceiptHandle receiptHandle;
    private String messageId;
    private String topicName;
    private String groupName;
    private long invisibleTime;
    private long timeout;
}
