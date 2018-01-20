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

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Function;

public class NumberUtils
{
    /**
     * Allows to format {@link Double} and {@link Float} {@link Number}s<br>
     * <br>
     * Example:<br>
     * 
     * <pre>
     * String result = NumberUtils.formatter()
     *                            .withPercentage()
     *                            .format(0.05);
     * </pre>
     * 
     * @author omnaest
     */
    public static interface NumberFormatter
    {
        /**
         * Sets the {@link Locale} for the {@link NumberFormatter}
         * 
         * @param locale
         * @return
         */
        public NumberFormatter forLocale(Locale locale);

        /**
         * Formats the given {@link Double} value
         * 
         * @param value
         * @return
         */
        public String format(double value);

        /**
         * Formats the given {@link Float} value
         * 
         * @param value
         * @return
         */
        public String format(float value);

        /**
         * Formats the given {@link Long} value
         * 
         * @param value
         * @return
         */
        public String format(long value);

        public NumberFormatter withMinimumFractionDigits(int minimumFractionDigits);

        public NumberFormatter withMaximumFractionDigits(int maximumFractionDigits);

        public NumberFormatter withMaximumIntegerDigits(int maximumIntegerDigits);

        public NumberFormatter withMinimumIntegerDigits(int minimumIntegerDigits);

        public NumberFormatter withThousandSeparator();

        /**
         * Defines the render syntax to be e.g. like "0.1%"
         * 
         * @return
         */
        public NumberFormatter withPercentage();

    }

    /**
     * Returns a {@link NumberFormatter}
     * 
     * @return
     */
    public static NumberFormatter formatter()
    {
        return new NumberFormatter()
        {
            private Locale                         locale                = Locale.US;
            private Integer                        minimumFractionDigits = null;
            private Integer                        maximumFractionDigits = null;
            private Integer                        minimumIntegerDigits  = null;
            private Integer                        maximumIntegerDigits  = null;
            private boolean                        useThousandSeparator  = false;
            private Function<Locale, NumberFormat> formatterFactory      = locale ->
                                                                         {
                                                                             return locale != null ? NumberFormat.getNumberInstance(locale)
                                                                                     : NumberFormat.getNumberInstance();
                                                                         };

            @Override
            public NumberFormatter withThousandSeparator()
            {
                this.useThousandSeparator = true;
                return this;
            }

            @Override
            public NumberFormatter withPercentage()
            {
                this.formatterFactory = locale -> locale != null ? NumberFormat.getPercentInstance(locale) : NumberFormat.getPercentInstance();
                if (this.maximumFractionDigits == null)
                {
                    this.maximumFractionDigits = 2;
                }
                return this;
            }

            @Override
            public NumberFormatter forLocale(Locale locale)
            {
                this.locale = locale;
                return this;
            }

            @Override
            public NumberFormatter withMinimumIntegerDigits(int minimumIntegerDigits)
            {
                this.minimumIntegerDigits = minimumIntegerDigits;
                return this;
            }

            @Override
            public NumberFormatter withMaximumIntegerDigits(int maximumIntegerDigits)
            {
                this.maximumIntegerDigits = maximumIntegerDigits;
                return this;
            }

            @Override
            public NumberFormatter withMinimumFractionDigits(int minimumFractionDigits)
            {
                this.minimumFractionDigits = minimumFractionDigits;
                return this;
            }

            @Override
            public NumberFormatter withMaximumFractionDigits(int maximumFractionDigits)
            {
                this.maximumFractionDigits = maximumFractionDigits;
                return this;
            }

            @Override
            public String format(double value)
            {
                return this.createNumberFormatInstance()
                           .format(value);
            }

            @Override
            public String format(long value)
            {
                return this.createNumberFormatInstance()
                           .format(value);
            }

            private NumberFormat createNumberFormatInstance()
            {
                NumberFormat retval = this.formatterFactory.apply(this.locale);

                retval.setMinimumFractionDigits(ObjectUtils.defaultIfNull(this.minimumFractionDigits, 0));
                retval.setMaximumFractionDigits(ObjectUtils.defaultIfNull(this.maximumFractionDigits, 20));
                retval.setMinimumIntegerDigits(ObjectUtils.defaultIfNull(this.minimumIntegerDigits, 1));
                retval.setMaximumIntegerDigits(ObjectUtils.defaultIfNull(this.maximumIntegerDigits, Integer.MAX_VALUE));
                retval.setGroupingUsed(this.useThousandSeparator);
                return retval;
            }

            @Override
            public String format(float value)
            {
                return this.createNumberFormatInstance()
                           .format(value);
            }
        };
    }

    /**
     * @see org.apache.commons.lang3.math.NumberUtils#toDouble(String)
     * @param str
     * @return
     */
    public static double toDouble(String str)
    {
        return org.apache.commons.lang3.math.NumberUtils.toDouble(str);
    }

    /**
     * @see org.apache.commons.lang3.math.NumberUtils#toInt(String)
     * @param str
     * @return
     */
    public static int toInt(String str)
    {
        return org.apache.commons.lang3.math.NumberUtils.toInt(str);
    }
}
