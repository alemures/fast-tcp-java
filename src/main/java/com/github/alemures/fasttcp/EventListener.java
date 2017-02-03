package com.github.alemures.fasttcp;

public interface EventListener {
    void onConnect();

    void onSocketConnect();

    void onError(Throwable throwable);

    void onClose();

    void onReconnecting();

    void onString(String event, String data);

    void onString(String event, String data, Socket.Ack ack);

    void onBinary(String event, byte[] data);

    void onBinary(String event, byte[] data, Socket.Ack ack);

    void onInteger(String event, long data);

    void onInteger(String event, long data, Socket.Ack ack);

    void onDecimal(String event, double data);

    void onDecimal(String event, double data, Socket.Ack ack);

    void onObject(String event, Object data);

    void onObject(String event, Object data, Socket.Ack ack);

    void onBoolean(String event, boolean data);

    void onBoolean(String event, boolean data, Socket.Ack ack);

    void onEmpty(String event);

    void onEmpty(String event, Socket.Ack ack);
}
