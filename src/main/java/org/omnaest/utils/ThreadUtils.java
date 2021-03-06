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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.omnaest.utils.duration.TimeDuration;
import org.omnaest.utils.exception.handler.ExceptionHandler;

public class ThreadUtils
{
    public static void sleepSilently(int duration, TimeUnit timeUnit)
    {
        ExceptionHandler exceptionHandler = null;
        sleepSilently(duration, timeUnit, exceptionHandler);
    }

    public static void sleepSilently(int duration, TimeUnit timeUnit, ExceptionHandler exceptionHandler)
    {
        try
        {
            Thread.sleep(timeUnit.toMillis(duration));
        }
        catch (InterruptedException e)
        {
            if (exceptionHandler != null)
            {
                exceptionHandler.accept(e);
            }
        }
    }

    public static void sleepSilently(TimeDuration timeDuration)
    {
        sleepSilently((int) timeDuration.getDuration(), timeDuration.getTimeUnit());
    }

    public static void sleepSilently(Duration duration)
    {
        if (duration != null)
        {
            sleepSilently((int) duration.get(ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
        }
    }
}
