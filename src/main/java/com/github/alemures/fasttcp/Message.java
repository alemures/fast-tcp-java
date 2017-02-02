package com.github.alemures.fasttcp;

import java.io.UnsupportedEncodingException;

class Message {
    byte[] event;
    byte[] data;
    int messageId;
    byte mt;
    byte dt;

    Message() {
    }

    Message setErrorAndGet(String data) {
        return setAndGet(null, data.getBytes(), Serializer.MT_ERROR, Serializer.DT_STRING, 0);
    }

    Message setAndGet(byte[] event, byte[] data, byte mt, byte dt, int messageId) {
        this.event = event;
        this.data = data;
        this.messageId = messageId;
        this.mt = mt;
        this.dt = dt;
        return this;
    }

    String eventToString() throws UnsupportedEncodingException {
        return new String(event, "UTF-8");
    }

    String dataToString() throws UnsupportedEncodingException {
        return new String(data, "UTF-8");
    }

    long dataToInteger() {
        return Utils.readInt48(data, 0);
    }

    double dataToDouble() {
        return Utils.readDouble(data, 0);
    }

    boolean dataToBoolean() {
        return Utils.readBoolean(data, 0);
    }
}
