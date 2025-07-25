package cn.coderule.minimq.domain.domain.message;

import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.minimq.domain.core.enums.message.MessageVersion;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MessageDecoder {
    public static MessageBO decode(ByteBuffer byteBuffer) {
        return decode(byteBuffer, true, true);
    }

    public static MessageBO decode(ByteBuffer byteBuffer, final boolean readBody) {
        return decode(byteBuffer, readBody, true);
    }

    public static MessageBO decode(
        java.nio.ByteBuffer byteBuffer, final boolean readBody,
        final boolean isSetPropertiesString) {
        return decode(byteBuffer, readBody, isSetPropertiesString, false);
    }

    public static MessageBO decode(
        java.nio.ByteBuffer byteBuffer, final boolean readBody,
        final boolean isSetPropertiesString, final boolean checkCRC) {
        try {

            MessageBO msgExt = new MessageBO();

            // 1 TOTALSIZE
            int storeSize = byteBuffer.getInt();
            msgExt.setMessageSize(storeSize);

            // 2 MAGICCODE
            int magicCode = byteBuffer.getInt();
            MessageVersion version = MessageVersion.valueOfMagicCode(magicCode);

            // 3 BODYCRC
            int bodyCRC = byteBuffer.getInt();
            msgExt.setBodyCRC(bodyCRC);

            // 4 QUEUEID
            int queueId = byteBuffer.getInt();
            msgExt.setQueueId(queueId);

            // 5 FLAG
            int flag = byteBuffer.getInt();
            msgExt.setFlag(flag);

            // 6 QUEUEOFFSET
            long queueOffset = byteBuffer.getLong();
            msgExt.setQueueOffset(queueOffset);

            // 7 PHYSICALOFFSET
            long physicOffset = byteBuffer.getLong();
            msgExt.setCommitLogOffset(physicOffset);

            // 8 SYSFLAG
            int sysFlag = byteBuffer.getInt();
            msgExt.setSysFlag(sysFlag);

            // 9 BORNTIMESTAMP
            long bornTimeStamp = byteBuffer.getLong();
            msgExt.setBornTimestamp(bornTimeStamp);

            // 10 BORNHOST
            int bornhostIPLength = 4;
            byte[] bornHost = new byte[bornhostIPLength];
            byteBuffer.get(bornHost, 0, bornhostIPLength);
            int port = byteBuffer.getInt();
            msgExt.setBornHost(new InetSocketAddress(InetAddress.getByAddress(bornHost), port));

            // 11 STORETIMESTAMP
            long storeTimestamp = byteBuffer.getLong();
            msgExt.setStoreTimestamp(storeTimestamp);

            // 12 STOREHOST
            int storehostIPLength = 4;
            byte[] storeHost = new byte[storehostIPLength];
            byteBuffer.get(storeHost, 0, storehostIPLength);
            port = byteBuffer.getInt();
            msgExt.setStoreHost(new InetSocketAddress(InetAddress.getByAddress(storeHost), port));

            // 13 RECONSUMETIMES
            int reconsumeTimes = byteBuffer.getInt();
            msgExt.setReconsumeTimes(reconsumeTimes);

            // 14 Prepared Transaction Offset
            long preparedTransactionOffset = byteBuffer.getLong();
            msgExt.setPreparedTransactionOffset(preparedTransactionOffset);

            // 15 BODY
            int bodyLen = byteBuffer.getInt();
            if (bodyLen > 0) {
                if (readBody) {
                    byte[] body = new byte[bodyLen];
                    byteBuffer.get(body);

                    if (checkCRC) {
                        //crc body
                        int crc = HashUtil.crc32(body);
                        if (crc != bodyCRC) {
                            throw new Exception("Msg crc is error!");
                        }
                    }

                    msgExt.setBody(body);
                } else {
                    byteBuffer.position(byteBuffer.position() + bodyLen);
                }
            }

            // 16 TOPIC
            int topicLen = version.getTopicLength(byteBuffer);
            byte[] topic = new byte[topicLen];
            byteBuffer.get(topic);
            msgExt.setTopic(new String(topic, StandardCharsets.UTF_8));

            // 17 properties
            short propertiesLength = byteBuffer.getShort();
            if (propertiesLength > 0) {
                byte[] properties = new byte[propertiesLength];
                byteBuffer.get(properties);
                String propertiesString = new String(properties, StandardCharsets.UTF_8);
//                if (!isSetPropertiesString) {
//                    Map<String, String> map = string2messageProperties(propertiesString);
//                    msgExt.setProperties(map);
//                } else {
//                    Map<String, String> map = string2messageProperties(propertiesString);
//                    map.put("propertiesString", propertiesString);
//                    msgExt.setProperties(map);
//                }
            }

            int msgIDLength = storehostIPLength + 4 + 8;
            ByteBuffer byteBufferMsgId = ByteBuffer.allocate(msgIDLength);
//            String msgId = createMessageId(byteBufferMsgId, msgExt.getStoreHost(), msgExt.getCommitLogOffset());
//            msgExt.setMsgId(msgId);

            return msgExt;
        } catch (Exception e) {
            byteBuffer.position(byteBuffer.limit());
        }

        return null;
    }

    public static String createMessageId(final ByteBuffer input, final ByteBuffer addr, final long offset) {
        input.flip();
        int msgIDLength = addr.limit() == 8 ? 16 : 28;
        input.limit(msgIDLength);

        input.put(addr);
        input.putLong(offset);
        return null;
//        return StringUtils.bytes2string(input.array());
    }

}
