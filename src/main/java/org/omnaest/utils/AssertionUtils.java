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
 * Helper asserting variables
 * 
 * @author omnaest
 */
public class AssertionUtils
{
    private AssertionUtils()
    {
    }

    /**
     * @throws IllegalArgumentException
     * @param object
     */
    public static void assertIsNotNull(Object object)
    {
        assertIsNotNull("Object must not be null", object);
    }

    /**
     * Similar to {@link #assertIsNotNull(Object)} but allows to provide a message
     * 
     * @param message
     * @param object
     */
    public static void assertIsNotNull(String message, Object object)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(message);
        }
    }
}
