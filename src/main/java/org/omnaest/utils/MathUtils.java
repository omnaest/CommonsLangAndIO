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

import java.util.Collection;

public class MathUtils
{

    public static class AverageAndStandardDeviation
    {
        private double average;
        private double standardDeviation;

        public AverageAndStandardDeviation(double average, double standardDeviation)
        {
            super();
            this.average = average;
            this.standardDeviation = standardDeviation;
        }

        public double getAverage()
        {
            return this.average;
        }

        public double getStandardDeviation()
        {
            return this.standardDeviation;
        }

        @Override
        public String toString()
        {
            return "[average=" + this.average + ", standardDeviation=" + this.standardDeviation + "]";
        }

    }

    public static AverageAndStandardDeviation calculateAverage(Collection<? extends Number> values)
    {
        double average = values.stream()
                               .mapToDouble(v -> v.doubleValue())
                               .filter(v -> v > 0.01) //only use value >0 for the average
                               .average()
                               .orElse(0);
        double standardDeviation;
        if (values.isEmpty())
        {
            standardDeviation = 0.0;
        }
        else if (values.size() == 1)
        {
            standardDeviation = average;
        }
        else
        {
            standardDeviation = Math.sqrt(values.stream()
                                                .mapToDouble(v -> v.doubleValue())
                                                .map(v -> Math.pow(Math.abs(average - v), 2))
                                                .sum()
                    / (values.size() - 1));
        }
        return new AverageAndStandardDeviation(average, standardDeviation);

    }

    public static double random(double min, double max)
    {
        return Math.min(max, min) + Math.abs(max - min) * Math.random();
    }

    public static boolean randomBinary()
    {
        return Math.random() >= 0.5;
    }

    /**
     * Returns 2^power, which means 0 -> 2^0=1, 1 -> 2^1 = 2 , 2 -> 2^2 = 4, ...
     * 
     * @param power
     * @return
     */
    public static int pow2(int power)
    {
        return 1 << power;
    }
}
