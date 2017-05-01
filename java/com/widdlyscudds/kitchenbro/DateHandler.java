package com.widdlyscudds.kitchenbro;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.SimpleTimeZone;
public class DateHandler extends GregorianCalendar {
    public Calendar calendar;
    public DateHandler() {
        // get the supported ids for GMT-08:00 (Pacific Standard Time)
        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
        // if no ids were returned, something is wrong. get out.
        if (ids.length == 0)
            System.exit(0);

        // begin output
        System.out.println("Current Time");

        // create a Pacific Standard Time time zone
        SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);

        // set up rules for Daylight Saving Time
        pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

        // create a GregorianCalendar with the Pacific Daylight time zone
        // and the current date and time
        //calendar = new GregorianCalendar(pdt);
        this.setTimeZone(pdt);
        Date trialTime = new Date();
        this.setTime(trialTime);

        //System.out.println(getDate(this.get(Calendar.YEAR),this.get(Calendar.MONTH),this.get(Calendar.DAY_OF_MONTH),this.get(Calendar.DAY_OF_WEEK)));
    }
    public String getDate() {
        String m = ""; String wd = "";
        switch (get(Calendar.MONTH)) {
            case 0:
                m = "January";
                break;
            case 1:
                m = "February";
                break;
            case 2:
                m = "March";
                break;
            case 3:
                m = "April";
                break;
            case 4:
                m = "May";
                break;
            case 5:
                m = "June";
                break;
            case 6:
                m = "July";
                break;
            case 7:
                m = "August";
                break;
            case 8:
                m = "September";
                break;
            case 9:
                m = "October";
                break;
            case 10:
                m = "November";
                break;
            case 11:
                m = "December";
                break;
        }
        switch (get(Calendar.DAY_OF_WEEK)) {
            case 1:
                wd = "Sunday";
                break;
            case 2:
                wd = "Monday";
                break;
            case 3:
                wd = "Tuesday";
                break;
            case 4:
                wd = "Wednesday";
                break;
            case 5:
                wd = "Thursday";
                break;
            case 6:
                wd = "Friday";
                break;
            case 7:
                wd = "Saturday";
                break;
        }
        return wd + "\n" + m + " " + get(Calendar.DAY_OF_MONTH) + ", " + get(Calendar.YEAR);
    }
}