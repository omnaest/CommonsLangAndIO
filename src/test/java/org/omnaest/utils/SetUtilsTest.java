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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.utils.SetUtils.SetDelta;

public class SetUtilsTest
{

    @Test
    public void testMerge() throws Exception
    {
        Set<String> merge = SetUtils.merge(Arrays.asList("a", "b"), Arrays.asList("b", "c"));
        assertEquals(Arrays.asList("a", "b", "c")
                           .stream()
                           .collect(Collectors.toSet()),
                     merge);
    }

    @Test
    public void testCopyAndRemove() throws Exception
    {
        Set<String> result = SetUtils.copyAndRemove(Arrays.asList("a", "b", "c")
                                                          .stream()
                                                          .collect(Collectors.toSet()),
                                                    "b");
        assertEquals(Arrays.asList("a", "c")
                           .stream()
                           .collect(Collectors.toSet()),
                     result);
    }

    @Test
    public void testDelta() throws Exception
    {
        SetDelta<String> delta = SetUtils.delta(Arrays.asList("a", "b"), Arrays.asList("b", "c"));
        assertEquals(Arrays.asList("a")
                           .stream()
                           .collect(Collectors.toSet()),
                     delta.getRemoved());
        assertEquals(Arrays.asList("c")
                           .stream()
                           .collect(Collectors.toSet()),
                     delta.getAdded());
        assertEquals(Arrays.asList("b")
                           .stream()
                           .collect(Collectors.toSet()),
                     delta.getShared());
    }

}
