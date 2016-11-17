package com.github.alemures.fasttcp;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.management.RuntimeErrorException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

class Serializer {
    static final byte VERSION = 1;

    static final byte DT_TEXT = 1;
    static final byte DT_BINARY = 2;
    static final byte DT_INTEGER = 3;
    static final byte DT_DECIMAL = 4;
    static final byte DT_JSON = 5;

    static final byte MT_REGISTER = 1;
    static final byte MT_DATA = 2;
    static final byte MT_DATA_TO_SOCKET = 3;
    static final byte MT_DATA_TO_ROOM = 4;
    static final byte MT_DATA_BROADCAST = 5;
    static final byte MT_DATA_WITH_ACK = 6;
    static final byte MT_ACK = 7;
    static final byte MT_JOIN_ROOM = 8;
    static final byte MT_LEAVE_ROOM = 9;
    static final byte MT_LEAVE_ALL_ROOMS = 10;

    static byte[] serialize(byte[] event, byte[] data, byte mt, byte dt, int messageId) {
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

    static Message deserialize(byte[] buffer, Message message) throws UnsupportedEncodingException {
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
            case DT_TEXT:
                return message.setAndGet(event, deserializeDataAsString(buffer, offset, dataLength), messageId, mt, dt);
            case DT_BINARY:
                return message.setAndGet(event, deserializeDataAsByteArray(buffer, offset, dataLength), messageId, mt, dt);
            case DT_JSON:
                if (Utils.isJsonObject(buffer, offset)) {
                    return message.setAndGet(event, deserializeDataAsJsonObject(buffer, offset, dataLength), messageId, mt, dt);
                } else if (Utils.isJsonArray(buffer, offset)) {
                    return message.setAndGet(event, deserializeDataAsJsonArray(buffer, offset, dataLength), messageId, mt, dt);
                } else {
                    throw new RuntimeErrorException(new Error("Invalid json"));
                }
            case DT_INTEGER:
                return message.setAndGet(event, Utils.readInt48(buffer, offset), messageId, mt, dt);
            case DT_DECIMAL:
                return message.setAndGet(event, Utils.readDouble(buffer, offset), messageId, mt, dt);
            default:
                throw new RuntimeErrorException(new Error("Invalid data type: " + dt));
        }
    }

    private static byte[] deserializeDataAsByteArray(byte[] buffer, int offset, int dataLength) {
        return Arrays.copyOfRange(buffer, offset, offset + dataLength);
    }

    private static String deserializeDataAsString(byte[] buffer, int offset, int dataLength) throws UnsupportedEncodingException {
        return new String(deserializeDataAsByteArray(buffer, offset, dataLength), "UTF-8");
    }

    private static JSONObject deserializeDataAsJsonObject(byte[] buffer, int offset, int dataLength) throws UnsupportedEncodingException {
        return new JSONObject(deserializeDataAsString(buffer, offset, dataLength));
    }

    private static JSONArray deserializeDataAsJsonArray(byte[] buffer, int offset, int dataLength) throws UnsupportedEncodingException {
        return new JSONArray(deserializeDataAsString(buffer, offset, dataLength));
    }
}
