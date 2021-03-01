/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
