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
package org.omnaest.utils.functional;

import java.util.function.Function;

public class CompositeBidirectionalFunction<S, T> implements BidirectionalFunction<S, T>
{
    private Function<S, T> forward;
    private Function<T, S> backward;

    public CompositeBidirectionalFunction(Function<S, T> forward, Function<T, S> backward)
    {
        super();
        this.forward = forward;
        this.backward = backward;
    }

    @Override
    public Function<S, T> forward()
    {
        return this.forward;
    }

    @Override
    public Function<T, S> backward()
    {
        return this.backward;
    }

    @Override
    public String toString()
    {
        return "CompositeBidirectionalFunction [forward=" + this.forward + ", backward=" + this.backward + "]";
    }

}
