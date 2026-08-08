/*******************************************************************************
 * Copyright 2026 Danny Kunz
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
package org.apache.commons.lang.math;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Migration shim for the discontinued commons-lang 2.x {@link org.apache.commons.lang.math.RandomUtils}. This is the one shim class
 * that cannot simply forward, because commons-lang3 both moved the class out of the <code>math</code> subpackage and changed the
 * signature of the bound variant.
 *
 * @deprecated use {@link org.apache.commons.lang3.RandomUtils} instead - note the package is <code>org.apache.commons.lang3</code>,
 *             NOT <code>org.apache.commons.lang3.math</code>. Signature mapping:<br>
 *             <br>
 *             <ul>
 *             <li><code>nextInt(int n)</code> becomes <code>RandomUtils.nextInt(0, n)</code></li>
 *             <li><code>nextInt()</code> becomes <code>RandomUtils.nextInt()</code>, which returns a NON-NEGATIVE int, whereas this
 *             method spans the full int range</li>
 *             <li>the {@link Random} accepting variants have no commons-lang3 equivalent - call the {@link Random} instance
 *             directly</li>
 *             </ul>
 * @author omnaest
 */
@Deprecated(forRemoval = true)
public class RandomUtils
{
    /**
     * Returns a random int across the full int range, as commons-lang 2.x did. Note that
     * {@link org.apache.commons.lang3.RandomUtils#nextInt()} returns a non-negative value instead.
     *
     * @return
     */
    public static int nextInt()
    {
        return ThreadLocalRandom.current()
                                .nextInt();
    }

    public static int nextInt(Random random)
    {
        return random.nextInt();
    }

    /**
     * Returns a random int between 0 (inclusive) and the given bound (exclusive).
     *
     * @param n
     * @return
     */
    public static int nextInt(int n)
    {
        return ThreadLocalRandom.current()
                                .nextInt(n);
    }

    public static int nextInt(Random random, int n)
    {
        return random.nextInt(n);
    }
}
