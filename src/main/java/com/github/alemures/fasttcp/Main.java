package com.github.alemures.fasttcp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

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

        socket.on("end", (args) -> System.out.println("fast-tcp end"));
        socket.on("error", (args) -> System.out.println("fast-tcp error " + args[0]));
        socket.on("close", (args) -> System.out.println("fast-tcp close"));
        socket.on("socket_connect", (args) -> System.out.println("fast-tcp socket_connected"));
        socket.on("connect", (args) -> System.out.println("fast-tcp connect"));
        socket.on("reconnecting", (args) -> System.out.println("fast-tcp reconnecting"));

        socket.on("string", (args) -> System.out.println(args[0]));
        socket.on("double", (args) -> System.out.println(args[0]));
        socket.on("int", (args) -> System.out.println(args[0]));
        socket.on("buffer", (args) -> System.out.println(Utils.byteArrayToLiteralString((byte[]) args[0])));
        socket.on("object", (args) -> System.out.println(args[0]));

        socket.on("array", (args) -> System.out.println(args[0]));
        socket.on("div", (args) -> {
            JSONObject numbers = (JSONObject) args[0];
            Socket.Ack ack = (Socket.Ack) args[1];

            ack.send(numbers.getDouble("a") / numbers.getDouble("b"));
        });

        socket.connect();
    }

    public static void socketPerformance() throws IOException {
        Socket socket = new Socket("localhost", 5000);
        String temp = "";

        for (int i = 0; i < 10000; i++) {
            temp += "a";
        }

        final String data = temp;

        socket.on("connect", (args) -> {
            try {
                for(int i = 0; i < 100000; i++) {
                    socket.emit("data", data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        socket.connect();
    }
}
