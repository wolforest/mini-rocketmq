package cn.coderule.minimq.rpc.registry.protocol.cluster;

import cn.coderule.minimq.rpc.common.core.enums.RequestType;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigSerializeWrapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInfo extends ServerInfo {
    private String haAddress;
    private Boolean enableMasterElection;

    private TopicConfigSerializeWrapper topicInfo;
    private List<String> filterList;

    private int registerTimeout;
    private RequestType registerType;

    private int hash;
    private boolean compressed;

    public StoreInfo(String clusterName, String address) {
        this.clusterName = clusterName;
        this.address = address;
    }

    public boolean isEnableMasterElection() {
        return enableMasterElection != null && enableMasterElection;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj instanceof StoreInfo addr) {
            return clusterName.equals(addr.clusterName)
                && address.equals(addr.address);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && clusterName.length() + address.length() > 0) {
            for (int i = 0; i < clusterName.length(); i++) {
                h = 31 * h + clusterName.charAt(i);
            }
            h = 31 * h + '_';
            for (int i = 0; i < address.length(); i++) {
                h = 31 * h + address.charAt(i);
            }
            hash = h;
        }
        return h;
    }

}
