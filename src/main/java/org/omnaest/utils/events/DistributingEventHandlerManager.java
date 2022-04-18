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
package org.omnaest.utils.events;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Distributing {@link EventHandlerManager}
 *
 * @author Omnaest
 * @param <E>
 */
public class DistributingEventHandlerManager<E> implements EventHandlerManager<E>
{
    private List<EventHandler<E>> handlers = new ArrayList<>();

    @Override
    public void accept(E event)
    {
        this.handlers.stream()
                     .collect(Collectors.toList())
                     .forEach(handler -> handler.accept(event));
    }

    @Override
    public DistributingEventHandlerManager<E> register(EventHandler<E> eventHandler)
    {
        this.handlers.add(eventHandler);
        return this;
    }

}
