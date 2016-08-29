package com.github.alemures.fasttcp;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.management.RuntimeErrorException;

import org.json.JSONObject;

class Serializer {
	public static final byte VERSION = 1;

	public static final byte DT_STRING = 1;
	public static final byte DT_BUFFER = 2;
	public static final byte DT_INT = 3;
	public static final byte DT_DOUBLE = 4;
	public static final byte DT_OBJECT = 5;

	public static final byte MT_REGISTER = 1;
	public static final byte MT_DATA = 2;
//	public static final byte MT_DATA_TO_SOCKET = 3;
//	public static final byte MT_DATA_TO_ROOM = 4;
//	public static final byte MT_DATA_BROADCAST = 5;
	public static final byte MT_DATA_WITH_ACK = 6;
	public static final byte MT_ACK = 7;
	public static final byte MT_JOIN_ROOM = 8;
	public static final byte MT_LEAVE_ROOM = 9;
	public static final byte MT_LEAVE_ALL_ROOMS = 10;

	public static byte[] serialize(byte[] event, byte[] data, byte mt, byte dt, int messageId) {
		short eventLength = (short) event.length;
		int dataLength = data.length;
		int messageLength = 8 + 2 + eventLength + 4 + dataLength;

		byte[] buffer = new byte[4 + messageLength];
		int offset = 0;
		
		Utils.writeInt(messageLength, buffer, offset);
		offset += 4;
		
		buffer[offset++] = VERSION;
		buffer[offset++] = 0;
		buffer[offset++] = dt;
		buffer[offset++] = mt;
	
		Utils.writeInt(messageId, buffer, offset);
		offset += 4;
		
		Utils.writeShort(eventLength, buffer, offset);
		offset += 2;
		
		System.arraycopy(event, 0, buffer, offset, eventLength);
		offset += eventLength;
		
		Utils.writeInt(dataLength, buffer, offset);
		offset += 4;
		
		System.arraycopy(data, 0, buffer, offset, dataLength);

		return buffer;
	}

	public static Message deserialize(byte[] buffer) throws UnsupportedEncodingException {
		int offset = 4;
				
		byte version = buffer[offset++];
		if (version != VERSION) {
			throw new RuntimeException("Message version " + version
					+ " and Serializer version " + VERSION + " doesn\'t match");
		}
		
		offset++; // Skip unused flags
		byte dt = buffer[offset++];
		byte mt = buffer[offset++];
		
		int messageId = Utils.readInt(buffer, offset);
		offset += 4;
		
		short eventLength = Utils.readShort(buffer, offset);
		offset += 2;
		
		byte[] eventBytes = Arrays.copyOfRange(buffer, offset, offset + eventLength);
		String event = new String(eventBytes, "UTF-8");
		offset += eventLength;

		int dataLength = Utils.readInt(buffer, offset);
		offset += 4;

		switch (dt) {
		case DT_STRING:
			byte[] dataBytes = Arrays.copyOfRange(buffer, offset, offset + dataLength);
			return new Message(event, new String(dataBytes, "UTF-8"), messageId, mt, dt);
		case DT_BUFFER:
			byte[] dataBytes2 = Arrays.copyOfRange(buffer, offset, offset + dataLength);
			return new Message(event, dataBytes2, messageId, mt, dt);
		case DT_OBJECT:
			byte[] dataBytes3 = Arrays.copyOfRange(buffer, offset, offset + dataLength);
			return new Message(event, new JSONObject(new String(dataBytes3, "UTF-8")), messageId, mt, dt);
		case DT_INT:
			return new Message(event, Utils.readInt48(buffer, offset), messageId, mt, dt);
		case DT_DOUBLE:
			return new Message(event, Utils.readDouble(buffer, offset), messageId, mt, dt);
		default:
			throw new RuntimeErrorException(new Error("Invalid data type: " + dt));
		}
	}
}
