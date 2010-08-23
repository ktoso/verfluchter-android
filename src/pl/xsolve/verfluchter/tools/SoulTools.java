package pl.xsolve.verfluchter.tools;

import android.util.Log;
import android.util.Pair;
import pl.xsolve.verfluchter.rest.RestResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A simple string helper class
 *
 * @author Konrad Ktoso Malawski
 */
public class SoulTools {
    static SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_YYYYMMDD);
    private static final String TAG = "SoulTools";

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

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * Checks if the now date is on an weekend
     *
     * @param now the calendar date to be checked
     * @return true if its on a weekend, false otherwise
     */
    public static boolean itsWeekend(GregorianCalendar now) {
        int today = now.get(Calendar.DAY_OF_WEEK);
        return !(today == Calendar.SUNDAY || today == Calendar.SATURDAY);
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
    public static Pair<Integer, Integer> convertTimeStringToIntPair(String string) {
        String[] hourAndMin = string.split(":");
        return new Pair<Integer, Integer>(Integer.valueOf(hourAndMin[0]), Integer.valueOf(hourAndMin[1]));
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
}
