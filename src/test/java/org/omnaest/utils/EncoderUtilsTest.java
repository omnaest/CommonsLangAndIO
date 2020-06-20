package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.omnaest.utils.EncoderUtils.TextEncoderAndDecoder;

public class EncoderUtilsTest
{

    @Test
    public void testAlphaNumericText() throws Exception
    {
        Arrays.asList("abc", "012349", "°^!§$%&/()=?`*'_:;²³{}\\´+~#-.,")
              .forEach(text ->
              {
                  TextEncoderAndDecoder encoderAndDecoder = EncoderUtils.newInstance()
                                                                        .forAlphaNumericText();
                  assertEquals(text, encoderAndDecoder.decode(encoderAndDecoder.encode(text)));
              });
    }

}
