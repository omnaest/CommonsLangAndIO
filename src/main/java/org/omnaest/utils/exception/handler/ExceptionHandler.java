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
package org.omnaest.utils.exception.handler;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.omnaest.utils.events.EventHandler;
import org.omnaest.utils.exception.handler.internal.DefaultRethrowingExceptionHandler;
import org.omnaest.utils.exception.handler.internal.NoOperationExceptionHandler;

/**
 * {@link EventHandler} for {@link Exception}s
 *
 * @see EventHandler
 * @author Omnaest
 */
public interface ExceptionHandler extends EventHandler<Exception>
{

    /**
     * Returns a {@link NoOperationExceptionHandler}
     * 
     * @return
     */
    public static ExceptionHandler noOperationExceptionHandler()
    {
        return new NoOperationExceptionHandler();
    }

    public static ExceptionHandler rethrowingExceptionHandler()
    {
        return new DefaultRethrowingExceptionHandler();
    }

    public static ExceptionHandler fromConsumer(Consumer<Exception> consumer)
    {
        return consumer::accept;
    }

    public static ExceptionHandler fromBiConsumer(BiConsumer<String, Exception> consumer)
    {
        return e -> consumer.accept(e != null ? e.getMessage() : null, e);
    }

}
