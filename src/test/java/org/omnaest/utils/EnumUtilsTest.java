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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

public class EnumUtilsTest
{
    private static enum TestEnum
    {
        ONE, TWO, THREE
    }

    @Test
    public void testDecideOn() throws Exception
    {
        assertTrue(EnumUtils.decideOn(TestEnum.TWO)
                            .ifEqualTo(TestEnum.ONE, () -> fail())
                            .isEqualTo(TestEnum.TWO));
        assertFalse(EnumUtils.decideOn(TestEnum.ONE)
                             .ifEqualTo(TestEnum.TWO, () -> fail())
                             .isEqualTo(TestEnum.TWO));
        assertFalse(EnumUtils.decideOn((TestEnum) null)
                             .ifEqualTo(TestEnum.TWO, () -> fail())
                             .ifEqualTo(TestEnum.ONE, () -> fail())
                             .isEqualTo(TestEnum.TWO));
        assertTrue(EnumUtils.decideOn((TestEnum) null)
                            .orElse(TestEnum.TWO)
                            .ifEqualTo(TestEnum.ONE, () -> fail())
                            .isEqualTo(TestEnum.TWO));
        assertTrue(EnumUtils.decideOn(TestEnum.THREE)
                            .ifAnyEqualTo(Arrays.asList(TestEnum.ONE, TestEnum.TWO), (matches) -> fail("Wrong matches :" + matches))
                            .isEqualToAny(TestEnum.TWO, TestEnum.THREE));

    }

}
