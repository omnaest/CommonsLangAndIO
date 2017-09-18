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
package org.omnaest.utils.events;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @see DistributionEventHandler
 * @author Omnaest
 */
public class DistributionEventHandlerTest
{

	@Test
	public void testHandle() throws Exception
	{
		List<String> result = new ArrayList<>();

		DistributionEventHandler<String> distributionEventHandler = new DistributionEventHandler<>();
		distributionEventHandler.register(result::add)
								.register(result::add);

		Arrays	.asList("1", "2", "3")
				.stream()
				.forEach(event -> distributionEventHandler.handle(event));

		assertEquals(Arrays.asList("1", "1", "2", "2", "3", "3"), result);
	}

}
