package com.github.am0e.utils;

/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;

/**
 * A helper class that parses Dates out of Strings with date time in RFC822 and
 * W3CDateTime formats plus the variants Atom (0.3) and RSS (0.9, 0.91, 0.92,
 * 0.93, 0.94, 1.0 and 2.0) specificators added to those formats.
 * <p/>
 * It uses the JDK java.text.SimpleDateFormat class attemtping the parse using a
 * mask for each one of the possible formats.
 * <p/>
 *
 * @author Alejandro Abdelnur
 *
 */
public final class CommonDates {

    private static String[] ADDITIONAL_MASKS;

    // order is like this because the SimpleDateFormat.parse does not fail with
    // exception
    // if it can parse a valid date out of a substring of the full string given
    // the mask
    // so we have to check the most complete format first, then it fails with
    // exception
    private static final String[] RFC822_MASKS = { "EEE, dd MMM yy HH:mm:ss z", "EEE, dd MMM yy HH:mm z",
            "dd MMM yy HH:mm:ss z", "dd MMM yy HH:mm z" };

    // order is like this because the SimpleDateFormat.parse does not fail with
    // exception
    // if it can parse a valid date out of a substring of the full string given
    // the mask
    // so we have to check the most complete format first, then it fails with
    // exception
    private static final String[] W3CDATETIME_MASKS = { "yyyy-MM-dd'T'HH:mm:ss.SSSz", "yyyy-MM-dd't'HH:mm:ss.SSSz",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd't'HH:mm:ss.SSS'z'", "yyyy-MM-dd'T'HH:mm:ssz",
            "yyyy-MM-dd't'HH:mm:ssz", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd't'HH:mm:ss'z'", "yyyy-MM-dd'T'HH:mmz", // together
                                                                                                                     // with
                                                                                                                     // logic
                                                                                                                     // in
                                                                                                                     // the
                                                                                                                     // parseW3CDateTime
                                                                                                                     // they
            "yyyy-MM'T'HH:mmz", // handle W3C dates without time forcing them to
                                // be GMT
            "yyyy'T'HH:mmz", "yyyy-MM-dd't'HH:mmz", "yyyy-MM-dd'T'HH:mm'Z'", "yyyy-MM-dd't'HH:mm'z'", "yyyy-MM-dd",
            "yyyy-MM", "yyyy" };

    /** Base of nanosecond timings, to avoid wrapping */
    static final long NANO_ORIGIN = System.nanoTime();

    /**
     * Private constructor to avoid DateParser instances creation.
     */
    private CommonDates() {
    }

    /**
     * Parses a Date out of a string using an array of masks.
     * <p/>
     * It uses the masks in order until one of them succedes or all fail.
     * <p/>
     *
     * @param masks
     *            array of masks to use for parsing the string
     * @param sDate
     *            string to parse for a date.
     * @return the Date represented by the given string using one of the given
     *         masks. It returns <b>null</b> if it was not possible to parse the
     *         the string with any of the masks.
     *
     */
    private static Date parseUsingMask(String[] masks, String sDate) {
        sDate = (sDate != null) ? sDate.trim() : null;
        ParsePosition pp = null;
        Date d = null;
        for (int i = 0; d == null && i < masks.length; i++) {
            DateFormat df = new SimpleDateFormat(masks[i], Locale.US);
            // df.setLenient(false);
            df.setLenient(true);
            try {
                pp = new ParsePosition(0);
                d = df.parse(sDate, pp);
                if (pp.getIndex() != sDate.length()) {
                    d = null;
                }
                // System.out.println("pp["+pp.getIndex()+"] s["+sDate+"
                // m["+masks[i]+"] d["+d+"]");
            } catch (Exception ex1) {
                // System.out.println("s: "+sDate+" m: "+masks[i]+" d: "+null);
            }
        }
        return d;
    }

    /**
     * Parses a Date out of a String with a date in RFC822 format.
     * <p/>
     * It parsers the following formats:
     * <ul>
     * <li>"EEE, dd MMM yyyy HH:mm:ss z"</li>
     * <li>"EEE, dd MMM yyyy HH:mm z"</li>
     * <li>"EEE, dd MMM yy HH:mm:ss z"</li>
     * <li>"EEE, dd MMM yy HH:mm z"</li>
     * <li>"dd MMM yyyy HH:mm:ss z"</li>
     * <li>"dd MMM yyyy HH:mm z"</li>
     * <li>"dd MMM yy HH:mm:ss z"</li>
     * <li>"dd MMM yy HH:mm z"</li>
     * </ul>
     * <p/>
     * Refer to the java.text.SimpleDateFormat javadocs for details on the
     * format of each element.
     * <p/>
     * 
     * @param sDate
     *            string to parse for a date.
     * @return the Date represented by the given RFC822 string. It returns
     *         <b>null</b> if it was not possible to parse the given string into
     *         a Date.
     *
     */
    public static Date parseRFC822(String sDate) {
        int utIndex = sDate.indexOf(" UT");
        if (utIndex > -1) {
            String pre = sDate.substring(0, utIndex);
            String post = sDate.substring(utIndex + 3);
            sDate = pre + " GMT" + post;
        }
        return parseUsingMask(RFC822_MASKS, sDate);
    }

    /**
     * Parses a Date out of a String with a date in W3C date-time format.
     * <p/>
     * It parsers the following formats:
     * <ul>
     * <li>"yyyy-MM-dd'T'HH:mm:ssz"</li>
     * <li>"yyyy-MM-dd'T'HH:mmz"</li>
     * <li>"yyyy-MM-dd"</li>
     * <li>"yyyy-MM"</li>
     * <li>"yyyy"</li>
     * </ul>
     * <p/>
     * Refer to the java.text.SimpleDateFormat javadocs for details on the
     * format of each element.
     * <p/>
     * 
     * @param sDate
     *            string to parse for a date.
     * @return the Date represented by the given W3C date-time string. It
     *         returns <b>null</b> if it was not possible to parse the given
     *         string into a Date.
     *
     */
    public static Date parseW3CDateTime(String sDate) {
        // if sDate has time on it, it injects 'GTM' before de TZ displacement
        // to
        // allow the SimpleDateFormat parser to parse it properly
        int tIndex = sDate.indexOf("T");
        if (tIndex > -1) {
            if (sDate.endsWith("Z")) {
                sDate = sDate.substring(0, sDate.length() - 1) + "+00:00";
            }
            int tzdIndex = sDate.indexOf("+", tIndex);
            if (tzdIndex == -1) {
                tzdIndex = sDate.indexOf("-", tIndex);
            }
            if (tzdIndex > -1) {
                String pre = sDate.substring(0, tzdIndex);
                int secFraction = pre.indexOf(",");
                if (secFraction > -1) {
                    pre = pre.substring(0, secFraction);
                }
                String post = sDate.substring(tzdIndex);
                sDate = pre + "GMT" + post;
            }
        } else {
            sDate += "T00:00GMT";
        }
        return parseUsingMask(W3CDATETIME_MASKS, sDate);
    }

    /**
     * Parses a Date out of a String with a date in W3C date-time format or in a
     * RFC822 format.
     * <p>
     * 
     * @param sDate
     *            string to parse for a date.
     * @return the Date represented by the given W3C date-time string. It
     *         returns <b>null</b> if it was not possible to parse the given
     *         string into a Date.
     *
     */
    public static Date parseDate(String sDate) {
        Date d = parseW3CDateTime(sDate);
        if (d == null) {
            d = parseRFC822(sDate);
            if (d == null && ADDITIONAL_MASKS.length > 0) {
                d = parseUsingMask(ADDITIONAL_MASKS, sDate);
            }
        }
        return d;
    }

    /**
     * create a RFC822 representation of a date.
     * <p/>
     * Refer to the java.text.SimpleDateFormat javadocs for details on the
     * format of each element.
     * <p/>
     * 
     * @param date
     *            Date to parse
     * @return the RFC822 represented by the given Date It returns <b>null</b>
     *         if it was not possible to parse the date.
     *
     */
    public static String formatRFC822(Date date) {
        SimpleDateFormat dateFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        dateFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormater.format(date);
    }

    /**
     * create a W3C Date Time representation of a date which is based on ISO
     * 8601.
     * <p/>
     * Refer to the java.text.SimpleDateFormat javadocs for details on the
     * format of each element.
     * <p/>
     * 
     * @param date
     *            Date to parse
     * @return the W3C Date Time represented by the given Date It returns
     *         <b>null</b> if it was not possible to parse the date.
     *
     */
    public static String formatW3CDateTime(Date date) {
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        dateFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormater.format(date);
    }

    @SuppressWarnings("deprecation")
    public static Date parseDateYYYYMMDD(String s) {
        // Convert ingram date: YYYYMMDD
        //
        return new Date(Integer.parseInt(s.substring(0, 4)) - 1900, Integer.parseInt(s.substring(4, 6)) - 1,
                Integer.parseInt(s.substring(6, 8)));
    }

    @SuppressWarnings("deprecation")
    public static Date parseDateYYYY_MM_DD(String s) {
        // Convert ingram date: YYYY-MM-DD
        //
        return new Date(Integer.parseInt(s.substring(0, 4)) - 1900, Integer.parseInt(s.substring(5, 7)) - 1,
                Integer.parseInt(s.substring(8, 10)));
    }

    @SuppressWarnings("deprecation")
    public static Date parseDateDD_MM_YYYY(String s) {
        // Convert ingram date: DD-MM-YYYY
        // 01234567890
        return new Date(Integer.parseInt(s.substring(6, 10)) - 1900, Integer.parseInt(s.substring(3, 5)) - 1,
                Integer.parseInt(s.substring(0, 2)));
    }

    public static long timeDiff(long start) {
        long now = System.currentTimeMillis();
        return (now > start) ? now - start : start - now;
    }

    public static long timeDiff(Date d1, Date d2) {
        long l1 = d1.getTime();
        long l2 = d2.getTime();
        return (l1 > l2) ? l1 - l2 : l2 - l1;
    }

    public static long nanoNow() {
        return System.nanoTime() - NANO_ORIGIN;
    }

    public static Timestamp timestampNow() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * returns the date now, the time part is always 00:00:00
     */
    public static Date dateNow() {
        return DateUtils.truncate(new Date(), Calendar.DATE);
    }

    public static Date dateTimeNow() {
        return new Date();
    }

    public static long formatElapsedTime(StringBuilder sb, String label, long s, long elapsed) {
        if (elapsed >= s) {
            long v = (elapsed / s);
            sb.append(v);
            sb.append(label);
            sb.append("  ");
            elapsed = (elapsed % s);
        }
        return elapsed;
    }

    /**
     * Formats elapsed time as "2d 12h", "12h 2m", "2m 1s". Only 2 units are
     * shown.
     */
    public static String formatElapsedTime(long elapsed, boolean includeSecs, boolean includeMilliSecs) {
        if ((elapsed % 1000) != 0 && elapsed < 30000) {
            includeMilliSecs = true;
        }

        StringBuilder sb = new StringBuilder();
        boolean hasDays = elapsed >= DateUtils.MILLIS_PER_DAY;
        elapsed = formatElapsedTime(sb, " d", DateUtils.MILLIS_PER_DAY, elapsed);
        boolean hasHours = elapsed >= DateUtils.MILLIS_PER_HOUR;
        elapsed = formatElapsedTime(sb, " h", DateUtils.MILLIS_PER_HOUR, elapsed);
        if (!hasDays) {
            boolean hasMins = elapsed >= DateUtils.MILLIS_PER_MINUTE;
            elapsed = formatElapsedTime(sb, " m", DateUtils.MILLIS_PER_MINUTE, elapsed);
            if (!hasHours && includeSecs) {
                elapsed = formatElapsedTime(sb, " s", DateUtils.MILLIS_PER_SECOND, elapsed);
                if (!hasMins && includeMilliSecs) {
                    if (elapsed >= 1) {
                        sb.append(elapsed);
                        sb.append(" ms");
                    }
                }
            }
        }
        return sb.toString().trim();
    }

    public static String formatShortDateTime(Date date) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
    }

    public static String ordinal(int dayOfMonth) {
        int mod = dayOfMonth % 100;
        if ((mod >= 10) && (mod <= 20))
            return "th";

        switch (dayOfMonth % 10) {
        case 1:
            return "st";
        case 2:
            return "nd";
        case 3:
            return "rd";
        default:
            return "th";
        }
    }

}
