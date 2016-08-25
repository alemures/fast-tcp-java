package com.github.alemures.fasttcp;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

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
//	public static final byte MT_DATA_WITH_ACK = 6;
//	public static final byte MT_ACK = 7;
//	public static final byte MT_JOIN_ROOM = 8;
//	public static final byte MT_LEAVE_ROOM = 9;
//	public static final byte MT_LEAVE_ALL_ROOMS = 10;
	
	public static byte[] serialize(String event, String data, byte mt, int messageId) {
		return serialize(event, data.getBytes(), mt, messageId, DT_STRING);
	}
	
	public static byte[] serialize(String event, long data, byte mt, int messageId) {
		return serialize(event, Utils.int48ToByteArray(data), mt, messageId, DT_INT);
	}
	
	public static byte[] serialize(String event, double data, byte mt, int messageId) {
		return serialize(event, Utils.doubleToByteArray(data), mt, messageId, DT_DOUBLE);
	}
	
	public static byte[] serialize(String event, JSONObject data, byte mt, int messageId) {
		return serialize(event, data.toString().getBytes(), mt, messageId, DT_OBJECT);
	}
	
	public static byte[] serialize(String event, byte[] data, byte mt, int messageId) {
		return serialize(event, data, mt, messageId, DT_BUFFER);
	}

	private static byte[] serialize(String event, byte[] data, byte mt, int messageId, byte dt) {
		short eventLength = (short) event.length();
		int dataLength = data.length;

		int messageLength = 8 + 2 + eventLength + 4 + dataLength;

		ByteBuffer buff = ByteBuffer.allocate(4 + messageLength);
		buff.order(ByteOrder.LITTLE_ENDIAN);

		buff.putInt(messageLength);
		buff.put(VERSION);
		// Skip flags
		buff.position(buff.position() + 1);
		buff.put(dt);
		buff.put(mt);
		buff.putInt(messageId);
		buff.putShort(eventLength);
		buff.put(event.getBytes());
		buff.putInt(dataLength);
		buff.put(data);

		return buff.array();
	}

	public static Message deserialize(byte[] buffData) throws UnsupportedEncodingException {
		ByteBuffer buff = ByteBuffer.wrap(buffData);
		buff.order(ByteOrder.LITTLE_ENDIAN);

		buff.position(buff.position() + 4);
		
		byte version = buff.get();
		if (version != VERSION) {
			throw new RuntimeException("Message version " + version
					+ " and Serializer version " + VERSION + " doesn\'t match");
		}
		
		// Skip flags
		buff.position(buff.position() + 1);

		byte dt = buff.get();
		byte mt = buff.get();
		int messageId = buff.getInt();
		short eventLength = buff.getShort();

		byte[] eventBytes = new byte[eventLength];
		buff.get(eventBytes, 0, eventLength);
		String event = new String(eventBytes, Charset.forName("UTF-8"));

		int dataLength = buff.getInt();

		switch (dt) {
		case DT_STRING:
			byte[] dataBytes = new byte[dataLength];
			buff.get(dataBytes, 0, dataLength);
			return new Message(event, new String(dataBytes, "UTF-8"), messageId, mt, dt);
		case DT_BUFFER:
			byte[] dataBytes2 = new byte[dataLength];
			buff.get(dataBytes2, 0, dataLength);
			return new Message(event, dataBytes2, messageId, mt, dt);
		case DT_INT:
			byte[] dataBytes3 = new byte[6];
			buff.get(dataBytes3, 0, 6);
			return new Message(event, Utils.int48FromByteArray(dataBytes3), messageId, mt, dt);
		case DT_DOUBLE:
			double dataDouble = buff.getDouble();
			return new Message(event, dataDouble, messageId, mt, dt);
		case DT_OBJECT:
			byte[] dataBytes4 = new byte[dataLength];
			buff.get(dataBytes4, 0, dataLength);
			return new Message(event, new JSONObject(new String(dataBytes4, "UTF-8")), messageId, mt, dt);
		default:
			throw new RuntimeErrorException(new Error("Invalid data type: " + dt));
		}
	}
}
