package com.github.alemures.fasttcp;

final class Message {
	public final String event;
	public final Object data;
	public final int messageId;
	public final byte mt;
	public final byte dt;

	public Message(String event, Object data, int messageId, byte mt, byte dt) {
		this.event = event;
		this.data = data;
		this.messageId = messageId;
		this.mt = mt;
		this.dt = dt;
	}
}
