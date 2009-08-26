//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2008 Michael Bayne
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A fluent approach to dealing with {@link Calendar} and some calendar related utilities.
 * Instead of:
 * <pre>
 * Calendar cal = Calendar.getInstance();
 * cal.set(Calendar.HOUR_OF_DAY, 0);
 * cal.set(Calendar.MINUTE, 0);
 * cal.set(Calendar.SECOND, 0);
 * cal.set(Calendar.MILLISECOND, 0);
 * cal.add(Calendar.DATE, 1);
 * long nextMidnight = cal.getTimeInMillis();
 * </pre>
 * you can simply write:
 * <pre>
 * long nextMidnight = Calendars.now().zeroTime().addDay(1).toTime();
 * </pre>
 */
public class Calendars
{
    /** Provides fluent methods for operating on a {@link Calendar}. */
    public static class Builder
    {
        /** Adds the specified value to the {@link Calendar#YEAR} field. Use negative values to
         * subtract. */
        public Builder addYears (int years) {
            return add(Calendar.YEAR, years);
        }

        /** Adds the specified value to the {@link Calendar#MONTH} field. Use negative values to
         * subtract. */
        public Builder addMonths (int months) {
            return add(Calendar.MONTH, months);
        }

        /** Adds the specified value to the {@link Calendar#DATE} field. Use negative values to
         * subtract. */
        public Builder addDays (int days) {
            return add(Calendar.DATE, days);
        }

        /** Sets the {@link Calendar#HOUR_OF_DAY} field to the specified value. This uses a 24-hour
         * clock. Midnight is 0. */
        public Builder setHour (int hour) {
            return set(Calendar.HOUR_OF_DAY, hour);
        }

        /** Adds the specified value to the {@link Calendar#HOUR_OF_DAY} field. Use negative values
         * to subtract. */
        public Builder addHours (int hours) {
            return add(Calendar.HOUR_OF_DAY, hours);
        }

        /** Sets the {@link Calendar#MINUTE} field to the specified value. */
        public Builder setMinute (int minute) {
            return set(Calendar.MINUTE, minute);
        }

        /** Adds the specified value to the {@link Calendar#MINUTE} field. Use negative values to
         * subtract. */
        public Builder addMinutes (int minutes) {
            return add(Calendar.MINUTE, minutes);
        }

        /** See {@link Calendar#add}. */
        public Builder add (int field, int amount) {
            _calendar.add(field, amount);
            return this;
        }

        /** See {@link Calendar#roll}. */
        public Builder roll (int field, boolean up) {
            _calendar.roll(field, up);
            return this;
        }

        /** See {@link Calendar#set(int,int)}. */
        public Builder set (int field, int value) {
            _calendar.set(field, value);
            return this;
        }

        /** Zeros out the time fields of this calendar, preserving only the date. */
        public Builder zeroTime () {
            _calendar.set(Calendar.HOUR_OF_DAY, 0);
            _calendar.set(Calendar.MINUTE, 0);
            _calendar.set(Calendar.SECOND, 0);
            _calendar.set(Calendar.MILLISECOND, 0);
            return this;
        }

        /** Returns the milliseconds since the epoch for our calendar's current time. */
        public long toTime () {
            return _calendar.getTimeInMillis();
        }

        /** Returns a {@link Date} configured with our calendar's current time. */
        public Date toDate () {
            return _calendar.getTime();
        }

        /** Returns a {@link java.sql.Date} configured with our calendar's current time. */
        public java.sql.Date toSQLDate () {
            return new java.sql.Date(toTime());
        }

        /** Returns a {@link java.sql.Timestamp} configured with our calendar's current time. */
        public Timestamp toTimestamp () {
            return new Timestamp(toTime());
        }

        /** See {@link Calendar#get}. */
        public int get (int field) {
            return _calendar.get(field);
        }

        /** Returns the wrapped {@link Calendar} instance. <em>Note:</em> modifications to the
         * returned calendar will also affect this instance. */
        public Calendar asCalendar () {
            return _calendar;
        }

        protected Builder (Calendar calendar) {
            _calendar = calendar;
        }

        protected final Calendar _calendar;
    }

    /**
     * Returns a fluent wrapper around the supplied calendar.
     */
    public static Builder with (Calendar calendar)
    {
        return new Builder(calendar);
    }

    /**
     * Returns a fluent wrapper around a calendar obtained with {@link Calendar#getInstance}.
     */
    public static Builder now ()
    {
        return with(Calendar.getInstance());
    }

    /**
     * Returns a fluent wrapper around a calendar configured with the specified time.
     */
    public static Builder at (long millis)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return with(calendar);
    }

    /**
     * Returns a fluent wrapper around a calendar configured to Midnight on specified day in the
     * specified month and year. Note: be sure to use {@link Calendar#JANUARY}, etc. for the month
     * argument or be prepared rue your folly.
     */
    public static Builder at (int year, int month, int day)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return with(calendar).zeroTime();
    }

    /**
     * Returns a fluent wrapper around a calendar configured with the specified time.
     */
    public static Builder at (Date when)
    {
        return at(when.getTime());
    }

    /**
     * Returns a fluent wrapper around a calendar for the specifed time zone.
     */
    public static Builder in (TimeZone zone)
    {
        return with(Calendar.getInstance(zone));
    }

    /**
     * Returns a fluent wrapper around a calendar for the specifed locale.
     */
    public static Builder in (Locale locale)
    {
        return with(Calendar.getInstance(locale));
    }

    /**
     * Returns a fluent wrapper around a calendar for the specifed time zone and locale.
     */
    public static Builder in (TimeZone zone, Locale locale)
    {
        return with(Calendar.getInstance(zone, locale));
    }

    /**
     * Returns the difference between the dates represented by the two calendars in days, properly
     * accounting for daylight savings time, leap seconds, etc. The order of the two dates in time
     * does not matter, the absolute number of days between them will be returned.
     *
     * <p> From: http://www.jguru.com/forums/view.jsp?EID=489372
     *
     * @return the number of days between d1 and d2, 0 if they are the same day.
     */
    public static int getDaysBetween (Calendar d1, Calendar d2)
    {
        if (d1.after(d2)) {  // swap dates so that d1 is start and d2 is end
            Calendar swap = d1;
            d1 = d2;
            d2 = swap;
        }

        int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
        int y2 = d2.get(Calendar.YEAR);
        if (d1.get(Calendar.YEAR) != y2) {
            d1 = (Calendar)d1.clone();
            do {
                days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);
                d1.add(Calendar.YEAR, 1);
            } while (d1.get(Calendar.YEAR) != y2);
        }
        return days;
    }

    /**
     * Returns the number of whole months between the dates represented by the two calendar
     * objects, truncating any remainder. The order of the two dates in time does not matter, the
     * absolute number of months between them will be returned.
     */
    public static int getMonthsBetween (Calendar start, Calendar end)
    {
        if (end.before(start)) {
            Calendar swap = start;
            start = end;
            end = swap;
        }

        // we're going to manipulate end, so let's clone it
        end = (Calendar)end.clone();

        int months = -1;
        do {
            end.add(Calendar.MONTH, -1);
            months++;
        } while (!start.after(end));

        return months;
    }
}