package cn.coderule.minimq.rpc.common.rpc.core.invoke;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.enums.BoundaryType;
import cn.coderule.minimq.domain.core.enums.code.LanguageCode;
import cn.coderule.minimq.rpc.common.rpc.core.enums.RemotingCommandType;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.FastCodesHeader;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.SystemResponseCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RocketMQSerializable;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import cn.coderule.minimq.rpc.common.rpc.core.enums.SerializeType;
import com.alibaba.fastjson2.annotation.JSONField;
import com.google.common.base.Stopwatch;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class RpcCommand implements Serializable {
    public static final String SERIALIZE_TYPE_PROPERTY = "rocketmq.serialize.type";
    public static final String SERIALIZE_TYPE_ENV = "ROCKETMQ_SERIALIZE_TYPE";
    public static final String REMOTING_VERSION_KEY = "rocketmq.remoting.version";
    private static final int RPC_TYPE = 0; // 0, REQUEST_COMMAND
    private static final int RPC_ONEWAY = 1; // 0, RPC
    private static final Map<Class<? extends CommandHeader>, Field[]> CLASS_HASH_MAP = new HashMap<>();
    private static final Map<Class<?>, String> CANONICAL_NAME_CACHE = new HashMap<>();
    // 1, Oneway
    // 1, RESPONSE_COMMAND
    private static final Map<Field, Boolean> NULLABLE_FIELD_CACHE = new HashMap<>();
    private static final String STRING_CANONICAL_NAME = String.class.getCanonicalName();
    private static final String DOUBLE_CANONICAL_NAME_1 = Double.class.getCanonicalName();
    private static final String DOUBLE_CANONICAL_NAME_2 = double.class.getCanonicalName();
    private static final String INTEGER_CANONICAL_NAME_1 = Integer.class.getCanonicalName();
    private static final String INTEGER_CANONICAL_NAME_2 = int.class.getCanonicalName();
    private static final String LONG_CANONICAL_NAME_1 = Long.class.getCanonicalName();
    private static final String LONG_CANONICAL_NAME_2 = long.class.getCanonicalName();
    private static final String BOOLEAN_CANONICAL_NAME_1 = Boolean.class.getCanonicalName();
    private static final String BOOLEAN_CANONICAL_NAME_2 = boolean.class.getCanonicalName();
    private static final String BOUNDARY_TYPE_CANONICAL_NAME = BoundaryType.class.getCanonicalName();
    private static volatile int configVersion = -1;
    private static final AtomicInteger REQUEST_ID = new AtomicInteger(0);

    private static SerializeType serializeTypeConfigInThisServer = SerializeType.JSON;

    static {
        final String protocol = System.getProperty(SERIALIZE_TYPE_PROPERTY, System.getenv(SERIALIZE_TYPE_ENV));
        if (!StringUtil.isBlank(protocol)) {
            try {
                serializeTypeConfigInThisServer = SerializeType.valueOf(protocol);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("parser specified protocol error. protocol=" + protocol, e);
            }
        }
    }

    private int code;
    private LanguageCode language = LanguageCode.JAVA;
    private int version = 0;
    private int opaque = REQUEST_ID.getAndIncrement();
    private int flag = 0;
    private String remark;
    private HashMap<String, String> extFields;
    private transient CommandHeader customHeader;
    private transient CommandHeader cachedHeader;

    private SerializeType serializeTypeCurrentRPC = serializeTypeConfigInThisServer;

    private transient byte[] body;
    private boolean suspended;
    private transient Stopwatch processTimer;

    public RpcCommand() {
    }

    public static RpcCommand createRequestCommand(int code) {
        return createRequestCommand(code, null);

    }

    public static RpcCommand createRequestCommand(int code, CommandHeader customHeader) {
        RpcCommand cmd = new RpcCommand();
        cmd.setCode(code);
        cmd.customHeader = customHeader;
        setCmdVersion(cmd);
        return cmd;
    }

    public static RpcCommand createResponseCommandWithHeader(int code, CommandHeader customHeader) {
        RpcCommand cmd = new RpcCommand();
        cmd.setCode(code);
        cmd.markResponseType();
        cmd.customHeader = customHeader;
        setCmdVersion(cmd);
        return cmd;
    }

    protected static void setCmdVersion(RpcCommand cmd) {
        if (configVersion >= 0) {
            cmd.setVersion(configVersion);
            return;
        }

        String v = System.getProperty(REMOTING_VERSION_KEY);
        if (v == null) {
            return;
        }

        int value = Integer.parseInt(v);
        cmd.setVersion(value);
        configVersion = value;
    }

    public static RpcCommand createResponseCommand(Class<? extends CommandHeader> classHeader) {
        return createResponseCommand(SystemResponseCode.SYSTEM_ERROR, "not set any response code", classHeader);
    }

    public static RpcCommand buildErrorResponse(int code, String remark,
        Class<? extends CommandHeader> classHeader) {
        final RpcCommand response = RpcCommand.createResponseCommand(classHeader);
        response.setCode(code);
        response.setRemark(remark);
        return response;
    }

    public static RpcCommand buildErrorResponse(int code, String remark) {
        return buildErrorResponse(code, remark, null);
    }

    public static RpcCommand createResponseCommand(int code, String remark,
        Class<? extends CommandHeader> classHeader) {
        RpcCommand cmd = new RpcCommand();
        cmd.markResponseType();
        cmd.setCode(code);
        cmd.setRemark(remark);
        setCmdVersion(cmd);

        if (classHeader == null) {
            return cmd;
        }

        return createResponseCommand(cmd, classHeader);
    }

    private static RpcCommand createResponseCommand(RpcCommand cmd, Class<? extends CommandHeader> classHeader) {
        try {
            cmd.customHeader = classHeader.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }

        return cmd;
    }

    public static RpcCommand createResponseCommand(int code, String remark) {
        return createResponseCommand(code, remark, null);
    }

    public static RpcCommand decode(final byte[] array) throws RemotingCommandException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        return decode(byteBuffer);
    }

    public static RpcCommand decode(final ByteBuffer byteBuffer) throws RemotingCommandException {
        return decode(Unpooled.wrappedBuffer(byteBuffer));
    }

    public static RpcCommand decode(final ByteBuf byteBuffer) throws RemotingCommandException {
        int length = byteBuffer.readableBytes();
        int oriHeaderLen = byteBuffer.readInt();
        int headerLength = getHeaderLength(oriHeaderLen);
        if (headerLength > length - 4) {
            throw new RemotingCommandException("decode error, bad header length: " + headerLength);
        }

        RpcCommand cmd = decodeHeader(byteBuffer, headerLength, getProtocolType(oriHeaderLen));

        int bodyLength = length - 4 - headerLength;
        byte[] bodyData = null;
        if (bodyLength > 0) {
            bodyData = new byte[bodyLength];
            byteBuffer.readBytes(bodyData);
        }
        assert cmd != null;
        cmd.body = bodyData;

        return cmd;
    }

    public static int getHeaderLength(int length) {
        return length & 0xFFFFFF;
    }

    private static RpcCommand decodeHeader(ByteBuf byteBuffer, int len,
        SerializeType type) throws RemotingCommandException {
        switch (type) {
            case JSON:
                byte[] headerData = new byte[len];
                byteBuffer.readBytes(headerData);
                RpcCommand resultJson = RpcSerializable.decode(headerData, RpcCommand.class);
                resultJson.setSerializeTypeCurrentRPC(type);
                return resultJson;
            case ROCKETMQ:
                RpcCommand resultRMQ = RocketMQSerializable.rocketMQProtocolDecode(byteBuffer, len);
                resultRMQ.setSerializeTypeCurrentRPC(type);
                return resultRMQ;
            default:
                break;
        }

        return null;
    }

    public static SerializeType getProtocolType(int source) {
        return SerializeType.valueOf((byte) ((source >> 24) & 0xFF));
    }

    public static int createNewRequestId() {
        return REQUEST_ID.getAndIncrement();
    }

    public static SerializeType getSerializeTypeConfigInThisServer() {
        return serializeTypeConfigInThisServer;
    }

    public static int markProtocolType(int source, SerializeType type) {
        return (type.getCode() << 24) | (source & 0x00FFFFFF);
    }

    public RpcCommand success() {
        return setCodeAndRemark(ResponseCode.SUCCESS, null);
    }

    public boolean isSuccess() {
        return getCode() == ResponseCode.SUCCESS;
    }

    public RpcCommand setCodeAndRemark(int code, String remark) {
        setCode(code);
        setRemark(remark);
        return this;
    }

    public void markResponseType() {
        int bits = 1 << RPC_TYPE;
        this.flag |= bits;
    }

    public CommandHeader readCustomHeader() {
        return customHeader;
    }

    public void writeHeader(CommandHeader customHeader) {
        this.customHeader = customHeader;
    }

    public <T extends CommandHeader> T decodeHeader(
        Class<T> classHeader) throws RemotingCommandException {
        return decodeHeader(classHeader, false);
    }

    public <T extends CommandHeader> T decodeHeader(
        Class<T> classHeader, boolean isCached) throws RemotingCommandException {
        if (isCached && cachedHeader != null) {
            return classHeader.cast(cachedHeader);
        }
        cachedHeader = decodeCommandHeaderDirectly(classHeader, true);
        if (cachedHeader == null) {
            return null;
        }
        return classHeader.cast(cachedHeader);
    }

    public <T extends CommandHeader> T decodeCommandHeaderDirectly(Class<T> classHeader,
        boolean useFastEncode) throws RemotingCommandException {
        T objectHeader = initCommandHeader(classHeader);
        if (objectHeader == null) {
            return null;
        }

        if (this.extFields == null) {
            return objectHeader;
        }

        if (objectHeader instanceof FastCodesHeader && useFastEncode) {
            ((FastCodesHeader) objectHeader).decode(this.extFields);
            objectHeader.checkFields();
            return objectHeader;
        }

        decodeHeader(classHeader, objectHeader);
        objectHeader.checkFields();

        return objectHeader;
    }

    private <T extends CommandHeader> T initCommandHeader(Class<T> classHeader) {
        T objectHeader;
        try {
            objectHeader = classHeader.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }

        return objectHeader;
    }

    private void decodeCommandHeaderField(Field field, String fieldName, CommandHeader objectHeader) {
        try {
            String value = this.extFields.get(fieldName);
            if (null == value) {
                if (!isFieldNullable(field)) {
                    throw new RemotingCommandException("the custom field <" + fieldName + "> is null");
                }
                return;
            }

            field.setAccessible(true);
            String type = getCanonicalName(field.getType());
            Object valueParsed;

            if (type.equals(STRING_CANONICAL_NAME)) {
                valueParsed = value;
            } else if (type.equals(INTEGER_CANONICAL_NAME_1) || type.equals(INTEGER_CANONICAL_NAME_2)) {
                valueParsed = Integer.parseInt(value);
            } else if (type.equals(LONG_CANONICAL_NAME_1) || type.equals(LONG_CANONICAL_NAME_2)) {
                valueParsed = Long.parseLong(value);
            } else if (type.equals(BOOLEAN_CANONICAL_NAME_1) || type.equals(BOOLEAN_CANONICAL_NAME_2)) {
                valueParsed = Boolean.parseBoolean(value);
            } else if (type.equals(DOUBLE_CANONICAL_NAME_1) || type.equals(DOUBLE_CANONICAL_NAME_2)) {
                valueParsed = Double.parseDouble(value);
            } else if (type.equals(BOUNDARY_TYPE_CANONICAL_NAME)) {
                valueParsed = BoundaryType.getType(value);
            } else {
                throw new RemotingCommandException("the custom field <" + fieldName + "> type is not supported");
            }

            field.set(objectHeader, valueParsed);

        } catch (Throwable e) {
            log.error("Failed field [{}] decoding", fieldName, e);
        }
    }

    private void decodeHeader(Class<? extends CommandHeader> classHeader, CommandHeader objectHeader) {
        Field[] fields = getClazzFields(classHeader);
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String fieldName = field.getName();
            if (fieldName.startsWith("this")) {
                continue;
            }

            decodeCommandHeaderField(field, fieldName, objectHeader);
        }
    }

    //make it able to test
    Field[] getClazzFields(Class<? extends CommandHeader> classHeader) {
        Field[] field = CLASS_HASH_MAP.get(classHeader);

        if (field != null) {
            return field;
        }

        Set<Field> fieldList = new HashSet<>();
        for (Class<?> className = classHeader; className != Object.class; className = className.getSuperclass()) {
            Field[] fields = className.getDeclaredFields();
            fieldList.addAll(Arrays.asList(fields));
        }
        field = fieldList.toArray(new Field[0]);
        synchronized (CLASS_HASH_MAP) {
            CLASS_HASH_MAP.put(classHeader, field);
        }
        return field;
    }

    private boolean isFieldNullable(Field field) {
        if (!NULLABLE_FIELD_CACHE.containsKey(field)) {
            Annotation annotation = field.getAnnotation(CFNotNull.class);
            synchronized (NULLABLE_FIELD_CACHE) {
                NULLABLE_FIELD_CACHE.put(field, annotation == null);
            }
        }
        return NULLABLE_FIELD_CACHE.get(field);
    }

    private String getCanonicalName(Class<?> clazz) {
        String name = CANONICAL_NAME_CACHE.get(clazz);

        if (name == null) {
            name = clazz.getCanonicalName();
            synchronized (CANONICAL_NAME_CACHE) {
                CANONICAL_NAME_CACHE.put(clazz, name);
            }
        }
        return name;
    }

    public ByteBuffer encode() {
        // 1> header length size
        int length = 4;

        // 2> header data length
        byte[] headerData = this.headerEncode();
        length += headerData.length;

        // 3> body data length
        if (this.body != null) {
            length += body.length;
        }

        ByteBuffer result = ByteBuffer.allocate(4 + length);

        // length
        result.putInt(length);

        // header length
        result.putInt(markProtocolType(headerData.length, serializeTypeCurrentRPC));

        // header data
        result.put(headerData);

        // body data;
        if (this.body != null) {
            result.put(this.body);
        }

        result.flip();

        return result;
    }

    private byte[] headerEncode() {
        this.makeCustomHeaderToNet();
        if (SerializeType.ROCKETMQ == serializeTypeCurrentRPC) {
            return RocketMQSerializable.rocketMQProtocolEncode(this);
        } else {
            return RpcSerializable.encode(this);
        }
    }

    public void makeCustomHeaderToNet() {
        if (this.customHeader == null) {
            return;
        }

        Field[] fields = getClazzFields(customHeader.getClass());
        if (null == this.extFields) {
            this.extFields = new HashMap<>();
        }

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            addFieldToExtFields(field);
        }
    }

    private void addFieldToExtFields(Field field) {
        String name = field.getName();
        if (name.startsWith("this")) {
            return;
        }

        Object value = null;
        try {
            field.setAccessible(true);
            value = field.get(this.customHeader);
        } catch (Exception e) {
            log.error("Failed to access field [{}]", name, e);
        }

        if (value != null) {
            this.extFields.put(name, value.toString());
        }
    }

    public void fastEncodeHeader(ByteBuf out) {
        int bodySize = this.body != null ? this.body.length : 0;
        int beginIndex = out.writerIndex();
        // skip 8 bytes
        out.writeLong(0);
        int headerSize;
        if (SerializeType.ROCKETMQ == serializeTypeCurrentRPC) {
            if (customHeader != null && !(customHeader instanceof FastCodesHeader)) {
                this.makeCustomHeaderToNet();
            }
            headerSize = RocketMQSerializable.rocketMQProtocolEncode(this, out);
        } else {
            this.makeCustomHeaderToNet();
            byte[] header = RpcSerializable.encode(this);
            headerSize = header.length;
            out.writeBytes(header);
        }
        out.setInt(beginIndex, 4 + headerSize + bodySize);
        out.setInt(beginIndex + 4, markProtocolType(headerSize, serializeTypeCurrentRPC));
    }

    public ByteBuffer encodeHeader() {
        return encodeHeader(this.body != null ? this.body.length : 0);
    }

    public ByteBuffer encodeHeader(final int bodyLength) {
        // 1> header length size
        int length = 4;

        // 2> header data length
        byte[] headerData;
        headerData = this.headerEncode();

        length += headerData.length;

        // 3> body data length
        length += bodyLength;

        ByteBuffer result = ByteBuffer.allocate(4 + length - bodyLength);

        // length
        result.putInt(length);

        // header length
        result.putInt(markProtocolType(headerData.length, serializeTypeCurrentRPC));

        // header data
        result.put(headerData);

        result.flip();

        return result;
    }

    public void markOnewayRPC() {
        int bits = 1 << RPC_ONEWAY;
        this.flag |= bits;
    }

    @JSONField(serialize = false)
    public boolean isOnewayRPC() {
        int bits = 1 << RPC_ONEWAY;
        return (this.flag & bits) == bits;
    }

    @JSONField(serialize = false)
    public RemotingCommandType getType() {
        if (this.isResponseType()) {
            return RemotingCommandType.RESPONSE_COMMAND;
        }

        return RemotingCommandType.REQUEST_COMMAND;
    }

    @JSONField(serialize = false)
    public boolean isResponseType() {
        int bits = 1 << RPC_TYPE;
        return (this.flag & bits) == bits;
    }

    public void addExtField(String key, String value) {
        if (null == extFields) {
            extFields = new HashMap<>(256);
        }
        extFields.put(key, value);
    }

    public void addExtFieldIfNotExist(String key, String value) {
        extFields.putIfAbsent(key, value);
    }

    @Override
    public String toString() {
        return "RemotingCommand [code=" + code + ", language=" + language + ", version=" + version + ", opaque=" + opaque + ", flag(B)="
            + Integer.toBinaryString(flag) + ", remark=" + remark + ", extFields=" + extFields + ", serializeTypeCurrentRPC="
            + serializeTypeCurrentRPC + "]";
    }

}
