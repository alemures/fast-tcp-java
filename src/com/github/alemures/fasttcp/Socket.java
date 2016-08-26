package com.github.alemures.fasttcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import com.github.alemures.fasttcp.internal.FutureCallback;
import com.github.alemures.fasttcp.internal.FutureExecutor;

public class Socket {
	private static final int MAX_MESSAGE_ID = Integer.MAX_VALUE;
	
	private String host;
	private int port;
	private Opts opts;
	
	private ExecutorService executorService;
	private FutureExecutor futureExecutor;
	
	private java.net.Socket socket;
	private boolean connected;
	private boolean manuallyClosed;
	private int messageId = 1;
	private BufferedInputStream bufferedInputStream;
	private BufferedOutputStream bufferedOutputStream;
	
	private EventListener eventListener;
	
	private LinkedList<byte[]> queue;
	
	public String id;
	
	public Socket(String host, int port) {
		this(host, port, new Opts());
	}
	
	public Socket(String host, int port, Opts opts) {
		this.host = host;
		this.port = port;
		this.opts = opts;
		
		if (opts.autoConnect) {
			connect();
		}
		
		if (opts.useQueue) {
			queue = new LinkedList<>();
		}
	}
	
	public void connect() {
		if (!connected) {
			manuallyClosed = false;
			executorService = Executors.newSingleThreadExecutor();
			futureExecutor = new FutureExecutor(executorService);
			futureExecutor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					socket = new java.net.Socket(host, port);
					bufferedInputStream = new BufferedInputStream(socket.getInputStream());
					bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
					new SocketReceiverThread().start();
					return null;
				}
			}).addCallback(new FutureCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					connected = true;
					
					try {
						flushQueue();
					} catch (Exception e) {
						eventListener.onError(e);
					}
					
					if (eventListener != null) {
						eventListener.onSocketConnect();
					}
				}
				@Override
				public void onFailure(Throwable failure) {
					if (eventListener != null) {
						eventListener.onError(failure);
						eventListener.onClose();
					}
					
					if (opts.reconnect && !manuallyClosed) {
						reconnect();
					}
				}
			});
		}
	}
	
	
	
	public void end() {
		destroy();
	}

	public void destroy() {
		if (connected) {
			manuallyClosed = true;
			futureExecutor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					socket.close();
					return null;
				}
			}).addCallback(new FutureCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					if (eventListener != null) {
						eventListener.onEnd();
						eventListener.onClose();
					}
					
					executorService.shutdown();
				}
				@Override
				public void onFailure(Throwable failure) {
					eventListener.onError(failure);
				}
			});
		}
	}
	
	public void emit(String event, String data) throws IOException {
		send(event, data.getBytes(), Serializer.MT_DATA, Serializer.DT_STRING);
	}

	public void emit(String event, long data) throws IOException {
		send(event, Utils.int48ToByteArray(data), Serializer.MT_DATA, Serializer.DT_INT);
	}

	public void emit(String event, double data) throws IOException {
		send(event, Utils.doubleToByteArray(data), Serializer.MT_DATA, Serializer.DT_DOUBLE);
	}

	public void emit(String event, JSONObject data) throws IOException {
		send(event, data.toString().getBytes(), Serializer.MT_DATA, Serializer.DT_OBJECT);
	}

	public void emit(String event, byte[] data) throws IOException {
		send(event, data, Serializer.MT_DATA, Serializer.DT_BUFFER);
	}
	
	public void join(String room) throws IOException {
		send("", room.getBytes(), Serializer.MT_JOIN_ROOM, Serializer.DT_STRING);
	}
	
	public void leave(String room) throws IOException {
		send("", room.getBytes(), Serializer.MT_LEAVE_ROOM, Serializer.DT_STRING);
	}
	
	public void leaveAll() throws IOException {
		send("", new byte[0], Serializer.MT_LEAVE_ALL_ROOMS, Serializer.DT_STRING);
	}
	
	private void reconnect() {
		futureExecutor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Thread.sleep(opts.reconnectInterval);
				if (eventListener != null) {
					eventListener.onReconnecting();
				}
				connect();
				return null;
			}
		});
	}
	
	public int getVersion() {
		return Serializer.VERSION;
	}
	
	public void setEventListener(EventListener listener) {
		this.eventListener = listener;
	}
 	
	private void flushQueue() throws IOException {
		if (queue.size() == 0) {
			return;
		}
		
		for (byte[] data: queue) {
			bufferedOutputStream.write(data);
		}
		bufferedOutputStream.flush();
		
		queue.clear();
	}
	
	private void send(String event, byte[] data, byte mt, byte dt) throws IOException {
		byte[] message = Serializer.serialize(event, data, mt, dt, nextMessageId());
		
		if (connected) {
			bufferedOutputStream.write(message);
			bufferedOutputStream.flush();
		} else {
			if (queue.size() + 1 > opts.queueSize) {
				queue.poll();
			}
			queue.offer(message);
		}
	}
	
	private int nextMessageId() {
		if (++messageId >= MAX_MESSAGE_ID) {
			messageId = 1;
		}
		
		return messageId;
	}
	
	private class SocketReceiverThread extends Thread {
		Reader reader = new Reader();
		byte[] chunk = new byte[1024];
		
		@Override
		public void run() {
			int bytesRead;
			try {
				while((bytesRead = bufferedInputStream.read(chunk)) != -1) {
					ArrayList<byte[]> buffers = reader.read(Arrays.copyOfRange(chunk, 0, bytesRead));
					for (byte[] buffer: buffers) {
						process(Serializer.deserialize(buffer));
					}
				}
				
			} catch (IOException e) {
				if (eventListener != null) {
					eventListener.onError(e);
				}
			}
			
			if (opts.reconnect && !manuallyClosed) {
				reconnect();
			}
			
			if (eventListener != null) {
				eventListener.onEnd();
				eventListener.onClose();
			}
			
			connected = false;
		}
		
		public void process(Message message) {
			switch (message.mt) {
			case Serializer.MT_DATA:
				if (eventListener != null) {
					eventListener.onMessage(message.event, message.data);
				}
				break;
			case Serializer.MT_REGISTER:
				id = (String) message.data;
				if (eventListener != null) {
					eventListener.onConnect();
				}
				break;
			default:
				throw new RuntimeException("Not implemented message type " + message.mt);
			}
		}
	}
	
	static abstract class EventListener {
		public void onSocketConnect() {}
		public void onEnd() {}
		public void onClose() {}
		public void onError(Throwable err) {}
		public void onConnect() {}
		public void onReconnecting() {}
		public void onMessage(String event, Object data) {}
	}
	
	static class Opts {
		private boolean reconnect = true;
		private long reconnectInterval = 1000;
		private boolean autoConnect = true;
		private boolean useQueue = true;
		private int queueSize = 1024;

		public Opts reconnect(boolean reconnect) {
			this.reconnect = reconnect;
			return this;
		}
		
		public Opts reconnectInterval(long reconnectInterval) {
			this.reconnectInterval = reconnectInterval;
			return this;
		}
		
		public Opts autoConnect(boolean autoConnect) {
			this.autoConnect = autoConnect;
			return this;
		}
		
		public Opts useQueue(boolean useQueue) {
			this.useQueue = useQueue;
			return this;
		}
		
		public Opts queueSize(int queueSize) {
			this.queueSize = queueSize;
			return this;
		}
	}
}
