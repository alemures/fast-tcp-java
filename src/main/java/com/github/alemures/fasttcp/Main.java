package com.github.alemures.fasttcp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        socket();
//        socketPerformance();
    }

    public static void socket() throws IOException {
        Socket socket = new Socket("localhost", 5000);

        socket.emit("string", "Hello, World!");
        socket.emit("double", 123.23);
        socket.emit("int", 1121);
        socket.emit("buffer", new byte[]{1, 2, 3, 4});
        socket.emit("object", new JSONObject("{\"key\":\"value\"}"));

        socket.emit("sum-with-cb", new JSONObject().put("n1", 5).put("n2", 13), (data) -> System.out.println("Result: " + data));

        socket.emit("array", new JSONArray().put(1).put(2).put(3));

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
            public void onMessage(String event, Object data) {
                System.out.println("fast-tcp onMessage: " + event + " -> " + data);
            }

            @Override
            public void onMessage(String event, Object data, Socket.Ack ack) {
                if (event.equals("div")) {
                    JSONObject o = (JSONObject) data;
                    ack.send(o.getDouble("a") / o.getDouble("b"));
                    return;
                }
                System.out.println("fast-tcp onMessage with ack: "  + event + " -> " + data);
                ack.send(1234);
            }
        });
    }

    public static void socketPerformance() throws IOException {
        Socket socket = new Socket("localhost", 5000, new Socket.Opts().autoConnect(false));
        String temp = "";

        for (int i = 0; i < 10000; i++) {
            temp += "a";
        }

        final String data = temp;

        socket.setEventListener(new Socket.EventListener() {
            @Override
            public void onConnect() {
                try {
                    for(int i = 0; i < 100000; i++) {
                        socket.emit("data", data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.connect();
    }
}
