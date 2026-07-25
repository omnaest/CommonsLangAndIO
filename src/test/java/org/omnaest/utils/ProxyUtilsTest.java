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
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.omnaest.utils.ProxyUtils.Argument;
import org.omnaest.utils.ProxyUtils.Arguments;
import org.omnaest.utils.ProxyUtils.InvocationMethod;
import org.omnaest.utils.ProxyUtils.ProxyBuilderLoaded;

public class ProxyUtilsTest
{
    protected static interface TestType
    {
        public String getValue();

        public void setValue(String value);
    }

    protected static interface BaseType
    {
        public String getBaseValue();
    }

    protected static interface FirstType extends BaseType
    {
        public String getFirstValue();
    }

    protected static interface SecondType extends BaseType
    {
        public String getSecondValue();
    }

    protected static interface DefaultMethodType
    {
        public String getValue();

        public default String getUpperCaseValue()
        {
            return this.getValue()
                       .toUpperCase();
        }
    }

    protected static interface PrimitiveType
    {
        public int getCount();
    }

    protected static interface ConsumerType
    {
        public void accept(Object value);
    }

    protected static enum TestEnum
    {
        FIRST, SECOND
    }

    private Argument captureFirstArgument(Object value)
    {
        AtomicReference<Arguments> capturedArguments = new AtomicReference<>();
        ConsumerType consumerType = ProxyUtils.builder()
                                              .of(ConsumerType.class)
                                              .withHandler(method -> true, arguments ->
                                              {
                                                  capturedArguments.set(arguments);
                                                  return null;
                                              })
                                              .build();
        consumerType.accept(value);
        return capturedArguments.get()
                                .first();
    }

    @Test
    public void testWithHandlers() throws Exception
    {
        AtomicReference<String> content = new AtomicReference<>();
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .withHandlers(builder -> builder.setHandlerIf(method -> "setValue".equals(method.getName()), arguments ->
                                      {
                                          content.set(arguments.first()
                                                               .get());
                                          return null;
                                      })
                                                                      .setHandlerIf(method -> "getValue".equals(method.getName()),
                                                                                    arguments -> content.get())
                                                                      .build())
                                      .build();

        testType.setValue("value1");
        assertEquals("value1", testType.getValue());
    }

    @Test
    public void testWithHandlerByMethodMatcher() throws Exception
    {
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .withHandler(method -> "getValue".equals(method.getName()), arguments -> "fixed")
                                      .withDefaultHandler(arguments -> null)
                                      .build();

        assertEquals("fixed", testType.getValue());
        testType.setValue("ignored");
        assertEquals("fixed", testType.getValue());
    }

    @Test
    public void testMethodInvocationHandlerProvidesMethodMetadata() throws Exception
    {
        AtomicReference<InvocationMethod> capturedMethod = new AtomicReference<>();
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .withHandler(method -> "setValue".equals(method.getName()), (method, arguments) ->
                                      {
                                          capturedMethod.set(method);
                                          return null;
                                      })
                                      .withDefaultHandler(arguments -> null)
                                      .build();

        testType.setValue("value1");

        assertEquals("setValue", capturedMethod.get()
                                               .getName());
        assertEquals(void.class, capturedMethod.get()
                                               .getReturnType());
        assertEquals(Arrays.asList(String.class), capturedMethod.get()
                                                               .getArgumentTypes());
    }

    @Test
    public void testArgumentsAccess() throws Exception
    {
        AtomicReference<Arguments> capturedArguments = new AtomicReference<>();
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .withHandler(method -> "setValue".equals(method.getName()), arguments ->
                                      {
                                          capturedArguments.set(arguments);
                                          return null;
                                      })
                                      .withDefaultHandler(arguments -> null)
                                      .build();

        testType.setValue("value1");

        Arguments arguments = capturedArguments.get();
        assertEquals(1, arguments.size());
        assertEquals(1, arguments.stream()
                                 .count());
        assertEquals("value1", arguments.first()
                                        .get());
        assertEquals("mapped:value1", arguments.first()
                                               .getAs(value -> "mapped:" + value));
        assertEquals(String.class, arguments.first()
                                            .getType());

        //
        // out of range access must not throw
        assertNull(arguments.at(5)
                            .get());
        assertNull(arguments.at(5)
                            .getAs(value -> value));
        assertNull(arguments.at(5)
                            .getType());
        assertNull(arguments.at(-1)
                            .get());
    }

    @Test
    public void testArgumentsOfMethodWithoutParameters() throws Exception
    {
        AtomicReference<Arguments> capturedArguments = new AtomicReference<>();
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .withHandler(method -> "getValue".equals(method.getName()), arguments ->
                                      {
                                          capturedArguments.set(arguments);
                                          return null;
                                      })
                                      .build();

        testType.getValue();

        Arguments arguments = capturedArguments.get();
        assertEquals(0, arguments.size());
        assertNull(arguments.first()
                            .get());
        assertNull(arguments.first()
                            .getAs(value -> value));
    }

    @Test
    public void testArgumentGetAsStringAndCharacter() throws Exception
    {
        assertEquals("value1", this.captureFirstArgument("value1")
                                   .getAsString());
        assertEquals("123", this.captureFirstArgument(123)
                                .getAsString());
        assertNull(this.captureFirstArgument(null)
                       .getAsString());

        assertEquals(Character.valueOf('a'), this.captureFirstArgument('a')
                                                 .getAsCharacter());
        assertEquals(Character.valueOf('a'), this.captureFirstArgument("abc")
                                                 .getAsCharacter());
        assertNull(this.captureFirstArgument("")
                       .getAsCharacter());
        assertNull(this.captureFirstArgument(null)
                       .getAsCharacter());
    }

    @Test
    public void testArgumentGetAsNumber() throws Exception
    {
        assertEquals(Byte.valueOf((byte) 42), this.captureFirstArgument(42)
                                                  .getAsByte());
        assertEquals(Short.valueOf((short) 42), this.captureFirstArgument("42")
                                                    .getAsShort());
        assertEquals(Integer.valueOf(42), this.captureFirstArgument(42)
                                              .getAsInteger());
        assertEquals(Long.valueOf(42), this.captureFirstArgument("42")
                                           .getAsLong());
        assertEquals(Float.valueOf(42.5f), this.captureFirstArgument(42.5)
                                               .getAsFloat());
        assertEquals(Double.valueOf(42.5), this.captureFirstArgument("42.5")
                                               .getAsDouble());

        //
        // a Number is narrowed like an explicit cast would do
        assertEquals(Integer.valueOf(42), this.captureFirstArgument(42.7)
                                              .getAsInteger());

        //
        // the String representation is used to stay exact
        assertEquals(new BigDecimal("0.1"), this.captureFirstArgument(0.1d)
                                                .getAsBigDecimal());
        assertEquals(BigInteger.valueOf(42), this.captureFirstArgument("42.9")
                                                 .getAsBigInteger());

        assertNull(this.captureFirstArgument(null)
                       .getAsInteger());
        assertNull(this.captureFirstArgument(null)
                       .getAsBigDecimal());
    }

    @Test(expected = NumberFormatException.class)
    public void testArgumentGetAsNumberOfNonNumericalValue() throws Exception
    {
        this.captureFirstArgument("abc")
            .getAsInteger();
    }

    @Test
    public void testArgumentGetAsBoolean() throws Exception
    {
        assertTrue(this.captureFirstArgument(true)
                       .getAsBoolean());
        assertFalse(this.captureFirstArgument(false)
                        .getAsBoolean());
        assertTrue(this.captureFirstArgument("TRUE")
                       .getAsBoolean());
        assertTrue(this.captureFirstArgument("yes")
                       .getAsBoolean());
        assertFalse(this.captureFirstArgument("off")
                        .getAsBoolean());
        assertTrue(this.captureFirstArgument(1)
                       .getAsBoolean());
        assertFalse(this.captureFirstArgument(0)
                        .getAsBoolean());

        assertNull(this.captureFirstArgument("maybe")
                       .getAsBoolean());
        assertNull(this.captureFirstArgument(null)
                       .getAsBoolean());
    }

    @Test
    public void testArgumentGetAsEnum() throws Exception
    {
        assertEquals(TestEnum.FIRST, this.captureFirstArgument(TestEnum.FIRST)
                                         .getAsEnum(TestEnum.class));
        assertEquals(TestEnum.SECOND, this.captureFirstArgument("SECOND")
                                          .getAsEnum(TestEnum.class));

        //
        // a non matching name returns null instead of throwing
        assertNull(this.captureFirstArgument("THIRD")
                       .getAsEnum(TestEnum.class));
        assertNull(this.captureFirstArgument(null)
                       .getAsEnum(TestEnum.class));
    }

    @Test
    public void testArgumentConvertersOfMissingArgument() throws Exception
    {
        AtomicReference<Arguments> capturedArguments = new AtomicReference<>();
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .withHandler(method -> "getValue".equals(method.getName()), arguments ->
                                      {
                                          capturedArguments.set(arguments);
                                          return null;
                                      })
                                      .build();
        testType.getValue();

        Argument argument = capturedArguments.get()
                                             .first();
        assertNull(argument.getAsString());
        assertNull(argument.getAsCharacter());
        assertNull(argument.getAsBoolean());
        assertNull(argument.getAsInteger());
        assertNull(argument.getAsLong());
        assertNull(argument.getAsDouble());
        assertNull(argument.getAsBigDecimal());
        assertNull(argument.getAsBigInteger());
        assertNull(argument.getAsEnum(TestEnum.class));
    }

    @Test
    public void testObjectMethodsAreAlwaysHandled() throws Exception
    {
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .build();
        TestType otherTestType = ProxyUtils.builder()
                                           .of(TestType.class)
                                           .build();

        assertEquals(System.identityHashCode(testType), testType.hashCode());
        assertTrue(testType.equals(testType));
        assertFalse(testType.equals(otherTestType));
        assertFalse(testType.equals(null));
        assertNotNull(testType.toString());
        assertTrue(testType.toString()
                           .contains(TestType.class.getName()));

        //
        // a proxy must be usable within hash based collections
        Set<TestType> proxies = new HashSet<>();
        proxies.add(testType);
        assertTrue(proxies.contains(testType));
        assertFalse(proxies.contains(otherTestType));
    }

    @Test
    public void testObjectMethodHandlingWithCatchAllMethodMatcher() throws Exception
    {
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .withHandler(method -> true, arguments -> "any")
                                      .build();

        //
        // no interface method is named hashCode/equals/toString, so the built in handling still applies
        assertEquals("any", testType.getValue());
        assertNotNull(testType.toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnhandledMethodThrowsException() throws Exception
    {
        ProxyUtils.builder()
                  .of(TestType.class)
                  .build()
                  .getValue();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnhandledMethodWithPrimitiveReturnTypeThrowsException() throws Exception
    {
        ProxyUtils.builder()
                  .of(PrimitiveType.class)
                  .build()
                  .getCount();
    }

    @Test
    public void testDefaultHandler() throws Exception
    {
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .withDefaultHandler(arguments -> "default")
                                      .build();

        assertEquals("default", testType.getValue());
    }

    @Test
    public void testDefaultHandlerReturningNull() throws Exception
    {
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .withDefaultHandler(arguments -> null)
                                      .build();

        assertNull(testType.getValue());
    }

    @Test
    public void testPrimitiveReturnType() throws Exception
    {
        PrimitiveType primitiveType = ProxyUtils.builder()
                                                .of(PrimitiveType.class)
                                                .withHandler(method -> true, arguments -> 42)
                                                .build();

        assertEquals(42, primitiveType.getCount());
    }

    @Test
    public void testDefaultMethodIsInvokedOnProxy() throws Exception
    {
        DefaultMethodType defaultMethodType = ProxyUtils.builder()
                                                        .of(DefaultMethodType.class)
                                                        .withHandler(method -> "getValue".equals(method.getName()), arguments -> "value1")
                                                        .build();

        assertEquals("VALUE1", defaultMethodType.getUpperCaseValue());
    }

    @Test
    public void testMultipleInterfacesWithSharedSuperInterface() throws Exception
    {
        SecondType secondType = ProxyUtils.builder()
                                          .of(FirstType.class)
                                          .of(SecondType.class)
                                          .withHandler(method -> true, arguments -> "any")
                                          .build();

        assertEquals("any", secondType.getSecondValue());
        assertEquals("any", secondType.getBaseValue());
        assertEquals("any", ((FirstType) secondType).getFirstValue());
    }

    @Test
    public void testSameInterfaceLoadedTwice() throws Exception
    {
        TestType testType = ProxyUtils.builder()
                                      .of(TestType.class)
                                      .of(TestType.class)
                                      .withHandler(method -> true, arguments -> "any")
                                      .build();

        assertEquals("any", testType.getValue());
    }

    @Test
    public void testInterfaceOfOtherClassLoaderCanBeCombined() throws Exception
    {
        TestType testType = ProxyUtils.builder()
                                      .of(Runnable.class)
                                      .of(TestType.class)
                                      .withHandler(method -> "getValue".equals(method.getName()), arguments -> "any")
                                      .build();

        assertEquals("any", testType.getValue());
        assertTrue(testType instanceof Runnable);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonInterfaceTypeIsRejected() throws Exception
    {
        ProxyUtils.builder()
                  .of(String.class);
    }

    @Test
    public void testHandlerRegisteredAfterBuildDoesNotAffectExistingProxy() throws Exception
    {
        ProxyBuilderLoaded<TestType> builder = ProxyUtils.builder()
                                                         .of(TestType.class);

        TestType firstProxy = builder.withHandler(method -> "getValue".equals(method.getName()), arguments -> "first")
                                     .build();

        TestType secondProxy = builder.withHandler(method -> "getValue".equals(method.getName()), arguments -> "second")
                                      .build();

        assertEquals("first", firstProxy.getValue());
        assertEquals("second", secondProxy.getValue());
    }

}
