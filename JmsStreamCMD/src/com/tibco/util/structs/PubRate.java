/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.structs;

/**
 * Title:        PubRate
 * Description:  This class is used to set the playback rate.<p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "WeakerAccess"})
public final class PubRate
{
    public int intRate;
    public long intMilli;

    public PubRate()
    {
        intRate = 0;
        intMilli = 0;
    }

    public PubRate(float fltRate)
    {
        setRate(fltRate);
    }
    /**
     * Set the playback rate.<p>
     * In order to allow fractional rates i.e. (0.5 msg/sec) we have to take the integer value and put it as the rate.
     * Then take the decimal value a adjust the _intMillis wait time to get an accurate msg/sec rate.<p>
     * @param fltRate  The rate of publication (msg/sec).
     */
    public void setRate(Float fltRate)
    {
        if (fltRate == 0) intRate = 0;
        else {
            intRate = Math.round(fltRate.floatValue());
            if (fltRate < 1) {
                //  Send less than one msg a sec.
                intRate = 1;
                intMilli = Math.round(1000 / fltRate.doubleValue());
            }
            else if (fltRate.doubleValue() != Math.rint(fltRate.doubleValue())) {
                // fltRate is NOT an integer.  Round the value so we an just increase or decrease the _intMillis wait
                // by 50%.  i.e. between 500 and 1500 milliseconds.
                intRate = Math.round(fltRate.floatValue());
                double dblAdj = fltRate.doubleValue() < Math.rint(fltRate) ?
                                (fltRate.doubleValue() - Math.floor(fltRate.doubleValue())) :
                                1 + (fltRate.doubleValue() - Math.floor(fltRate.doubleValue()));
                intMilli = Math.round(1000 / dblAdj);
            }
            else {
                // fltRate is an integer.
                intRate = fltRate.intValue();
                intMilli = 1000;
            }
        }
    }
}
