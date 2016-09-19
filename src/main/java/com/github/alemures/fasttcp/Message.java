package com.github.alemures.fasttcp;

class Message {
    String event;
    Object data;
    int messageId;
    byte mt;
    byte dt;

    Message() {
    }

    Message setAndGet(String event, Object data, int messageId, byte mt, byte dt) {
        this.event = event;
        this.data = data;
        this.messageId = messageId;
        this.mt = mt;
        this.dt = dt;

        return this;
    }
}
