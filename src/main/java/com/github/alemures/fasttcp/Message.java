package com.github.alemures.fasttcp;

import java.io.UnsupportedEncodingException;

class Message {
    String event;
    byte[] data;
    int messageId;
    byte mt;
    byte dt;

    Message() {
    }

    Message setErrorAndGet(String data) {
        return setAndGet(null, data.getBytes(), Serializer.MT_ERROR, Serializer.DT_STRING, 0);
    }

    Message setAndGet(String event, byte[] data, byte mt, byte dt, int messageId) {
        this.event = event;
        this.data = data;
        this.messageId = messageId;
        this.mt = mt;
        this.dt = dt;
        return this;
    }

    String getDataAsString() throws UnsupportedEncodingException {
        return new String(data, "UTF-8");
    }

    long getDataAsInteger() {
        return Utils.readInt48(data, 0);
    }

    double getDataAsDecimal() {
        return Utils.readDouble(data, 0);
    }

    boolean getDataAsBoolean() {
        return Utils.readBoolean(data, 0);
    }
}
