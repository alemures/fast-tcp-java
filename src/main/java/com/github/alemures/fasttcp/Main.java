package com.github.alemures.fasttcp;

import org.json.JSONObject;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5000);

        socket.emit("event1", "Hello, World!");
        socket.emit("event1", 123.23);
        socket.emit("event1", 1121);
        socket.emit("event1", new byte[]{1, 2, 3, 4});
        socket.emit("event1", new JSONObject("{\"key\":\"value\"}"));

        socket.emit("event2", "Hello", (data) -> System.out.println("ACK " + data));

        socket.setEventListener(new Socket.EventListener() {
            @Override
            public void onEnd() {
                System.out.println("fast-tcp onEnd");
            }

            @Override
            public void onError(Throwable err) {
                System.out.println("fast-tcp onError: " + err.getMessage());
            }

            @Override
            public void onClose() {
                System.out.println("fast-tcp onClose");
            }

            @Override
            public void onSocketConnect() {
                System.out.println("fast-tcp onSocketConnected");
            }

            @Override
            public void onConnect() {
                System.out.println("fast-tcp onConnect");
            }

            @Override
            public void onReconnecting() {
                System.out.println("fast-tcp onReconnecting");
            }

            @Override
            public void onMessage(String event, Object data, Socket.Ack cb) {
                System.out.println(event + " " + data);
                cb.call("Hello");
            }

            @Override
            public void onMessage(String event, Object data) {
                switch (event) {
                    case "string":
                        System.out.println("fast-tcp onMessage: " + event + " -> " + data);
                        break;
                    case "double":
                        System.out.println("fast-tcp onMessage: " + event + " -> " + data);
                        break;
                    case "long":
                        System.out.println("fast-tcp onMessage: " + event + " -> " + data);
                        break;
                    case "buffer":
                        System.out.println("fast-tcp onMessage: " + event + " -> " + Utils.byteArrayToLiteralString((byte[]) data));
                        break;
                    case "json":
                        System.out.println("fast-tcp onMessage: " + event + " -> " + data);
                        break;
                    default:
                        System.out.println("fast-tcp onMessage: " + event + " -> " + data);
                }
            }
        });
    }
}
