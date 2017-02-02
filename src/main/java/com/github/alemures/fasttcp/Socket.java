package com.github.alemures.fasttcp;

import com.github.alemures.fasttcp.futures.FutureCallback;
import com.github.alemures.fasttcp.futures.FutureExecutor;
import com.github.alemures.fasttcp.futures.ListenableFuture;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Socket {
    private static final int MAX_MESSAGE_ID = Integer.MAX_VALUE;

    public String id;
    private String host;
    private int port;
    private Opts opts;
    private java.net.Socket socket;
    private boolean connected;
    private boolean manuallyClosed;
    private int messageId = 1;
    private BufferedInputStream bufferedInputStream;
    private BufferedOutputStream bufferedOutputStream;
    private EventThread eventThread = new EventThread();
    private LinkedList<byte[]> queue;
    private EventListener eventListener = new EventAdapter() {
        @Override
        public void onError(Throwable throwable) {
            System.err.println("Missing error handler on `Socket`.");
            System.err.println(throwable.getMessage());
        }
    };
    private Map<Integer, EventListener> acks = new HashMap<>();

    public Socket(String host, int port) {
        this(host, port, new Opts());
    }

    public Socket(String host, int port, Opts opts) {
        this.host = host;
        this.port = port;
        this.opts = opts;

        if (opts.useQueue) {
            queue = new LinkedList<>();
        }
    }

    public void connect() {
        if (connected) return;

        manuallyClosed = false;
        eventThread.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                socket = new java.net.Socket();
                socket.connect(new InetSocketAddress(host, port), opts.timeout);
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

                eventListener.onSocketConnect();
            }

            @Override
            public void onFailure(Throwable failure) {
                eventListener.onError(failure);
                eventListener.onClose();

                if (opts.reconnect && !manuallyClosed) {
                    reconnect();
                } else {
                    eventThread.stop();
                }
            }
        });
    }

    public void close() {
        if (!connected) return;

        manuallyClosed = true;
        eventThread.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                socket.close();
                return null;
            }
        }).addCallback(new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                eventListener.onClose();
                eventThread.stop();
            }

            @Override
            public void onFailure(Throwable failure) {
                eventListener.onError(failure);
            }
        });
    }

    public void emit(String event) throws IOException {
        send(event, Utils.EMPTY_BYTE_ARRAY, Serializer.MT_DATA, Serializer.DT_EMPTY);
    }

    public void emit(String event, EmitOpts emitOpts) throws IOException {
        emitTo(event, Utils.EMPTY_BYTE_ARRAY, Serializer.DT_EMPTY, emitOpts);
    }

    public void emit(String event, EventListener cb) throws IOException {
        send(event, Utils.EMPTY_BYTE_ARRAY, Serializer.MT_DATA_WITH_ACK, Serializer.DT_EMPTY, cb);
    }

    public void emit(String event, String data) throws IOException {
        send(event, data.getBytes(), Serializer.MT_DATA, Serializer.DT_STRING);
    }

    public void emit(String event, String data, EmitOpts emitOpts) throws IOException {
        emitTo(event, data.getBytes(), Serializer.DT_STRING, emitOpts);
    }

    public void emit(String event, String data, EventListener cb) throws IOException {
        send(event, data.getBytes(), Serializer.MT_DATA_WITH_ACK, Serializer.DT_STRING, cb);
    }

    public void emit(String event, long data) throws IOException {
        send(event, Utils.int48ToByteArray(data), Serializer.MT_DATA, Serializer.DT_INTEGER);
    }

    public void emit(String event, long data, EmitOpts emitOpts) throws IOException {
        emitTo(event, Utils.int48ToByteArray(data), Serializer.DT_INTEGER, emitOpts);
    }

    public void emit(String event, long data, EventListener cb) throws IOException {
        send(event, Utils.int48ToByteArray(data), Serializer.MT_DATA_WITH_ACK, Serializer.DT_INTEGER, cb);
    }

    public void emit(String event, double data) throws IOException {
        send(event, Utils.doubleToByteArray(data), Serializer.MT_DATA, Serializer.DT_DECIMAL);
    }

    public void emit(String event, double data, EmitOpts emitOpts) throws IOException {
        emitTo(event, Utils.doubleToByteArray(data), Serializer.DT_DECIMAL, emitOpts);
    }

    public void emit(String event, double data, EventListener cb) throws IOException {
        send(event, Utils.doubleToByteArray(data), Serializer.MT_DATA_WITH_ACK, Serializer.DT_DECIMAL, cb);
    }

    public void emit(String event, Object data) throws IOException {
        send(event, opts.objectSerializer.serialize(data), Serializer.MT_DATA, Serializer.DT_OBJECT);
    }

    public void emit(String event, Object data, EmitOpts emitOpts) throws IOException {
        emitTo(event, opts.objectSerializer.serialize(data), Serializer.DT_OBJECT, emitOpts);
    }

    public void emit(String event, Object data, EventListener cb) throws IOException {
        send(event, opts.objectSerializer.serialize(data), Serializer.MT_DATA_WITH_ACK, Serializer.DT_OBJECT, cb);
    }

    public void emit(String event, boolean data) throws IOException {
        send(event, Utils.booleanToByteArray(data), Serializer.MT_DATA, Serializer.DT_BOOLEAN);
    }

    public void emit(String event, boolean data, EmitOpts emitOpts) throws IOException {
        emitTo(event, Utils.booleanToByteArray(data), Serializer.DT_BOOLEAN, emitOpts);
    }

    public void emit(String event, boolean data, EventListener cb) throws IOException {
        send(event, Utils.booleanToByteArray(data), Serializer.MT_DATA_WITH_ACK, Serializer.DT_BOOLEAN, cb);
    }

    public void emit(String event, byte[] data) throws IOException {
        send(event, data, Serializer.MT_DATA, Serializer.DT_BINARY);
    }

    public void emit(String event, byte[] data, EmitOpts emitOpts) throws IOException {
        emitTo(event, data, Serializer.DT_BINARY, emitOpts);
    }

    public void emit(String event, byte[] data, EventListener cb) throws IOException {
        send(event, data, Serializer.MT_DATA_WITH_ACK, Serializer.DT_BINARY, cb);
    }

    public void join(String room) throws IOException {
        send(Utils.EMPTY_STRING, room.getBytes(), Serializer.MT_JOIN_ROOM, Serializer.DT_STRING);
    }

    public void leave(String room) throws IOException {
        send(Utils.EMPTY_STRING, room.getBytes(), Serializer.MT_LEAVE_ROOM, Serializer.DT_STRING);
    }

    public void leaveAll() throws IOException {
        send(Utils.EMPTY_STRING, Utils.EMPTY_BYTE_ARRAY, Serializer.MT_LEAVE_ALL_ROOMS, Serializer.DT_STRING);
    }

    public int getSerializerVersion() {
        return Serializer.VERSION;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    private void flushQueue() throws IOException {
        if (!opts.useQueue || queue.size() == 0) {
            return;
        }

        for (byte[] message : queue) {
            bufferedOutputStream.write(message);
        }
        bufferedOutputStream.flush();

        queue.clear();
    }

    private void reconnect() {
        eventThread.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Thread.sleep(opts.reconnectInterval);
                eventListener.onReconnecting();
                connect();
                return null;
            }
        });
    }

    private void emitTo(String event, byte[] data, byte dt, EmitOpts emitOpts) throws IOException {
        if (emitOpts.broadcast) {
            send(event, data, Serializer.MT_DATA_BROADCAST, dt);
        }

        if (emitOpts.socketIds != null && emitOpts.socketIds.size() > 0) {
            send(Utils.join(emitOpts.socketIds, ",") + "|" + event, data, Serializer.MT_DATA_TO_SOCKET, dt);
        }

        if (emitOpts.rooms != null && emitOpts.rooms.size() > 0) {
            send(Utils.join(emitOpts.rooms, ",") + "|" + event, data, Serializer.MT_DATA_TO_ROOM, dt);
        }
    }

    private void send(String event, byte[] data, byte mt, byte dt) throws IOException {
        send(event, data, mt, dt, nextMessageId());
    }

    private void send(String event, byte[] data, byte mt, byte dt, EventListener cb) throws IOException {
        acks.put(messageId, cb);
        send(event, data, mt, dt, nextMessageId());
    }

    private void send(String event, byte[] data, byte mt, byte dt, int messageId) throws IOException {
        byte[] message = Serializer.serialize(event.getBytes(), data, mt, dt, messageId);

        if (connected) {
            bufferedOutputStream.write(message);
            bufferedOutputStream.flush();
        } else if (opts.useQueue) {
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

    private Ack createAck(final Message message) {
        return new Ack() {
            @Override
            public void send(String data) {
                send(data.getBytes(), Serializer.DT_STRING);
            }

            @Override
            public void send(long data) {
                send(Utils.int48ToByteArray(data), Serializer.DT_INTEGER);
            }

            @Override
            public void send(double data) {
                send(Utils.doubleToByteArray(data), Serializer.DT_DECIMAL);
            }

            @Override
            public void send(Object data) {
                send(opts.objectSerializer.serialize(data), Serializer.DT_OBJECT);
            }

            @Override
            public void send(boolean data) {
                send(Utils.booleanToByteArray(data), Serializer.DT_BOOLEAN);
            }

            @Override
            public void send(byte[] data) {
                send(data, Serializer.DT_BINARY);
            }

            private void send(byte[] data, byte dt) {
                try {
                    Socket.this.send("", data, Serializer.MT_ACK, dt, message.messageId);
                } catch (IOException e) {
                    eventListener.onError(e);
                }
            }
        };
    }

    public interface ObjectSerializer {
        byte[] serialize(Object object);
        Object deserialize(byte[] data);
    }

    public interface Ack {
        void send(String data);

        void send(long data);

        void send(double data);

        void send(Object data);

        void send(boolean data);

        void send(byte[] data);
    }

    public static class EmitOpts {
        private Set<String> socketIds;
        private Set<String> rooms;
        private boolean broadcast;

        public EmitOpts socketIds(Set<String> socketIds) {
            this.socketIds = socketIds;
            return this;
        }

        public EmitOpts rooms(Set<String> rooms) {
            this.rooms = rooms;
            return this;
        }

        public EmitOpts broadcast(boolean broadcast) {
            this.broadcast = broadcast;
            return this;
        }
    }

    public static class Opts {
        private boolean reconnect = true;
        private long reconnectInterval = 1000;
        private boolean useQueue = true;
        private int queueSize = 1024;
        private int timeout = 0; // Disabled by default
        private ObjectSerializer objectSerializer = null;

        public Opts reconnect(boolean reconnect) {
            this.reconnect = reconnect;
            return this;
        }

        public Opts reconnectInterval(long reconnectInterval) {
            this.reconnectInterval = reconnectInterval;
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

        public Opts timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Opts objectSerializer(ObjectSerializer objectSerializer) {
            this.objectSerializer = objectSerializer;
            return this;
        }
    }

    private class EventThread {
        private ExecutorService executorService;
        private FutureExecutor futureExecutor;

        private ListenableFuture<Void> run(Callable<Void> callable) {
            if (executorService == null || executorService.isShutdown()) {
                executorService = Executors.newSingleThreadExecutor();
                futureExecutor = new FutureExecutor(executorService);
            }

            return futureExecutor.submit(callable);
        }

        private void stop() {
            executorService.shutdown();
        }
    }

    private class SocketReceiverThread extends Thread {
        private Reader reader = new Reader();
        private byte[] chunk = new byte[1024];
        private final Message message = new Message();

        @Override
        public void run() {
            int bytesRead;
            try {
                socket.setSoTimeout(opts.timeout);
                while ((bytesRead = bufferedInputStream.read(chunk)) != -1) {
                    ArrayList<byte[]> buffers = reader.read(chunk, bytesRead);
                    for (byte[] buffer : buffers) {
                        process(Serializer.deserialize(buffer, message));
                    }
                }

            } catch (IOException e) {
                eventListener.onError(e);
            }

            if (opts.reconnect && !manuallyClosed) {
                reconnect();
            } else {
                eventThread.stop();
            }

            eventListener.onClose();

            connected = false;
        }

        private void process(Message message) throws UnsupportedEncodingException {
            switch (message.mt) {
                case Serializer.MT_DATA:
                    processWithListener(message, eventListener, false);
                    break;
                case Serializer.MT_DATA_WITH_ACK:
                    processWithListener(message, eventListener, true);
                    break;
                case Serializer.MT_ACK:
                    if (acks.containsKey(message.messageId)) {
                        processWithListener(message, acks.get(message.messageId), false);
                        acks.remove(message.messageId);
                    }
                    break;
                case Serializer.MT_REGISTER:
                    id = message.dataToString();
                    eventListener.onConnect();
                    break;
                case Serializer.MT_ERROR:
                    eventListener.onError(new Exception(message.dataToString()));
            }
        }

        private void processWithListener(Message message, EventListener eventListener, boolean dataWithAck) throws UnsupportedEncodingException {
            switch (message.dt) {
                case Serializer.DT_STRING:
                    if (dataWithAck) {
                        eventListener.onString(message.eventToString(), message.dataToString(), createAck(message));
                    } else {
                        eventListener.onString(message.eventToString(), message.dataToString());
                    }
                    break;
                case Serializer.DT_BINARY:
                    if (dataWithAck) {
                        eventListener.onBinary(message.eventToString(), message.data, createAck(message));
                    } else {
                        eventListener.onBinary(message.eventToString(), message.data);
                    }
                    break;
                case Serializer.DT_INTEGER:
                    if (dataWithAck) {
                        eventListener.onInteger(message.eventToString(), message.dataToInteger(), createAck(message));
                    } else {
                        eventListener.onInteger(message.eventToString(), message.dataToInteger());
                    }
                    break;
                case Serializer.DT_DECIMAL:
                    if (dataWithAck) {
                        eventListener.onDecimal(message.eventToString(), message.dataToDouble(), createAck(message));
                    } else {
                        eventListener.onDecimal(message.eventToString(), message.dataToDouble());
                    }
                    break;
                case Serializer.DT_OBJECT:
                    if (dataWithAck) {
                        eventListener.onObject(message.eventToString(), opts.objectSerializer.deserialize(message.data), createAck(message));
                    } else {
                        eventListener.onObject(message.eventToString(), opts.objectSerializer.deserialize(message.data));
                    }
                    break;
                case Serializer.DT_BOOLEAN:
                    if (dataWithAck) {
                        eventListener.onBoolean(message.eventToString(), message.dataToBoolean(), createAck(message));
                    } else {
                        eventListener.onBoolean(message.eventToString(), message.dataToBoolean());
                    }
            }
        }
    }
}
