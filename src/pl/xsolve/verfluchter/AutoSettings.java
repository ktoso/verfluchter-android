package pl.xsolve.verfluchter;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import pl.xsolve.verfluchter.activities.SettingsActivity;
import pl.xsolve.verfluchter.tools.Constants;
import pl.xsolve.verfluchter.tools.PasswdUtil;
import pl.xsolve.verfluchter.tools.UberSimplePasswdUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Konrad Ktoso Malawski
 */
public class AutoSettings extends Application {

    // My instance, "the one to rule them all"
    private static AutoSettings myInstance;

    // Logger tag
    private static final String TAG = "AutoSettings";

    public static final String SETTINGS_NAME = "autoSettings";

    protected Map<String, Object> settings = new HashMap<String, Object>();

    PasswdUtil passwdUtil = new UberSimplePasswdUtil();

    private static final String INT_SUFFIX = "_I";
    private static final String FLOAT_SUFFIX = "_F";
    private static final String STRING_SUFFIX = "_S";
    private static final String BOOLEAN_SUFFIX = "_B";
    private static final String LONG_SUFFIX = "_L";

    public static final String SERVER_DOMAIN_S = "SERVER_DOMAIN_S";
    public static final String MY_AUTH_USER_S = "MY_AUTH_USER_S";
    public static final String MY_AUTH_PASS_S = "MY_AUTH_PASS_S";
    public static final String BASIC_AUTH_USER_S = "BASIC_AUTH_USER_S";
    public static final String BASIC_AUTH_PASS_S = "BASIC_AUTH_PASS_S";
    public static final String USE_REMINDER_SERVICE_B = "USE_REMINDER_SERVICE_B";
    public static final String USE_REFRESHER_SERVICE_B = "USE_REFRESHER_SERVICE_B";
    public static final String WORKING_HOURS_START_HOUR_I = "WORKING_HOURS_START_HOUR_I";
    public static final String WORKING_HOURS_START_MIN_I = "WORKING_HOURS_START_MIN_I";
    public static final String WORKING_HOURS_END_HOUR_I = "WORKING_HOURS_END_HOUR_I";
    public static final String WORKING_HOURS_END_MIN_I = "WORKING_HOURS_END_MIN_I";

    private AutoSettings() {
        //default values and automatic setting+loading setup
        settings.put(SERVER_DOMAIN_S, Constants.DEFAULT.SERVER_DOMAIN);
        settings.put(MY_AUTH_USER_S, null);
        settings.put(MY_AUTH_PASS_S, null);
        settings.put(BASIC_AUTH_USER_S, Constants.DEFAULT.BASIC_AUTH_USER);
        settings.put(BASIC_AUTH_PASS_S, Constants.DEFAULT.BASIC_AUTH_PASS);
        settings.put(USE_REMINDER_SERVICE_B, Constants.DEFAULT.USE_REMINDER_SERVICE);
        settings.put(USE_REFRESHER_SERVICE_B, Constants.DEFAULT.USE_REFRESHER_SERVICE);
        settings.put(WORKING_HOURS_START_HOUR_I, Constants.DEFAULT.WORKING_HOURS_START_HOUR);
        settings.put(WORKING_HOURS_START_MIN_I, Constants.DEFAULT.WORKING_HOURS_START_MIN);
        settings.put(WORKING_HOURS_END_HOUR_I, Constants.DEFAULT.WORKING_HOURS_END_HOUR);
        settings.put(WORKING_HOURS_END_MIN_I, Constants.DEFAULT.WORKING_HOURS_END_MIN);

//        restoreSettings();
    }

    public static AutoSettings getInstance() {
        if (myInstance == null) {
            myInstance = new AutoSettings();
        }
        return myInstance;
    }

    /**
     * Restore preferences and load them into our variables
     */
    public void restoreSettings(SharedPreferences preferences) {
//        SharedPreferences preferences = getSharedPreferences(SETTINGS_NAME, 0);
        for (String key : settings.keySet()) {
            Object value = restoreFromPreferences(preferences, key);
//            if (key.contains("PASS") && value != null) {
//                value = passwdUtil.decrypt((String) value);
//            }
            if (value != null) {
                settings.put(key, value);
            }
        }
    }

    /**
     * Persist our cache into persistent SharedPreferences
     */
    public void persistSettings(SharedPreferences preferences) {
        if (!getClass().equals(SettingsActivity.class)) {
            return;
        }

        Log.d(TAG, "Persisting autoSettings: " + settings);

        // We need an Editor object to make preference changes.
//        SharedPreferences preferences = getSharedPreferences(SETTINGS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        for (String key : settings.keySet()) {
            Object value = getSetting(key);
//            if (key.contains("PASS") && value != null) {
//                value = passwdUtil.encrypt((String) value);
//            }
            persistIntoPreferencesEditor(editor, key, value);
        }

        editor.commit(); // Commit the edits!
    }

    private Object restoreFromPreferences(SharedPreferences preferences, String key) {
        if (key.endsWith(STRING_SUFFIX)) {
            return preferences.getString(key, null);
        } else if (key.endsWith(BOOLEAN_SUFFIX)) {
            return preferences.getBoolean(key, false);
        } else if (key.endsWith(INT_SUFFIX)) {
            return preferences.getInt(key, 0);
        } else if (key.endsWith(FLOAT_SUFFIX)) {
            return preferences.getFloat(key, 0);
        } else if (key.endsWith(LONG_SUFFIX)) {
            return preferences.getLong(key, 0);
        }
        return null;
    }

    private void persistIntoPreferencesEditor(SharedPreferences.Editor editor, String key, Object value) {
        if (key.endsWith(STRING_SUFFIX) || value == null) {
            editor.putString(key, (String) value);
        } else if (key.endsWith(BOOLEAN_SUFFIX)) {
            editor.putBoolean(key, (Boolean) value);
        } else if (key.endsWith(INT_SUFFIX)) {
            editor.putInt(key, (Integer) value);
        } else if (key.endsWith(FLOAT_SUFFIX)) {
            editor.putFloat(key, (Float) value);
        } else if (key.endsWith(LONG_SUFFIX)) {
            editor.putLong(key, (Long) value);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getSetting(String key) {
        return (T) settings.get(key);
    }

    public String getSettingString(String key) {
        return (String) settings.get(key);
    }

    public Boolean getSettingBoolean(String key) {
        return (Boolean) settings.get(key);
    }

    public <T> void setSetting(String key, T value) {
        settings.put(key, value);
    }

    public String print() {
        StringBuilder sb = new StringBuilder("-- AutoSettings --\n");
        sb.append(settings).append("\n");
        return sb.append("------------------\n").toString();
    }
}