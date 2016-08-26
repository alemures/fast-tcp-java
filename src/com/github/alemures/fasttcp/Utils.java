package com.github.alemures.fasttcp;

class Utils {
	public static byte[] int48ToByteArray(long value) {
		return new byte[] {
				(byte) value,
				(byte) (value >> 8),
				(byte) (value >> 16),
				(byte) (value >> 24),
				(byte) (value >> 32),
				(byte) (value >> 40) };
	}

	public static byte[] longToByteArray(long value) {
		return new byte[] { 
				(byte) value,
				(byte) (value >> 8),
				(byte) (value >> 16),
				(byte) (value >> 24),
				(byte) (value >> 32),
				(byte) (value >> 40),
				(byte) (value >> 48),
				(byte) (value >> 56) };
	}

	public static byte[] doubleToByteArray(double value) {
		return longToByteArray(Double.doubleToRawLongBits(value));
	}

	public static void writeInt48(long value, byte[] buffer, int offset) {
		buffer[offset] = (byte) value;
		buffer[offset + 1] = (byte) (value >> 8);
		buffer[offset + 2] = (byte) (value >> 16);
		buffer[offset + 3] = (byte) (value >> 24);
		buffer[offset + 4] = (byte) (value >> 32);
		buffer[offset + 5] = (byte) (value >> 40);
	}
	
	public static void writeInt(long value, byte[] buffer, int offset) {
		buffer[offset] = (byte) value;
		buffer[offset + 1] = (byte) (value >> 8);
		buffer[offset + 2] = (byte) (value >> 16);
		buffer[offset + 3] = (byte) (value >> 24);
	}
	
	public static void writeShort(long value, byte[] buffer, int offset) {
		buffer[offset] = (byte) value;
		buffer[offset + 1] = (byte) (value >> 8);
	}
	
	public static long readLong(byte[] buffer, int offset) {
		return (buffer[offset] & 0xFFL) |
				(buffer[offset + 1] & 0xFFL) << 8 |
				(buffer[offset + 2] & 0xFFL) << 16 |
				(buffer[offset + 3] & 0xFFL) << 24 |
				(buffer[offset + 4] & 0xFFL) << 32 |
				(buffer[offset + 5] & 0xFFL) << 40 |
				(buffer[offset + 6] & 0xFFL) << 48 |
				(buffer[offset + 7] & 0xFFL) << 56;
	}
	
	public static double readDouble(byte[] buffer, int offset) {
		return Double.longBitsToDouble(readLong(buffer, offset));
	}
	
	public static long readInt48(byte[] buffer, int offset) {
		return (buffer[offset] & 0xFFL) |
				(buffer[offset + 1] & 0xFFL) << 8 |
				(buffer[offset + 2] & 0xFFL) << 16 |
				(buffer[offset + 3] & 0xFFL) << 24 |
				(buffer[offset + 4] & 0xFFL) << 32 |
				(buffer[offset + 5] & 0xFFL) << 40;
	}
	
	public static int readInt(byte[] buffer, int offset) {
		return (buffer[offset] & 0xFF) |
				(buffer[offset + 1] & 0xFF) << 8 |
				(buffer[offset + 2] & 0xFF) << 16 |
				(buffer[offset + 3] & 0xFF) << 24;
	}

	public static short readShort(byte[] buffer, int offset) {
		return (short)((buffer[offset] & 0xFF) |
				(buffer[offset + 1] & 0xFF) << 8);
	}
	
	public static String byteArrayToLiteralString(byte[] array) {
        StringBuilder sb = new StringBuilder("[ ");
        for (byte bytee : array) {
            sb.append(Integer.toString(bytee & 0xff, 16));
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
