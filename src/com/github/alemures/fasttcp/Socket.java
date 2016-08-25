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
	
	private boolean connected;
	private java.net.Socket socket;
	private BufferedInputStream bufferedInputStream;
	private BufferedOutputStream bufferedOutputStream;
	public String id;
	private int messageId = 1;
	private boolean userEnded = false;
	private Listener listener;
	
	private LinkedList<byte[]> queue;
	
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
					if (listener != null) {
						listener.onSocketConnect();
					}
					
					try {
						flushQueue();
					} catch (Exception e) {
						listener.onError(e);
					}
				}
				@Override
				public void onFailure(Throwable failure) {
					if (listener != null) {
						listener.onError(failure);
						listener.onClose();
					}
					
					if (opts.reconnect && !userEnded) {
						reconnect();
					}
				}
			});
		}
	}
	
	private void reconnect() {
		futureExecutor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Thread.sleep(opts.reconnectInterval);
				connect();
				return null;
			}
		});
	}
	
	public void end() {
		destroy();
	}

	public void destroy() {
		if (connected) {
			userEnded = true;
			futureExecutor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					socket.close();
					return null;
				}
			}).addCallback(new FutureCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					if (listener != null) {
						listener.onEnd();
						listener.onClose();
					}
					
					executorService.shutdown();
				}
				@Override
				public void onFailure(Throwable failure) {
					listener.onError(failure);
				}
			});
		}
	}
	
	public int getVersion() {
		return Serializer.VERSION;
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	public void emit(String event, String data) throws IOException {
		send(Serializer.serialize(event, data, Serializer.MT_DATA, nextMessageId()));
	}

	public void emit(String event, long data) throws IOException {
		send(Serializer.serialize(event, data, Serializer.MT_DATA, nextMessageId()));
	}

	public void emit(String event, double data) throws IOException {
		send(Serializer.serialize(event, data, Serializer.MT_DATA, nextMessageId()));
	}

	public void emit(String event, JSONObject data) throws IOException {
		send(Serializer.serialize(event, data, Serializer.MT_DATA, nextMessageId()));
	}

	public void emit(String event, byte[] data) throws IOException {
		send(Serializer.serialize(event, data, Serializer.MT_DATA, nextMessageId()));
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
	
	private void send(byte[] data) throws IOException {
		if (connected) {
			bufferedOutputStream.write(data);
			bufferedOutputStream.flush();
		} else {
			if (queue.size() + 1 > opts.queueSize) {
				queue.poll();
			}
			queue.offer(data);
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
				if (listener != null) {
					listener.onError(e);
				}
			}
			
			if (opts.reconnect && !userEnded) {
				reconnect();
			}
			
			if (listener != null) {
				listener.onEnd();
				listener.onClose();
			}
			
			connected = false;
		}
		
		public void process(Message message) {
			switch (message.mt) {
			case Serializer.MT_DATA:
				if (listener != null) {
					listener.onMessage(message.event, message.data);
				}
				break;
			case Serializer.MT_REGISTER:
				id = (String) message.data;
				connected = true;
				if (listener != null) {
					listener.onConnect();
				}
				break;
			default:
				throw new RuntimeException("Not implemented message type " + message.mt);
			}
		}
	}
	
	static abstract class Listener {
		public void onSocketConnect() {}
		public void onEnd() {}
		public void onClose() {}
		public void onError(Throwable err) {}
		public void onConnect() {}
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
