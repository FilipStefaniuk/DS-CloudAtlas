package pl.edu.mimuw.cloudatlas.model;

import org.junit.Assert;
import org.junit.Test;

public class ValueDurationTest {

    @Test
    public void stringConstructorTest() {
        String value = "-1 01:01:01.001";
        Long expectedResult = -(((((24L + 1L)*60L+1L)*60L+1L)*1000L)+1L);
        ValueDuration valueDuration = new ValueDuration(value);

        Assert.assertEquals(expectedResult, valueDuration.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void stringConstructorIllegalPatternTest() {
        String value = "+1 25:20:11.333";
        ValueDuration valueDuration = new ValueDuration(value);
    }
}
