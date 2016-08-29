package com.github.alemures.fasttcp;

class Message {
    final String event;
    final Object data;
    final int messageId;
    final byte mt;
    final byte dt;

    Message(String event, Object data, int messageId, byte mt, byte dt) {
        this.event = event;
        this.data = data;
        this.messageId = messageId;
        this.mt = mt;
        this.dt = dt;
    }
}
