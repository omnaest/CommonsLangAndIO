/*******************************************************************************
 * Copyright 2026 Danny Kunz
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
package org.apache.commons.lang.time;

import java.util.Calendar;
import java.util.Date;

/**
 * Migration shim for the discontinued commons-lang 2.x {@link org.apache.commons.lang.time.DateUtils}.
 *
 * @deprecated use {@link org.apache.commons.lang3.time.DateUtils} instead. Replace the import
 *             <code>org.apache.commons.lang.time.DateUtils</code> with <code>org.apache.commons.lang3.time.DateUtils</code>; every
 *             method below exists there under the same name and signature. For new code prefer
 *             {@link java.time.LocalDate} / {@link java.time.LocalDateTime}.
 * @author omnaest
 */
@Deprecated(forRemoval = true)
public class DateUtils
{
    public static Date addYears(Date date, int amount)
    {
        return org.apache.commons.lang3.time.DateUtils.addYears(date, amount);
    }

    public static Date addDays(Date date, int amount)
    {
        return org.apache.commons.lang3.time.DateUtils.addDays(date, amount);
    }

    public static Calendar toCalendar(Date date)
    {
        return org.apache.commons.lang3.time.DateUtils.toCalendar(date);
    }
}
