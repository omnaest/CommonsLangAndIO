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
