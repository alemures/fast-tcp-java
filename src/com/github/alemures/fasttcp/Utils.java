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
		long l = Double.doubleToRawLongBits(value);
		return longToByteArray(l);
	}

	public static long int48FromByteArray(byte[] b) {
		return int48FromBytes(b[0], b[1], b[2], b[3], b[4], b[5]);
	}

	public static long int48FromBytes(byte b1, byte b2, byte b3, byte b4, byte b5,
			byte b6) {
		return (b1 & 0xFFL) |
				(b2 & 0xFFL) << 8 |
				(b3 & 0xFFL) << 16 |
				(b4 & 0xFFL) << 24 |
				(b5 & 0xFFL) << 32 |
				(b6 & 0xFFL) << 40;
	}

	public static void writeInt48ToBuffer(long value, byte[] buffer, int offset) {
		buffer[offset] = (byte) value;
		buffer[offset + 1] = (byte) (value >> 8);
		buffer[offset + 2] = (byte) (value >> 16);
		buffer[offset + 3] = (byte) (value >> 24);
		buffer[offset + 4] = (byte) (value >> 32);
		buffer[offset + 5] = (byte) (value >> 40);
	}

	public static void copyBuffer(byte[] source, byte[] target, int targetStart,
			int sourceStart, int sourceEnd) {
		for (int i = 0; i < sourceEnd - sourceStart; i++) {
			target[targetStart + i] = source[sourceStart + i];
		}
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
