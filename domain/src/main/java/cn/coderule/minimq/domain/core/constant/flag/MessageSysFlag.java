package cn.coderule.minimq.domain.core.constant.flag;


public class MessageSysFlag {

    /**
     * Meaning of each bit in the system flag
     *
     * | bit    | 7 | 6 | 5         | 4        | 3           | 2                | 1                | 0                |
     * |--------|---|---|-----------|----------|-------------|------------------|------------------|------------------|
     * | byte 1 |   |   | STOREHOST | BORNHOST | TRANSACTION | TRANSACTION      | MULTI_TAGS       | COMPRESSED       |
     * | byte 2 |   |   |           |          |             | COMPRESSION_TYPE | COMPRESSION_TYPE | COMPRESSION_TYPE |
     * | byte 3 |   |   |           |          |             |                  |                  |                  |
     * | byte 4 |   |   |           |          |             |                  |                  |                  |
     */
    public final static int COMPRESSED_FLAG = 0x1;
    public final static int MULTI_TAGS_FLAG = 0x1 << 1;

    /**
     * transaction type
     *  not type: 0000-0000-0000-00-00
     *  prepare:  0000-0000-0000-01-00
     *  commit:   0000-0000-0000-10-00
     *  rollback: 0000-0000-0000-11-00
     *
     * not transaction message flag, include below:
     *  1. normal message
     *  2. message with DelayLevel
     *  3. message with Timer
     */
    public final static int TRANSACTION_NOT_TYPE = 0;
    public final static int TRANSACTION_PREPARED_TYPE = 0x1 << 2;
    public final static int TRANSACTION_COMMIT_TYPE = 0x2 << 2;
    public final static int TRANSACTION_ROLLBACK_TYPE = 0x3 << 2;

    public final static int BORNHOST_V6_FLAG = 0x1 << 4;
    public final static int STOREHOSTADDRESS_V6_FLAG = 0x1 << 5;
    //Mark the flag for batch to avoid conflict
    public final static int NEED_UNWRAP_FLAG = 0x1 << 6;
    public final static int INNER_BATCH_FLAG = 0x1 << 7;

    // COMPRESSION_TYPE
    public final static int COMPRESSION_LZ4_TYPE = 0x1 << 8;
    public final static int COMPRESSION_ZSTD_TYPE = 0x2 << 8;
    public final static int COMPRESSION_ZLIB_TYPE = 0x3 << 8;
    public final static int COMPRESSION_TYPE_COMPARATOR = 0x7 << 8;

    /**
     * calculate message transaction type
     *
     * TRANSACTION_ROLLBACK_TYPE is
     *    0000-0000-0000-11-00
     * => flag & TRANSACTION_ROLLBACK_TYPE
     *  set 0-1 and 3-15 to 0
     *  then keep the 2-3 to  get the transaction type
     *
     * @param flag message flag
     * @return transaction type
     */
    public static int getTransactionValue(final int flag) {
        return flag & TRANSACTION_ROLLBACK_TYPE;
    }

    /**
     *
     * @param flag source transaction type
     * @param type target transaction type
     * @return new transaction type
     */
    public static int resetTransactionValue(final int flag, final int type) {
        return (flag & (~TRANSACTION_ROLLBACK_TYPE)) | type;
    }

    public static boolean check(int flag, int expectedFlag) {
        return (flag & expectedFlag) != 0;
    }

}
