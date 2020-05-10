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
}
