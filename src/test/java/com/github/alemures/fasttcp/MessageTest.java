package com.github.alemures.fasttcp;

import org.junit.Assert;
import org.junit.Test;

public class MessageTest {
    @Test
    public void shouldBuildAErrorMessage() {
        String data = "Something went wrong!";
        Message m = new Message("Something went wrong!");

        Assert.assertArrayEquals(data.getBytes(), m.data);
        Assert.assertEquals(Serializer.MT_ERROR, m.mt);
        Assert.assertEquals(Serializer.DT_STRING, m.dt);
    }
}
