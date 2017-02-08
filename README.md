fast-tcp-java
===
fast-tcp client implemented in *java*. [fast-tcp](https://github.com/alemures/fast-tcp) is an extremely fast TCP client and server implemented in Node.js that allows to emit and listen to events.

## Server
https://github.com/alemures/fast-tcp

## Sample
```java
Socket socket = new Socket("localhost", 5000);

socket.emit("login", "username", new Emitter.Listener() {
    @Override
    public void call(Object... data) {
        System.out.println("Login response: " + data[0]);
    }
});

socket.on("welcome", new Emitter.Listener() {
    @Override
    public void call(Object... args) {
        System.out.println("Server says: " + args[0]);
    }
});

socket.connect();
```