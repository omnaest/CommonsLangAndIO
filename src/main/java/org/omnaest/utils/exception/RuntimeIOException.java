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
package org.omnaest.utils.exception;

import java.io.IOException;

/**
 * {@link RuntimeException} similar to {@link IOException}
 * 
 * @author omnaest
 */
public class RuntimeIOException extends RuntimeException
{
    private static final long serialVersionUID = 1717098863908697550L;

    public RuntimeIOException()
    {
        super();
    }

    public RuntimeIOException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RuntimeIOException(String message)
    {
        super(message);
    }

    public RuntimeIOException(Throwable cause)
    {
        super(cause);
    }

}
