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

/**
 * @author omnaest
 */
public class BooleanUtils
{
    /**
     * Returns the int value depending on the given boolean value.
     * 
     * @param value
     * @param trueValue
     *            value in the true case
     * @param falseValue
     *            value in the false case
     * @return
     */
    public static int toIntValue(boolean value, int trueValue, int falseValue)
    {
        return value ? trueValue : falseValue;
    }
}
