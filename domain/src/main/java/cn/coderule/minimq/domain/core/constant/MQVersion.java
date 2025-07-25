package cn.coderule.minimq.domain.core.constant;

public class MQVersion {

    public static final int CURRENT_VERSION = Version.V5_2_0.ordinal();

    public static String getVersionDesc(int value) {
        int length = Version.values().length;
        if (value >= length) {
            return Version.values()[length - 1].name();
        }

        return Version.values()[value].name();
    }

    public static Version value2Version(int value) {
        int length = Version.values().length;
        if (value >= length) {
            return Version.values()[length - 1];
        }

        return Version.values()[value];
    }

    public enum Version {
        V3_0_0_SNAPSHOT,
        V3_0_0_ALPHA1,
        V3_0_0_BETA1,
        V3_0_0_BETA2,
        V3_0_0_BETA3,
        V3_0_0_BETA4,
        V3_0_0_BETA5,
        V3_0_0_BETA6_SNAPSHOT,
        V3_0_0_BETA6,
        V3_0_0_BETA7_SNAPSHOT,
        V3_0_0_BETA7,
        V3_0_0_BETA8_SNAPSHOT,
        V3_0_0_BETA8,
        V3_0_0_BETA9_SNAPSHOT,
        V3_0_0_BETA9,
        V3_0_0_FINAL,
        V3_0_1_SNAPSHOT,
        V3_0_1,
        V3_0_2_SNAPSHOT,
        V3_0_2,
        V3_0_3_SNAPSHOT,
        V3_0_3,
        V3_0_4_SNAPSHOT,
        V3_0_4,
        V3_0_5_SNAPSHOT,
        V3_0_5,
        V3_0_6_SNAPSHOT,
        V3_0_6,
        V3_0_7_SNAPSHOT,
        V3_0_7,
        V3_0_8_SNAPSHOT,
        V3_0_8,
        V3_0_9_SNAPSHOT,
        V3_0_9,

        V3_0_10_SNAPSHOT,
        V3_0_10,

        V3_0_11_SNAPSHOT,
        V3_0_11,

        V3_0_12_SNAPSHOT,
        V3_0_12,

        V3_0_13_SNAPSHOT,
        V3_0_13,

        V3_0_14_SNAPSHOT,
        V3_0_14,

        V3_0_15_SNAPSHOT,
        V3_0_15,

        V3_1_0_SNAPSHOT,
        V3_1_0,

        V3_1_1_SNAPSHOT,
        V3_1_1,

        V3_1_2_SNAPSHOT,
        V3_1_2,

        V3_1_3_SNAPSHOT,
        V3_1_3,

        V3_1_4_SNAPSHOT,
        V3_1_4,

        V3_1_5_SNAPSHOT,
        V3_1_5,

        V3_1_6_SNAPSHOT,
        V3_1_6,

        V3_1_7_SNAPSHOT,
        V3_1_7,

        V3_1_8_SNAPSHOT,
        V3_1_8,

        V3_1_9_SNAPSHOT,
        V3_1_9,

        V3_2_0_SNAPSHOT,
        V3_2_0,

        V3_2_1_SNAPSHOT,
        V3_2_1,

        V3_2_2_SNAPSHOT,
        V3_2_2,

        V3_2_3_SNAPSHOT,
        V3_2_3,

        V3_2_4_SNAPSHOT,
        V3_2_4,

        V3_2_5_SNAPSHOT,
        V3_2_5,

        V3_2_6_SNAPSHOT,
        V3_2_6,

        V3_2_7_SNAPSHOT,
        V3_2_7,

        V3_2_8_SNAPSHOT,
        V3_2_8,

        V3_2_9_SNAPSHOT,
        V3_2_9,

        V3_3_1_SNAPSHOT,
        V3_3_1,

        V3_3_2_SNAPSHOT,
        V3_3_2,

        V3_3_3_SNAPSHOT,
        V3_3_3,

        V3_3_4_SNAPSHOT,
        V3_3_4,

        V3_3_5_SNAPSHOT,
        V3_3_5,

        V3_3_6_SNAPSHOT,
        V3_3_6,

        V3_3_7_SNAPSHOT,
        V3_3_7,

        V3_3_8_SNAPSHOT,
        V3_3_8,

        V3_3_9_SNAPSHOT,
        V3_3_9,

        V3_4_1_SNAPSHOT,
        V3_4_1,

        V3_4_2_SNAPSHOT,
        V3_4_2,

        V3_4_3_SNAPSHOT,
        V3_4_3,

        V3_4_4_SNAPSHOT,
        V3_4_4,

        V3_4_5_SNAPSHOT,
        V3_4_5,

        V3_4_6_SNAPSHOT,
        V3_4_6,

        V3_4_7_SNAPSHOT,
        V3_4_7,

        V3_4_8_SNAPSHOT,
        V3_4_8,

        V3_4_9_SNAPSHOT,
        V3_4_9,
        V3_5_1_SNAPSHOT,
        V3_5_1,

        V3_5_2_SNAPSHOT,
        V3_5_2,

        V3_5_3_SNAPSHOT,
        V3_5_3,

        V3_5_4_SNAPSHOT,
        V3_5_4,

        V3_5_5_SNAPSHOT,
        V3_5_5,

        V3_5_6_SNAPSHOT,
        V3_5_6,

        V3_5_7_SNAPSHOT,
        V3_5_7,

        V3_5_8_SNAPSHOT,
        V3_5_8,

        V3_5_9_SNAPSHOT,
        V3_5_9,

        V3_6_1_SNAPSHOT,
        V3_6_1,

        V3_6_2_SNAPSHOT,
        V3_6_2,

        V3_6_3_SNAPSHOT,
        V3_6_3,

        V3_6_4_SNAPSHOT,
        V3_6_4,

        V3_6_5_SNAPSHOT,
        V3_6_5,

        V3_6_6_SNAPSHOT,
        V3_6_6,

        V3_6_7_SNAPSHOT,
        V3_6_7,

        V3_6_8_SNAPSHOT,
        V3_6_8,

        V3_6_9_SNAPSHOT,
        V3_6_9,

        V3_7_1_SNAPSHOT,
        V3_7_1,

        V3_7_2_SNAPSHOT,
        V3_7_2,

        V3_7_3_SNAPSHOT,
        V3_7_3,

        V3_7_4_SNAPSHOT,
        V3_7_4,

        V3_7_5_SNAPSHOT,
        V3_7_5,

        V3_7_6_SNAPSHOT,
        V3_7_6,

        V3_7_7_SNAPSHOT,
        V3_7_7,

        V3_7_8_SNAPSHOT,
        V3_7_8,

        V3_7_9_SNAPSHOT,
        V3_7_9,

        V3_8_1_SNAPSHOT,
        V3_8_1,

        V3_8_2_SNAPSHOT,
        V3_8_2,

        V3_8_3_SNAPSHOT,
        V3_8_3,

        V3_8_4_SNAPSHOT,
        V3_8_4,

        V3_8_5_SNAPSHOT,
        V3_8_5,

        V3_8_6_SNAPSHOT,
        V3_8_6,

        V3_8_7_SNAPSHOT,
        V3_8_7,

        V3_8_8_SNAPSHOT,
        V3_8_8,

        V3_8_9_SNAPSHOT,
        V3_8_9,

        V3_9_1_SNAPSHOT,
        V3_9_1,

        V3_9_2_SNAPSHOT,
        V3_9_2,

        V3_9_3_SNAPSHOT,
        V3_9_3,

        V3_9_4_SNAPSHOT,
        V3_9_4,

        V3_9_5_SNAPSHOT,
        V3_9_5,

        V3_9_6_SNAPSHOT,
        V3_9_6,

        V3_9_7_SNAPSHOT,
        V3_9_7,

        V3_9_8_SNAPSHOT,
        V3_9_8,

        V3_9_9_SNAPSHOT,
        V3_9_9,

        V4_0_0_SNAPSHOT,
        V4_0_0,

        V4_0_1_SNAPSHOT,
        V4_0_1,

        V4_0_2_SNAPSHOT,
        V4_0_2,

        V4_0_3_SNAPSHOT,
        V4_0_3,

        V4_0_4_SNAPSHOT,
        V4_0_4,

        V4_0_5_SNAPSHOT,
        V4_0_5,

        V4_0_6_SNAPSHOT,
        V4_0_6,

        V4_0_7_SNAPSHOT,
        V4_0_7,

        V4_0_8_SNAPSHOT,
        V4_0_8,

        V4_0_9_SNAPSHOT,
        V4_0_9,

        V4_1_0_SNAPSHOT,
        V4_1_0,

        V4_1_1_SNAPSHOT,
        V4_1_1,

        V4_1_2_SNAPSHOT,
        V4_1_2,

        V4_1_3_SNAPSHOT,
        V4_1_3,

        V4_1_4_SNAPSHOT,
        V4_1_4,

        V4_1_5_SNAPSHOT,
        V4_1_5,

        V4_1_6_SNAPSHOT,
        V4_1_6,

        V4_1_7_SNAPSHOT,
        V4_1_7,

        V4_1_8_SNAPSHOT,
        V4_1_8,

        V4_1_9_SNAPSHOT,
        V4_1_9,

        V4_2_0_SNAPSHOT,
        V4_2_0,

        V4_2_1_SNAPSHOT,
        V4_2_1,

        V4_2_2_SNAPSHOT,
        V4_2_2,

        V4_2_3_SNAPSHOT,
        V4_2_3,

        V4_2_4_SNAPSHOT,
        V4_2_4,

        V4_2_5_SNAPSHOT,
        V4_2_5,

        V4_2_6_SNAPSHOT,
        V4_2_6,

        V4_2_7_SNAPSHOT,
        V4_2_7,

        V4_2_8_SNAPSHOT,
        V4_2_8,

        V4_2_9_SNAPSHOT,
        V4_2_9,

        V4_3_0_SNAPSHOT,
        V4_3_0,

        V4_3_1_SNAPSHOT,
        V4_3_1,

        V4_3_2_SNAPSHOT,
        V4_3_2,

        V4_3_3_SNAPSHOT,
        V4_3_3,

        V4_3_4_SNAPSHOT,
        V4_3_4,

        V4_3_5_SNAPSHOT,
        V4_3_5,

        V4_3_6_SNAPSHOT,
        V4_3_6,

        V4_3_7_SNAPSHOT,
        V4_3_7,

        V4_3_8_SNAPSHOT,
        V4_3_8,

        V4_3_9_SNAPSHOT,
        V4_3_9,

        V4_4_0_SNAPSHOT,
        V4_4_0,

        V4_4_1_SNAPSHOT,
        V4_4_1,

        V4_4_2_SNAPSHOT,
        V4_4_2,

        V4_4_3_SNAPSHOT,
        V4_4_3,

        V4_4_4_SNAPSHOT,
        V4_4_4,

        V4_4_5_SNAPSHOT,
        V4_4_5,

        V4_4_6_SNAPSHOT,
        V4_4_6,

        V4_4_7_SNAPSHOT,
        V4_4_7,

        V4_4_8_SNAPSHOT,
        V4_4_8,

        V4_4_9_SNAPSHOT,
        V4_4_9,

        V4_5_0_SNAPSHOT,
        V4_5_0,

        V4_5_1_SNAPSHOT,
        V4_5_1,

        V4_5_2_SNAPSHOT,
        V4_5_2,

        V4_5_3_SNAPSHOT,
        V4_5_3,

        V4_5_4_SNAPSHOT,
        V4_5_4,

        V4_5_5_SNAPSHOT,
        V4_5_5,

        V4_5_6_SNAPSHOT,
        V4_5_6,

        V4_5_7_SNAPSHOT,
        V4_5_7,

        V4_5_8_SNAPSHOT,
        V4_5_8,

        V4_5_9_SNAPSHOT,
        V4_5_9,

        V4_6_0_SNAPSHOT,
        V4_6_0,

        V4_6_1_SNAPSHOT,
        V4_6_1,

        V4_6_2_SNAPSHOT,
        V4_6_2,

        V4_6_3_SNAPSHOT,
        V4_6_3,

        V4_6_4_SNAPSHOT,
        V4_6_4,

        V4_6_5_SNAPSHOT,
        V4_6_5,

        V4_6_6_SNAPSHOT,
        V4_6_6,

        V4_6_7_SNAPSHOT,
        V4_6_7,

        V4_6_8_SNAPSHOT,
        V4_6_8,

        V4_6_9_SNAPSHOT,
        V4_6_9,

        V4_7_0_SNAPSHOT,
        V4_7_0,

        V4_7_1_SNAPSHOT,
        V4_7_1,

        V4_7_2_SNAPSHOT,
        V4_7_2,

        V4_7_3_SNAPSHOT,
        V4_7_3,

        V4_7_4_SNAPSHOT,
        V4_7_4,

        V4_7_5_SNAPSHOT,
        V4_7_5,

        V4_7_6_SNAPSHOT,
        V4_7_6,

        V4_7_7_SNAPSHOT,
        V4_7_7,

        V4_7_8_SNAPSHOT,
        V4_7_8,

        V4_7_9_SNAPSHOT,
        V4_7_9,

        V4_8_0_SNAPSHOT,
        V4_8_0,

        V4_8_1_SNAPSHOT,
        V4_8_1,

        V4_8_2_SNAPSHOT,
        V4_8_2,

        V4_8_3_SNAPSHOT,
        V4_8_3,

        V4_8_4_SNAPSHOT,
        V4_8_4,

        V4_8_5_SNAPSHOT,
        V4_8_5,

        V4_8_6_SNAPSHOT,
        V4_8_6,

        V4_8_7_SNAPSHOT,
        V4_8_7,

        V4_8_8_SNAPSHOT,
        V4_8_8,

        V4_8_9_SNAPSHOT,
        V4_8_9,

        V4_9_0_SNAPSHOT,
        V4_9_0,

        V4_9_1_SNAPSHOT,
        V4_9_1,

        V4_9_2_SNAPSHOT,
        V4_9_2,

        V4_9_3_SNAPSHOT,
        V4_9_3,

        V4_9_4_SNAPSHOT,
        V4_9_4,

        V4_9_5_SNAPSHOT,
        V4_9_5,

        V4_9_6_SNAPSHOT,
        V4_9_6,

        V4_9_7_SNAPSHOT,
        V4_9_7,

        V4_9_8_SNAPSHOT,
        V4_9_8,

        V4_9_9_SNAPSHOT,
        V4_9_9,

        V5_0_0_SNAPSHOT,
        V5_0_0,

        V5_0_1_SNAPSHOT,
        V5_0_1,

        V5_0_2_SNAPSHOT,
        V5_0_2,

        V5_0_3_SNAPSHOT,
        V5_0_3,

        V5_0_4_SNAPSHOT,
        V5_0_4,

        V5_0_5_SNAPSHOT,
        V5_0_5,

        V5_0_6_SNAPSHOT,
        V5_0_6,

        V5_0_7_SNAPSHOT,
        V5_0_7,

        V5_0_8_SNAPSHOT,
        V5_0_8,

        V5_0_9_SNAPSHOT,
        V5_0_9,

        V5_1_0_SNAPSHOT,
        V5_1_0,

        V5_1_1_SNAPSHOT,
        V5_1_1,

        V5_1_2_SNAPSHOT,
        V5_1_2,

        V5_1_3_SNAPSHOT,
        V5_1_3,

        V5_1_4_SNAPSHOT,
        V5_1_4,

        V5_1_5_SNAPSHOT,
        V5_1_5,

        V5_1_6_SNAPSHOT,
        V5_1_6,

        V5_1_7_SNAPSHOT,
        V5_1_7,

        V5_1_8_SNAPSHOT,
        V5_1_8,

        V5_1_9_SNAPSHOT,
        V5_1_9,

        V5_2_0_SNAPSHOT,
        V5_2_0,

        V5_2_1_SNAPSHOT,
        V5_2_1,

        V5_2_2_SNAPSHOT,
        V5_2_2,

        V5_2_3_SNAPSHOT,
        V5_2_3,

        V5_2_4_SNAPSHOT,
        V5_2_4,

        V5_2_5_SNAPSHOT,
        V5_2_5,

        V5_2_6_SNAPSHOT,
        V5_2_6,

        V5_2_7_SNAPSHOT,
        V5_2_7,

        V5_2_8_SNAPSHOT,
        V5_2_8,

        V5_2_9_SNAPSHOT,
        V5_2_9,

        V5_3_0_SNAPSHOT,
        V5_3_0,

        V5_3_1_SNAPSHOT,
        V5_3_1,

        V5_3_2_SNAPSHOT,
        V5_3_2,

        V5_3_3_SNAPSHOT,
        V5_3_3,

        V5_3_4_SNAPSHOT,
        V5_3_4,

        V5_3_5_SNAPSHOT,
        V5_3_5,

        V5_3_6_SNAPSHOT,
        V5_3_6,

        V5_3_7_SNAPSHOT,
        V5_3_7,

        V5_3_8_SNAPSHOT,
        V5_3_8,

        V5_3_9_SNAPSHOT,
        V5_3_9,

        V5_4_0_SNAPSHOT,
        V5_4_0,

        V5_4_1_SNAPSHOT,
        V5_4_1,

        V5_4_2_SNAPSHOT,
        V5_4_2,

        V5_4_3_SNAPSHOT,
        V5_4_3,

        V5_4_4_SNAPSHOT,
        V5_4_4,

        V5_4_5_SNAPSHOT,
        V5_4_5,

        V5_4_6_SNAPSHOT,
        V5_4_6,

        V5_4_7_SNAPSHOT,
        V5_4_7,

        V5_4_8_SNAPSHOT,
        V5_4_8,

        V5_4_9_SNAPSHOT,
        V5_4_9,

        V5_5_0_SNAPSHOT,
        V5_5_0,

        V5_5_1_SNAPSHOT,
        V5_5_1,

        V5_5_2_SNAPSHOT,
        V5_5_2,

        V5_5_3_SNAPSHOT,
        V5_5_3,

        V5_5_4_SNAPSHOT,
        V5_5_4,

        V5_5_5_SNAPSHOT,
        V5_5_5,

        V5_5_6_SNAPSHOT,
        V5_5_6,

        V5_5_7_SNAPSHOT,
        V5_5_7,

        V5_5_8_SNAPSHOT,
        V5_5_8,

        V5_5_9_SNAPSHOT,
        V5_5_9,

        V5_6_0_SNAPSHOT,
        V5_6_0,

        V5_6_1_SNAPSHOT,
        V5_6_1,

        V5_6_2_SNAPSHOT,
        V5_6_2,

        V5_6_3_SNAPSHOT,
        V5_6_3,

        V5_6_4_SNAPSHOT,
        V5_6_4,

        V5_6_5_SNAPSHOT,
        V5_6_5,

        V5_6_6_SNAPSHOT,
        V5_6_6,

        V5_6_7_SNAPSHOT,
        V5_6_7,

        V5_6_8_SNAPSHOT,
        V5_6_8,

        V5_6_9_SNAPSHOT,
        V5_6_9,

        V5_7_0_SNAPSHOT,
        V5_7_0,

        V5_7_1_SNAPSHOT,
        V5_7_1,

        V5_7_2_SNAPSHOT,
        V5_7_2,

        V5_7_3_SNAPSHOT,
        V5_7_3,

        V5_7_4_SNAPSHOT,
        V5_7_4,

        V5_7_5_SNAPSHOT,
        V5_7_5,

        V5_7_6_SNAPSHOT,
        V5_7_6,

        V5_7_7_SNAPSHOT,
        V5_7_7,

        V5_7_8_SNAPSHOT,
        V5_7_8,

        V5_7_9_SNAPSHOT,
        V5_7_9,

        V5_8_0_SNAPSHOT,
        V5_8_0,

        V5_8_1_SNAPSHOT,
        V5_8_1,

        V5_8_2_SNAPSHOT,
        V5_8_2,

        V5_8_3_SNAPSHOT,
        V5_8_3,

        V5_8_4_SNAPSHOT,
        V5_8_4,

        V5_8_5_SNAPSHOT,
        V5_8_5,

        V5_8_6_SNAPSHOT,
        V5_8_6,

        V5_8_7_SNAPSHOT,
        V5_8_7,

        V5_8_8_SNAPSHOT,
        V5_8_8,

        V5_8_9_SNAPSHOT,
        V5_8_9,

        V5_9_0_SNAPSHOT,
        V5_9_0,

        V5_9_1_SNAPSHOT,
        V5_9_1,

        V5_9_2_SNAPSHOT,
        V5_9_2,

        V5_9_3_SNAPSHOT,
        V5_9_3,

        V5_9_4_SNAPSHOT,
        V5_9_4,

        V5_9_5_SNAPSHOT,
        V5_9_5,

        V5_9_6_SNAPSHOT,
        V5_9_6,

        V5_9_7_SNAPSHOT,
        V5_9_7,

        V5_9_8_SNAPSHOT,
        V5_9_8,

        V5_9_9_SNAPSHOT,
        V5_9_9,

        HIGHER_VERSION
    }
}
