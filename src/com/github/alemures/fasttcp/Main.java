package com.github.alemures.fasttcp;

import java.io.IOException;

public class Main {
	public static void main(String[] args) {

		final Socket socket = new Socket("localhost", 5000);
		try {
			socket.emit("event1", "Hello, World!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket.setListener(new Socket.Listener() {
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
			public void onMessage(String event, Object data) {
				System.out.println("fast-tcp onMessage: " + event + " -> " + data);
			}
		});
	}
}
