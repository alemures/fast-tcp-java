package com.github.alemures.fasttcp;

public class EventAdapter implements EventListener {
    public void onConnect() {
    }

    public void onSocketConnect() {
    }

    public void onError(Throwable throwable) {
    }

    public void onClose() {
    }

    public void onReconnecting() {
    }

    public void onString(String event, String data) {
    }

    public void onString(String event, String data, Socket.Ack ack) {
    }

    public void onBinary(String event, byte[] data) {
    }

    public void onBinary(String event, byte[] data, Socket.Ack ack) {
    }

    public void onInteger(String event, long data) {
    }

    public void onInteger(String event, long data, Socket.Ack ack) {
    }

    public void onDecimal(String event, double data) {
    }

    public void onDecimal(String event, double data, Socket.Ack ack) {
    }

    public void onObject(String event, Object data) {
    }

    public void onObject(String event, Object data, Socket.Ack ack) {
    }

    public void onBoolean(String event, boolean data) {
    }

    public void onBoolean(String event, boolean data, Socket.Ack ack) {
    }

    public void onEmpty(String event) {
    }

    public void onEmpty(String event, Socket.Ack ack) {
    }
}
