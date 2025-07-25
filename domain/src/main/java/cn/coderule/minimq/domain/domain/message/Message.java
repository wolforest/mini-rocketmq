package cn.coderule.minimq.domain.domain.message;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    protected String topic;
    protected int flag;
    protected Map<String, String> properties;
    protected byte[] body;
    protected String transactionId;


    public String getStringBody() {
        return new String(this.body, StandardCharsets.UTF_8);
    }

    public void putProperty(final String name, final String value) {
        if (null == this.properties) {
            this.properties = new HashMap<>();
        }

        this.properties.put(name, value);
    }

    public String getProperty(final String name) {
        if (null == this.properties) {
            this.properties = new HashMap<>();
        }

        return this.properties.get(name);
    }

    public void removeProperty(final String name) {
        if (null == this.properties) {
            return;
        }

        this.properties.remove(name);
    }
}
