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
package org.omnaest.utils.bitset;

import java.util.BitSet;

/**
 * Wrapper around {@link BitSet} which provides further functionality
 * 
 * @author omnaest
 */
public class Bits
{
    private int    length = 0;
    private BitSet bitSet = new BitSet();

    public Bits flip(int bitIndex)
    {
        this.adjustLengthIfNecessary(bitIndex);
        this.bitSet.flip(bitIndex);
        return this;
    }

    public Bits clear(int bitIndex)
    {
        this.adjustLengthIfNecessary(bitIndex);
        this.bitSet.clear(bitIndex);
        return this;
    }

    public boolean get(int bitIndex)
    {
        this.assertIndexBounds(bitIndex);

        return this.bitSet.get(bitIndex);
    }

    private void assertIndexBounds(int bitIndex)
    {
        if (bitIndex < 0)
        {
            throw new IndexOutOfBoundsException("Bitset index cannot be lower than zero: " + bitIndex);
        }

        if (bitIndex >= this.length)
        {
            throw new IndexOutOfBoundsException("Bitset length is " + this.length + " but access was on index position " + bitIndex);
        }
    }

    public Bits set(int bitIndex)
    {
        this.adjustLengthIfNecessary(bitIndex);
        this.bitSet.set(bitIndex);
        return this;
    }

    private void adjustLengthIfNecessary(int bitIndex)
    {
        if (bitIndex >= this.length)
        {
            this.length = bitIndex + 1;
        }
    }

}
