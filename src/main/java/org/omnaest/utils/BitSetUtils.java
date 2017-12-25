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

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Helper for {@link BitSet} operations
 * 
 * @author omnaest
 */
public class BitSetUtils
{
	/**
	 * Translator between {@link Enum}s and {@link BitSet}s with the minimal needed number of bits
	 * 
	 * @author omnaest
	 * @param <T>
	 */
	public static interface EnumBitSetTranslator<T extends Enum<?>>
	{
		/**
		 * Returns a {@link BitSet} for the given {@link Enum}
		 * 
		 * @see #toEnum(BitSet)
		 * @param enumConstant
		 * @return
		 */
		public BitSet toBitSet(T enumConstant);

		/**
		 * Returns a {@link BitSet} for a given {@link Stream} of {@link Enum}s
		 * 
		 * @see #toEnumStream(BitSet)
		 * @param stream
		 * @return
		 */
		public BitSet toBitSet(Stream<T> stream);

		/**
		 * Returns a {@link Stream} of {@link Enum}s for a given {@link BitSet}
		 * 
		 * @see #toBitSet(Stream)
		 * @param bitSet
		 * @return
		 */
		public Stream<T> toEnumStream(BitSet bitSet);

		/**
		 * Returns a {@link Enum} based on the given {@link BitSet}
		 * 
		 * @see #toBitSet(Enum)
		 * @param bitSet
		 * @return
		 */
		public T toEnum(BitSet bitSet);

		/**
		 * Returns the number of bits used per {@link Enum}
		 * 
		 * @return
		 */
		public int getNumberOfBitsPerEnum();
	}

	/**
	 * Returns a {@link EnumBitSetTranslator} for the given {@link Enum} {@link Class} type
	 * 
	 * @param enumType
	 * @return
	 */
	public static <T extends Enum<?>> EnumBitSetTranslator<T> enumTranslator(Class<T> enumType)
	{
		return new EnumBitSetTranslator<T>()
		{
			private T[]	enumConstants		= enumType.getEnumConstants();
			private int	numberOfBitsPerEnum	= this.determineBitsPerEnum(enumType);

			private Function<Number, BitSet>	numberToBitSetMapper	= BitSetUtils	.mapper()
																						.fromNumberWithMaxValue(this.enumConstants.length + 1);
			private Function<BitSet, Number>	bitSetToNumber			= BitSetUtils	.mapper()
																						.toNumberWithMaxValue(this.enumConstants.length + 1);

			private Function<T, Number>	enumToOrdinalNumberMapper	= enumConstant -> enumConstant != null ? enumConstant.ordinal() + 1 : 0;
			private Function<Number, T>	ordinalNumberToEnumMapper	= ordinal ->
																	{
																		int ordinalIntValue = ordinal.intValue();
																		return ordinalIntValue >= 1 ? this.enumConstants[ordinalIntValue - 1] : null;
																	};

			private Function<T, BitSet>	enumToBitSetMapper	= this.enumToOrdinalNumberMapper.andThen(this.numberToBitSetMapper);
			private Function<BitSet, T>	bitSetToEnumMapper	= this.bitSetToNumber.andThen(this.ordinalNumberToEnumMapper);

			private int determineBitsPerEnum(Class<T> enumType)
			{
				int numberOfEnums = enumType.getEnumConstants().length + 1;

				/**
				 * 2^0 = 1 -> 1
				 * 2^1 = 2 -> 3
				 * 2^2 = 4 -> 7
				 */
				int retval = 0;

				int accumulatedValue = 0;

				while (accumulatedValue < numberOfEnums)
				{
					retval++;
					accumulatedValue += Math.pow(2, retval);
				}
				return retval + 1;
			}

			@Override
			public int getNumberOfBitsPerEnum()
			{
				return this.numberOfBitsPerEnum;
			}

			@Override
			public BitSet toBitSet(Stream<T> stream)
			{
				BitSet retval = new BitSet();

				AtomicInteger frameIndex = new AtomicInteger();
				stream	.map(this.enumToBitSetMapper)
						.forEach(elementBitSet ->
						{
							frame(retval, this.numberOfBitsPerEnum, frameIndex.getAndIncrement()).set(elementBitSet);
						});

				return retval;
			}

			@Override
			public Stream<T> toEnumStream(BitSet bitSet)
			{
				return frameStream(bitSet, this.numberOfBitsPerEnum).filter(iframe -> iframe.getAsByte() != 0)
																	.map(iframe -> iframe.get())
																	.map(this.bitSetToEnumMapper);
			}

			@Override
			public BitSet toBitSet(T enumConstant)
			{
				return this.enumToBitSetMapper.apply(enumConstant);
			}

			@Override
			public T toEnum(BitSet bitSet)
			{
				return this.bitSetToEnumMapper.apply(bitSet);
			}

		};
	}

	/**
	 * A {@link BitSetFrame} does describe a subset of bits of a given master {@link BitSet}
	 * 
	 * @author omnaest
	 */
	public static interface BitSetFrame
	{
		/**
		 * Sets the number of bits of the frame based on the frame size
		 * 
		 * @param bitSet
		 * @return
		 */
		public BitSetFrame set(BitSet bitSet);

		/**
		 * Returns the exact frame size bits as own {@link BitSet}
		 * 
		 * @return
		 */
		public BitSet get();

		/**
		 * Returns the value of {@link #get()} as {@link Byte}
		 * 
		 * @return
		 */
		public byte getAsByte();
	}

	/**
	 * Returns a {@link BitSetFrame} at the given frame index based on the given frame size in bits
	 * 
	 * @param bitSet
	 * @param frameSize
	 * @param frameIndex
	 * @return
	 */
	public static BitSetFrame frame(BitSet bitSet, int frameSize, int frameIndex)
	{
		return new BitSetFrame()
		{
			@Override
			public BitSetFrame set(BitSet otherBitSet)
			{
				for (int ii = 0; ii < frameSize; ii++)
				{
					int bitIndex = ii + frameSize * frameIndex;
					boolean value = otherBitSet.get(ii);
					bitSet.set(bitIndex, value);
				}

				return this;
			}

			@Override
			public BitSet get()
			{
				BitSet retval = new BitSet();

				if (frameIndex >= 0)
				{
					for (int ii = 0; ii < frameSize; ii++)
					{
						int bitIndex = ii + frameSize * frameIndex;
						boolean value = bitSet.get(bitIndex);
						retval.set(ii, value);
					}
				}

				return retval;
			}

			@Override
			public byte getAsByte()
			{
				return toByte(this.get());
			}

		};
	}

	/**
	 * Returns a {@link Stream} of {@link BitSetFrame}s based on the given frame size in bits
	 * 
	 * @param bitSet
	 * @param frameSize
	 * @return
	 */
	public static Stream<BitSetFrame> frameStream(BitSet bitSet, int frameSize)
	{
		return IntStream.range(0, bitSet.size() / frameSize)
						.mapToObj(frameIndex -> frame(bitSet, frameSize, frameIndex));
	}

	/**
	 * Returns the {@link Byte} representation of the {@link BitSet}
	 * 
	 * @param bitSet
	 * @return
	 */
	public static byte toByte(BitSet bitSet)
	{
		byte retval = 0;

		for (int ii = 0; ii < Byte.SIZE; ii++)
		{
			if (bitSet.get(ii))
			{
				retval |= ((byte) 1) << ii;
			}
		}

		return retval;
	}

	/**
	 * Returns the {@link Integer} representation of the {@link BitSet}
	 * 
	 * @param bitSet
	 * @return
	 */
	public static int toInt(BitSet bitSet)
	{
		int retval = 0;

		for (int ii = 0; ii < Integer.SIZE; ii++)
		{
			if (bitSet.get(ii))
			{
				retval |= 1 << ii;
			}
		}

		return retval;
	}

	/**
	 * Returns the {@link Long} representation of the {@link BitSet}
	 * 
	 * @param bitSet
	 * @return
	 */
	public static long toLong(BitSet bitSet)
	{
		long retval = 0;

		for (int ii = 0; ii < Long.SIZE; ii++)
		{
			if (bitSet.get(ii))
			{
				retval |= 1l << ii;
			}
		}

		return retval;
	}

	/**
	 * Returns a {@link BitSet} representing a given {@link Byte} value
	 * 
	 * @param value
	 * @return
	 */
	public static BitSet toBitSet(byte value)
	{
		BitSet retval = new BitSet();

		for (int ii = 0; ii < Byte.SIZE; ii++)
		{
			byte val = (byte) (value >> ii & 1);
			retval.set(ii, val == 1);
		}

		return retval;
	}

	/**
	 * Returns a {@link BitSet} representing a given {@link Integer} value
	 * 
	 * @param value
	 * @return
	 */
	public static BitSet toBitSet(int value)
	{
		BitSet retval = new BitSet();

		for (int ii = 0; ii < Integer.SIZE; ii++)
		{
			byte val = (byte) (value >> ii & 1);
			retval.set(ii, val == 1);
		}

		return retval;
	}

	/**
	 * Returns a {@link BitSet} representing a given {@link Long} value
	 * 
	 * @param value
	 * @return
	 */
	public static BitSet toBitSet(long value)
	{
		BitSet retval = new BitSet();

		for (int ii = 0; ii < Long.SIZE; ii++)
		{
			byte val = (byte) (value >> ii & 1);
			retval.set(ii, val == 1);
		}

		return retval;
	}

	/**
	 * Provides {@link Function} implementations in various directions between {@link BitSet} and {@link Long}, {@link Integer} or {@link Byte}
	 * 
	 * @author omnaest
	 */
	public static interface BitSetMapper
	{
		public Function<Long, BitSet> fromLong();

		public Function<Integer, BitSet> fromInt();

		public Function<Byte, BitSet> fromByte();

		/**
		 * Returns an appropriate {@link Function} based on the max value
		 * 
		 * @see Long#MAX_VALUE
		 * @see Integer#MAX_VALUE
		 * @see Byte#MAX_VALUE
		 * @param maxValue
		 * @return
		 */
		public Function<Number, BitSet> fromNumberWithMaxValue(long maxValue);

		public Function<BitSet, Long> toLong();

		public Function<BitSet, Integer> toInt();

		public Function<BitSet, Byte> toByte();

		/**
		 * Returns an appropriate {@link Function} based on the max value
		 * 
		 * @see Long#MAX_VALUE
		 * @see Integer#MAX_VALUE
		 * @see Byte#MAX_VALUE
		 * @param maxValue
		 * @return
		 */
		public Function<BitSet, Number> toNumberWithMaxValue(long maxValue);
	}

	public static BitSetMapper mapper()
	{
		return new BitSetMapper()
		{

			@Override
			public Function<Long, BitSet> fromLong()
			{
				return value -> toBitSet(value);
			}

			@Override
			public Function<Integer, BitSet> fromInt()
			{
				return value -> toBitSet(value);
			}

			@Override
			public Function<Byte, BitSet> fromByte()
			{
				return value -> toBitSet(value);
			}

			@SuppressWarnings("unchecked")
			@Override
			public Function<Number, BitSet> fromNumberWithMaxValue(long maxValue)
			{
				Function<? extends Number, BitSet> retval = null;
				if (maxValue <= Byte.MAX_VALUE)
				{
					retval = ((Function<Number, Byte>) value -> value.byteValue()).andThen(this.fromByte());
				}
				else if (maxValue <= Integer.MAX_VALUE)
				{
					retval = ((Function<Number, Integer>) value -> value.intValue()).andThen(this.fromInt());
				}
				else if (maxValue <= Long.MAX_VALUE)
				{
					retval = ((Function<Number, Long>) value -> value.longValue()).andThen(this.fromLong());
				}
				return (Function<Number, BitSet>) retval;
			}

			@Override
			public Function<BitSet, Long> toLong()
			{
				return bitSet -> BitSetUtils.toLong(bitSet);
			}

			@Override
			public Function<BitSet, Integer> toInt()
			{
				return bitSet -> BitSetUtils.toInt(bitSet);
			}

			@Override
			public Function<BitSet, Byte> toByte()
			{
				return bitSet -> BitSetUtils.toByte(bitSet);
			}

			@SuppressWarnings("unchecked")
			@Override
			public Function<BitSet, Number> toNumberWithMaxValue(long maxValue)
			{
				Function<BitSet, ? extends Number> retval = null;
				if (maxValue <= Byte.MAX_VALUE)
				{
					retval = this.toByte();
				}
				else if (maxValue <= Integer.MAX_VALUE)
				{
					retval = this.toInt();
				}
				else if (maxValue <= Long.MAX_VALUE)
				{
					retval = this.toLong();
				}
				return (Function<BitSet, Number>) retval;
			}

		};
	}

	public static BitSet valueOf(byte... values)
	{
		BitSet bitSet = new BitSet();

		if (values != null)
		{
			int frameSize = Byte.SIZE;
			for (int frameIndex = 0; frameIndex < values.length; frameIndex++)
			{
				frame(bitSet, frameSize, frameIndex).set(toBitSet(values[frameIndex]));
			}
		}

		return bitSet;
	}
}
