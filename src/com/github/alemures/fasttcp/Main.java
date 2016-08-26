package com.github.alemures.fasttcp;

import java.io.IOException;

import org.json.JSONObject;

public class Main {
	public static void main(String[] args) throws IOException {
		final Socket socket = new Socket("localhost", 5000);

		socket.emit("event1", "Hello, World!");
		socket.emit("event1", 123.23);
		socket.emit("event1", 1121);
		socket.emit("event1", new byte[] { 1, 2, 3, 4 });
		socket.emit("event1", new JSONObject("{\"key\":\"value\"}"));

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
				switch (event) {
				case "string":
					System.out.println("fast-tcp onMessage: " + event + " -> "
							+ data);
					break;
				case "double":
					System.out.println("fast-tcp onMessage: " + event + " -> "
							+ data);
					break;
				case "long":
					System.out.println("fast-tcp onMessage: " + event + " -> "
							+ data);
					break;
				case "buffer":
					System.out.println("fast-tcp onMessage: " + event + " -> "
							+ Utils.byteArrayToLiteralString((byte[]) data));
					break;
				case "json":
					System.out.println("fast-tcp onMessage: " + event + " -> "
							+ data);
					break;
				default:
					System.out.println("fast-tcp onMessage: " + event + " -> "
							+ data);
				}
			}
		});
	}
}
