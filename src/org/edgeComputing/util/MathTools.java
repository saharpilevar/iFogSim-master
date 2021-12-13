package org.edgeComputing.util;

import java.util.List;


public class MathTools {

    public static double varianceDouble(List<Double> data) {
        double mean = meanDouble(data);
        double sum = 0;
        double diff = 0;
        for (Double d : data) {
            diff = d - mean;
            sum += Math.pow(diff, 2);
        }
        return sum / data.size();
    }

    public static double meanDouble(List<Double> data) {
        double val = sumDouble(data);
        return val / data.size();
    }

    public static double sumDouble(List<Double> data) {
        double val = 0;
        for (int i = 0; i < data.size(); ++i)
            val += data.get(i);
        return val;
    }

    public static double standardDeviationDouble(List<Double> data) {
        double sum = 0.0, standardDeviation = 0.0;

        double mean = meanDouble(data);

        for (double num : data) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / data.size());
    }



    public static double varianceLong(List<Long> data) {
        double mean = meanLong(data);
        double sum = 0;
        double diff = 0;
        for (Long d : data) {
            diff = d - mean;
            sum += Math.pow(diff, 2);
        }
        return sum / data.size();
    }

    public static double meanLong(List<Long> data) {
        double val = sumLong(data);
        return val / data.size();
    }

    public static double sumLong(List<Long> data) {
        double val = 0;
        for (int i = 0; i < data.size(); ++i)
            val += data.get(i);
        return val;
    }

    public static double standardDeviationLong(List<Long> data) {
        double sum = 0.0, standardDeviation = 0.0;

        double mean = meanLong(data);

        for (double num : data) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / data.size());
    }


}
