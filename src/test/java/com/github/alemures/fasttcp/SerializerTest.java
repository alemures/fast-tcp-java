package com.github.alemures.fasttcp;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SerializerTest {
    private byte[] event = "eventName".getBytes();
    private byte[] data = "eventData".getBytes();
    private byte mt = Serializer.MT_DATA;
    private byte dt = Serializer.DT_STRING;
    private int messageId = 1;

    @Test
    public void shouldSerializeAMessage() {
        byte[] buffer = Serializer.serialize(event, data, mt, dt, messageId);

        Assert.assertEquals("Invalid message version", Serializer.VERSION, buffer[4]);
        Assert.assertEquals("Flags should be 0", 0, buffer[5]);
        Assert.assertEquals("The data type should be 'string'", Serializer.DT_STRING, buffer[6]);
        Assert.assertEquals("The message type should be 'data'", Serializer.MT_DATA, buffer[7]);
        Assert.assertEquals("Invalid message id", messageId, Utils.readInt(buffer, 8));
        Assert.assertEquals("Invalid event length", event.length, Utils.readShort(buffer, 12));

        int eventEnd = 14 + event.length;
        Assert.assertArrayEquals("Invalid event", event, Arrays.copyOfRange(buffer, 14, eventEnd));
        Assert.assertEquals("Invalid data length", data.length, Utils.readInt(buffer, eventEnd));

        int dataStart = eventEnd + 4;
        int dataEnd = dataStart + data.length;
        Assert.assertArrayEquals("Invalid data", data, Arrays.copyOfRange(buffer, dataStart, dataEnd));
    }

    @Test
    public void shouldDeserializeAMessage() throws UnsupportedEncodingException {
        byte[] buffer = Serializer.serialize(event, data, mt, dt, messageId);
        Message message = Serializer.deserialize(buffer);

        Assert.assertArrayEquals("Invalid event", message.event.getBytes(), event);
        Assert.assertArrayEquals("Invalid data", message.data, data);
        Assert.assertEquals("Invalid message type", message.mt, mt);
        Assert.assertEquals("Invalid data type", message.dt, dt);
        Assert.assertEquals("Invalid message id", message.id, messageId);
    }
}
