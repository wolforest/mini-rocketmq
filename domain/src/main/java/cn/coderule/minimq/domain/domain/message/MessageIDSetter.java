package cn.coderule.minimq.domain.domain.message;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;


public class MessageIDSetter {

    /**
     * ip.length + 2 + 4 + 4 + 2
     * length = 16
     */
    private static final int LEN;

    /**
     * ip + pid + classLoader.hashCode()
     * length = 10
     */
    private static final char[] FIX_STRING;
    private static final AtomicInteger COUNTER;
    private static long startTime;
    private static long nextStartTime;

    static {
        byte[] ip;
        try {
            ip = NetworkUtil.getLocalIP().getBytes();
        } catch (Exception e) {
            ip = createFakeIP();
        }
        LEN = ip.length + 2 + 4 + 4 + 2;
        ByteBuffer tempBuffer = ByteBuffer.allocate(ip.length + 2 + 4);
        tempBuffer.put(ip);
        tempBuffer.putShort((short) SystemUtil.getPID());
        tempBuffer.putInt(MessageIDSetter.class.getClassLoader().hashCode());
        FIX_STRING = StringUtil.bytes2string(tempBuffer.array()).toCharArray();
        setStartTime(System.currentTimeMillis());
        COUNTER = new AtomicInteger(0);
    }

    private synchronized static void setStartTime(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        nextStartTime = cal.getTimeInMillis();
    }

    public static Date getNearlyTimeFromID(String msgID) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        byte[] bytes = StringUtil.string2bytes(msgID);
        int ipLength = bytes.length == 28 ? 16 : 4;
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.put(bytes, ipLength + 2 + 4, 4);
        buf.position(0);
        long spanMS = buf.getLong();
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long monStartTime = cal.getTimeInMillis();
        if (monStartTime + spanMS >= now) {
            cal.add(Calendar.MONTH, -1);
            monStartTime = cal.getTimeInMillis();
        }
        cal.setTimeInMillis(monStartTime + spanMS);
        return cal.getTime();
    }

    public static String getIPStrFromID(String msgID) {
        byte[] ipBytes = getIPFromID(msgID);
        if (ipBytes.length == 16) {
            return NetworkUtil.ipToIPv6Str(ipBytes);
        } else {
            return NetworkUtil.ipToIPv4Str(ipBytes);
        }
    }

    public static byte[] getIPFromID(String msgID) {
        byte[] bytes = StringUtil.string2bytes(msgID);
        int ipLength = bytes.length == 28 ? 16 : 4;
        byte[] result = new byte[ipLength];
        System.arraycopy(bytes, 0, result, 0, ipLength);
        return result;
    }

    public static int getPidFromID(String msgID) {
        byte[] bytes = StringUtil.string2bytes(msgID);
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        int value = wrap.getShort(bytes.length - 2 - 4 - 4 - 2);
        return value & 0x0000FFFF;
    }

    /**
     * ip.length + 2 + 4 + 4 + 2
     * ip + pid + classLoader.hashCode() + timeDiff + counter
     *
     * @return unique messageID
     */
    public static String createUniqID() {
        char[] sb = new char[LEN * 2];
        System.arraycopy(FIX_STRING, 0, sb, 0, FIX_STRING.length);
        long current = System.currentTimeMillis();
        if (current >= nextStartTime) {
            setStartTime(current);
        }
        int diff = (int)(current - startTime);
        if (diff < 0 && diff > -1000_000) {
            // may cause by NTP
            diff = 0;
        }
        int pos = FIX_STRING.length;
        StringUtil.writeInt(sb, pos, diff);
        pos += 8;
        StringUtil.writeShort(sb, pos, COUNTER.getAndIncrement());
        return new String(sb);
    }

    public static void setUniqID(final MessageBO msg) {
        if (msg.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX) == null) {
            msg.putProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX, createUniqID());
        }
    }

    public static String getUniqID(MessageBO msg) {
        return msg.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX);
    }

    public static byte[] createFakeIP() {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(System.currentTimeMillis());
        bb.position(4);
        byte[] fakeIP = new byte[4];
        bb.get(fakeIP);
        return fakeIP;
    }
}
