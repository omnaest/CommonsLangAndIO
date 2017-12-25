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

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.utils.BitSetUtils.EnumBitSetTranslator;

public class BitSetUtilsTest
{
	private enum TestEnum
	{
		A, B, C
	}

	@Test
	public void testToBitSet() throws Exception
	{
		List<TestEnum> enumList = Arrays.asList(TestEnum.A, TestEnum.C, TestEnum.B);

		EnumBitSetTranslator<TestEnum> translator = BitSetUtils.enumTranslator(TestEnum.class);
		BitSet bitSet = translator.toBitSet(enumList.stream());
		assertEquals(enumList, translator	.toEnumStream(bitSet)
											.collect(Collectors.toList()));
	}

	@Test
	public void testToByte() throws Exception
	{
		BitSet bitSet = new BitSet();
		assertEquals(0, BitSetUtils.toByte(bitSet));
	}

	@Test
	public void testToBitSetFromByte() throws Exception
	{
		assertEquals(1, BitSetUtils.toByte(BitSetUtils.toBitSet((byte) 1)));
		assertEquals(0, BitSetUtils.toByte(BitSetUtils.toBitSet((byte) 0)));
		assertEquals(Byte.MAX_VALUE, BitSetUtils.toByte(BitSetUtils.toBitSet(Byte.MAX_VALUE)));
		assertEquals(Byte.MIN_VALUE, BitSetUtils.toByte(BitSetUtils.toBitSet(Byte.MIN_VALUE)));
	}

	@Test
	public void testToBitSetFromInt() throws Exception
	{
		assertEquals(1, BitSetUtils.toInt(BitSetUtils.toBitSet(1)));
		assertEquals(0, BitSetUtils.toInt(BitSetUtils.toBitSet(0)));
		assertEquals(Integer.MAX_VALUE, BitSetUtils.toInt(BitSetUtils.toBitSet(Integer.MAX_VALUE)));
		assertEquals(Integer.MIN_VALUE, BitSetUtils.toInt(BitSetUtils.toBitSet(Integer.MIN_VALUE)));
	}

	@Test
	public void testToBitSetFromLong() throws Exception
	{
		assertEquals(1, BitSetUtils.toLong(BitSetUtils.toBitSet(1)));
		assertEquals(0, BitSetUtils.toLong(BitSetUtils.toBitSet(0)));
		assertEquals(Long.MAX_VALUE, BitSetUtils.toLong(BitSetUtils.toBitSet(Long.MAX_VALUE)));
		assertEquals(Long.MIN_VALUE, BitSetUtils.toLong(BitSetUtils.toBitSet(Long.MIN_VALUE)));
	}
}
