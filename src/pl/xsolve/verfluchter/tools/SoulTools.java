/*
 * This file is part of verfluchter-android.
 *
 * verfluchter-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * verfluchter-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.xsolve.verfluchter.tools;

import android.util.Log;
import android.util.Pair;
import pl.xsolve.verfluchter.rest.RestResponse;
import pl.xsolve.verfluchter.rest.RestResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A simple toolbox/helper class
 *
 * @author Konrad Ktoso Malawski
 */
public class SoulTools {
    
    // logger tag
    private static final String TAG = SoulTools.class.getSimpleName();

    static SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_YYYYMMDD);

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8);
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static boolean hasText(String str) {
        return str != null
                && !str.equals("")
                && str.trim().length() > 0;
    }

    /**
     * Checks if the now date is on an weekend
     *
     * @param now the calendar date to be checked
     * @return true if its on a weekend, false otherwise
     */
    public static boolean itsWeekend(GregorianCalendar now) {
        int today = now.get(Calendar.DAY_OF_WEEK);
        return today == Calendar.SUNDAY || today == Calendar.SATURDAY;
    }

    public static boolean workTimeIsOver(GregorianCalendar now) {
        return now.get(Calendar.HOUR_OF_DAY) > Constants.DEFAULT.WORKING_HOURS_END_HOUR;
    }

    public static boolean workTimeHasntComeYet(GregorianCalendar now) {
        return now.get(Calendar.HOUR_OF_DAY) < Constants.DEFAULT.WORKING_HOURS_START_HOUR;
    }

    /**
     * Converts an "24:54" string into an Pair of 24h and 54min :-)
     *
     * @param string
     * @return
     */
    public static HourMin convertTimeStringToHourMin(String string) {
        String[] hourAndMin = string.split(":");
        return new HourMin(Integer.valueOf(hourAndMin[0]), Integer.valueOf(hourAndMin[1]));
    }

    /**
     * Converts an Pair of 24h and 54min into an "24:54" string :-)
     *
     * @param pair
     * @return
     */
    public static String convertIntPairToTimeString(Pair<Integer, Integer> pair) {
        return String.format("%d:%d", pair.first, pair.second);
    }

    public static String unNullify(String s) {
        return s == null ? "" : s;
    }

    public static boolean isResponseOK(RestResponse response) {
        return response != null
                && (response.getResponseCode() == Constants.HTTP_OK
                || response.getResponseCode() == Constants.HTTP_FOUND);
    }

    public static String getTodayDateString() {
        Calendar calendar = Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }

    //todo this method probably sux
    public static CharSequence getYesterdayString() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
            return sdf.format(calendar.getTime());
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.v(TAG, "Yesterday string creation failed");
        }
        return "";
    }

    public static String prefixHttpsIfNeeded(String domain) {
        if (domain.startsWith("http://") || domain.startsWith("http://")) {
            return domain;
        } else {
            return "https://" + domain;
        }
    }

    public static String removeHttpsPrefix(String domain) {
        return domain.replaceAll("https://", "");
    }

    public static String getDisplayDay(String datestring) {
        if (datestring.equals(getTodayDateString()))
            return "Dzisiaj";

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(datestring));
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            return Constants.weekDays[calendar.get(Calendar.DAY_OF_WEEK) - 2];
        } catch (ParseException e) {
            Log.d(TAG, "Invalid dateString supplied...");
            e.printStackTrace();
        }
        return "";
    }

    @SuppressWarnings({"ConstantConditions"})
    public static void throwExceptionIfResponseNotOk(RestResponse response) throws RestResponseException {
        if (!SoulTools.isResponseOK(response)) {
            Log.d(TAG, "But the response code is not 200...");
            Log.v(TAG, response == null ? "Response was null." : response.getResponse());
            throw new RestResponseException(response.getResponseCode());
        }
    }

    /**
     * This is an idiotic method for avoiding null pointers if an got setting for example is null
     * The properties will then return such Boolean bla = null; which renders getSetting() unusable in if statements
     * Thats what for this method is...
     * @param bool the "may be null" Boolean object
     * @return false if the Boolean was null or false, true otherwise
     */
    public static boolean isTrue(Boolean bool) {
        return bool == null ?  false : bool;
    }
}
                              