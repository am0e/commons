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
package com.github.am0e.utils;

import java.util.concurrent.TimeUnit;

public class FrequencyInterval {
    private int interval = -1;
    private int frequency = -1;

    /**
     * Parses [0-9]+[DWMHYS] Where the number is parsed into the interval field
     * and DWMHYS is parsed into the frequency field. Use getIntervalInMS() to
     * get the interval value in MS.
     */
    public static FrequencyInterval parse(String expr) {
        FrequencyInterval it = new FrequencyInterval();

        if ("daily".equals(expr)) {
            it.frequency = Frequency.DAILY;
            it.interval = 1;

        } else if ("hourly".equals(expr)) {
            it.frequency = Frequency.HOURLY;
            it.interval = 1;

        } else if ("weekly".equals(expr)) {
            it.frequency = Frequency.WEEKLY;
            it.interval = 1;

        } else {
            char c = expr.charAt(0);

            if (Character.isDigit(c)) {
                // Get the last character, this is the frequency recurrence
                // rule.
                //
                c = Character.toUpperCase(expr.charAt(expr.length() - 1));

                // Get the integer string.
                //
                String is = expr.substring(0, expr.length() - 1);

                // Parse the interval.
                //
                it.interval = Integer.parseInt(is);
            }
            if (c == 'D') {
                it.frequency = Frequency.DAILY;
            } else if (c == 'W') {
                it.frequency = Frequency.WEEKLY;
            } else if (c == 'N') {
                it.frequency = Frequency.MONTHLY;
            } else if (c == 'H') {
                it.frequency = Frequency.HOURLY;
            } else if (c == 'Y') {
                it.frequency = Frequency.YEARLY;
            } else if (c == 'M') {
                it.frequency = Frequency.MINUTELY;
            } else if (c == 'S') {
                it.frequency = Frequency.SECONDLY;
            } else {
                throw Validate.illegalArgument("frequency");
            }
        }

        return it;
    }

    public int interval() {
        return interval;
    }

    public int frequency() {
        return frequency;
    }

    public long intervalAsMilliseconds() {
        switch (frequency) {
        case Frequency.MILLISECONDLY:
            return interval;
        case Frequency.DAILY:
            return TimeUnit.DAYS.toMillis(interval);
        case Frequency.WEEKLY:
            return TimeUnit.DAYS.toMillis(interval * 7);
        case Frequency.MONTHLY:
            return TimeUnit.DAYS.toMillis(interval * 30);
        case Frequency.HOURLY:
            return TimeUnit.HOURS.toMillis(interval);
        case Frequency.MINUTELY:
            return TimeUnit.MINUTES.toMillis(interval);
        case Frequency.SECONDLY:
            return TimeUnit.SECONDS.toMillis(interval);
        default:
            return 0;
        }
    }

}
