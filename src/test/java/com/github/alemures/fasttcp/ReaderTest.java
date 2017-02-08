package com.github.alemures.fasttcp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class ReaderTest {
    private Reader reader;

    @Before
    public void resetReaderInstance() {
        reader = new Reader();
    }

    @Test
    public void shouldReadMessages() {
        byte[] message1 = Serializer.serialize("event1".getBytes(), "data1".getBytes(), Serializer.MT_DATA, Serializer.DT_STRING, 1);
        byte[] message2 = Serializer.serialize("event2".getBytes(), "data2".getBytes(), Serializer.MT_DATA, Serializer.DT_STRING, 2);
        byte[] message3 = Serializer.serialize("event3".getBytes(), "data3".getBytes(), Serializer.MT_DATA, Serializer.DT_STRING, 3);
        byte[] buffer = concat(message1, message2, message3);

        ArrayList<byte[]> messages = reader.read(buffer, buffer.length);
        Assert.assertEquals("Invalid number of read messages", 3, messages.size());
    }

    @Test
    public void shouldReadChunkedMessages() {
        byte[] message1 = Serializer.serialize("event1".getBytes(), "data1".getBytes(), Serializer.MT_DATA, Serializer.DT_STRING, 1);
        byte[] message2 = Serializer.serialize("event2".getBytes(), "data2".getBytes(), Serializer.MT_DATA, Serializer.DT_STRING, 2);
        byte[] message3 = Serializer.serialize("event3".getBytes(), "data3".getBytes(), Serializer.MT_DATA, Serializer.DT_STRING, 3);
        byte[] buffer = concat(message1, message2, message3);

        byte[][] chunkedBuffer = chunk(buffer, 5);
        ArrayList<byte[]> allMessages = new ArrayList<>();

        for (byte[] chunk : chunkedBuffer) {
            ArrayList<byte[]> messages = reader.read(chunk, chunk.length);
            allMessages.addAll(messages);
        }

        Assert.assertEquals("Invalid number of read messages", 3, allMessages.size());
    }

    private byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }

        byte[] result = new byte[length];
        int offset = 0;

        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    private byte[][] chunk(byte[] array, int chunkSize) {
        int chunkCount = (int) Math.ceil((double) array.length / chunkSize);
        byte[][] chunks = new byte[chunkCount][];
        int offset = 0;

        for (int i = 0; i < chunkCount; i++) {
            chunks[i] = new byte[chunkSize];

            int bytesToCopy = offset + chunkSize <= array.length ? chunkSize : array.length - offset;
            System.arraycopy(array, offset, chunks[i], 0, bytesToCopy);
            offset += chunkSize;
        }

        return chunks;
    }
}
