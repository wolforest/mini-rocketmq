package cn.coderule.minimq.store.domain.commitlog.vo;

import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.service.store.infra.MappedFile;
import cn.coderule.minimq.domain.domain.model.message.MessageEncoder;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsertContext implements Serializable {
    private long now;
    private MessageBO messageBO;
    @Builder.Default
    private EnqueueResult result = null;
    @Builder.Default
    private long elapsedTimeInLock = 0;
    private MappedFile mappedFile;

    private MessageEncoder encoder;
}
