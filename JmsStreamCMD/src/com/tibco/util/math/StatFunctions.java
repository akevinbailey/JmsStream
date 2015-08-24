/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.math;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"ForLoopReplaceableByForEach", "FieldCanBeLocal", "CanBeFinal", "WeakerAccess"})
public final class StatFunctions
{
    /**
     * This function will return the intervals between maxValue and minValue
     * but will not include minValue in the returned intervals.<p>
     *
     * @param maxValue          Max value
     * @param minValue          Min value
     * @param numOfIntervals    Number of intervals
     * @return the intervals
     */
    public static double[] linearIntervals(double maxValue, double minValue, int numOfIntervals)
    {
        double[] intervals =  new double[numOfIntervals];
        double[] x = new double[2];
        double[] y = new double[2];
        double b;

        x[0] = 0;
        x[1] = numOfIntervals;
        y[0] = minValue;
        y[1] = maxValue;
        // Use the standard linear regression formula
        b =  slope(x,y);
        for (int i=0; i < intervals.length; i++) intervals[i] = b*(i+1)+minValue;
        return intervals;
    }

    /**
     * Average or mean function.<p>
     *
     * @param array         An array of doubles to average
     * @return average of array
     */
    public static double avg(double[] array)
    {
        double total = 0;
        for (int i=0; i < array.length; i++) total += array[i];
        return total/array.length;
    }

    /**
     * Slope or linear regression function.<p>
     *
     * @param x     The x component of the line
     * @param y     The y component of the line
     * @return slope
     */
    public static double slope(double[] x, double[] y)
    {
        double b, a1=0, a2=0;
        double avgX = avg(x);
        double avgY = avg(y);
        for (int i=0; i < x.length; i++) a1 += (x[i]-avgX)*(y[i]-avgY);
        for (int i=0; i < x.length; i++) a2 += Math.pow(x[i]-avgX, 2);
        b = a1/a2;
        return b;
    }
}
