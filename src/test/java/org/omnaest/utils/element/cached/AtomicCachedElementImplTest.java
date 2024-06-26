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
package org.omnaest.utils.element.cached;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

public class AtomicCachedElementImplTest
{
    private Iterator<String>      elements      = Arrays.asList("1", "2", "3")
                                                        .iterator();
    private CachedElement<String> cachedElement = CachedElement.of(() -> this.elements.next());

    @Test
    public void testGet() throws Exception
    {
        assertEquals("1", this.cachedElement.get());
        assertEquals("1", this.cachedElement.get());

        assertEquals("2", this.cachedElement.reset()
                                            .get());
    }

    @Test
    public void testGetSoftReference() throws Exception
    {
        CachedElement<String> softCachedElement = this.cachedElement.asSoftReferenceCachedElement();
        assertEquals("1", softCachedElement.get());
        assertEquals("1", softCachedElement.get());

        assertEquals("2", softCachedElement.reset()
                                           .get());
    }

    @Test
    public void testGetIfCached()
    {
        assertFalse(this.cachedElement.getIfCached()
                                      .isPresent());
        assertEquals("1", this.cachedElement.get());
        assertEquals("1", this.cachedElement.getIfCached()
                                            .orElse(null));
    }

}
