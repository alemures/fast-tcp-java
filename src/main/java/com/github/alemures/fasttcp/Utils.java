package com.github.alemures.fasttcp;

import java.util.List;

class Utils {
    private static final byte CHAR_CODE_OPEN_BRACKET = 91;
    private static final byte CHAR_CODE_OPEN_BRACE = 123;

    static byte[] int48ToByteArray(long value) {
        return new byte[]{
                (byte) value,
                (byte) (value >> 8),
                (byte) (value >> 16),
                (byte) (value >> 24),
                (byte) (value >> 32),
                (byte) (value >> 40)};
    }

    static byte[] longToByteArray(long value) {
        return new byte[]{
                (byte) value,
                (byte) (value >> 8),
                (byte) (value >> 16),
                (byte) (value >> 24),
                (byte) (value >> 32),
                (byte) (value >> 40),
                (byte) (value >> 48),
                (byte) (value >> 56)};
    }

    static byte[] doubleToByteArray(double value) {
        return longToByteArray(Double.doubleToRawLongBits(value));
    }

    static void writeInt48(long value, byte[] buffer, int offset) {
        buffer[offset] = (byte) value;
        buffer[offset + 1] = (byte) (value >> 8);
        buffer[offset + 2] = (byte) (value >> 16);
        buffer[offset + 3] = (byte) (value >> 24);
        buffer[offset + 4] = (byte) (value >> 32);
        buffer[offset + 5] = (byte) (value >> 40);
    }

    static void writeInt(long value, byte[] buffer, int offset) {
        buffer[offset] = (byte) value;
        buffer[offset + 1] = (byte) (value >> 8);
        buffer[offset + 2] = (byte) (value >> 16);
        buffer[offset + 3] = (byte) (value >> 24);
    }

    static void writeShort(long value, byte[] buffer, int offset) {
        buffer[offset] = (byte) value;
        buffer[offset + 1] = (byte) (value >> 8);
    }

    static long readLong(byte[] buffer, int offset) {
        return (buffer[offset] & 0xFFL) |
                (buffer[offset + 1] & 0xFFL) << 8 |
                (buffer[offset + 2] & 0xFFL) << 16 |
                (buffer[offset + 3] & 0xFFL) << 24 |
                (buffer[offset + 4] & 0xFFL) << 32 |
                (buffer[offset + 5] & 0xFFL) << 40 |
                (buffer[offset + 6] & 0xFFL) << 48 |
                (buffer[offset + 7] & 0xFFL) << 56;
    }

    static double readDouble(byte[] buffer, int offset) {
        return Double.longBitsToDouble(readLong(buffer, offset));
    }

    static long readInt48(byte[] buffer, int offset) {
        return (buffer[offset] & 0xFFL) |
                (buffer[offset + 1] & 0xFFL) << 8 |
                (buffer[offset + 2] & 0xFFL) << 16 |
                (buffer[offset + 3] & 0xFFL) << 24 |
                (buffer[offset + 4] & 0xFFL) << 32 |
                (buffer[offset + 5] & 0xFFL) << 40;
    }

    static int readInt(byte[] buffer, int offset) {
        return (buffer[offset] & 0xFF) |
                (buffer[offset + 1] & 0xFF) << 8 |
                (buffer[offset + 2] & 0xFF) << 16 |
                (buffer[offset + 3] & 0xFF) << 24;
    }

    static short readShort(byte[] buffer, int offset) {
        return (short) ((buffer[offset] & 0xFF) |
                (buffer[offset + 1] & 0xFF) << 8);
    }

    static boolean isJsonObject(byte[] buffer) {
        return buffer[0] == CHAR_CODE_OPEN_BRACE;
    }

    static boolean isJsonArray(byte[] buffer) {
        return buffer[0] == CHAR_CODE_OPEN_BRACKET;
    }

    static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte bytee : bytes) {
            sb.append(Integer.toString(bytee & 0xFF, 16));
            sb.append(" ");
        }
        return sb.toString();
    }

    static String join(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            sb.append(list.get(i)).append(i < size - 1 ? delimiter : "");
        }
        return sb.toString();
    }
}
