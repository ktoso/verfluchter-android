package pl.xsolve.verfluchter.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import pl.xsolve.verfluchter.R;
import pl.xsolve.verfluchter.tools.Constants;
import pl.xsolve.verfluchter.tools.PasswdUtil;
import pl.xsolve.verfluchter.tools.UberSimplePasswdUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Common class for all "View" Activities, is supports menu handling etc
 *
 * @author Konrad Ktoso Malawski
 */
public abstract class CommonViewActivity extends Activity {
    static final String TAG = "CommonViewActivity";

    public static final String SETTINGS_NAME = "settings";

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

    protected Map<String, Object> settings = new HashMap<String, Object>();

    /**
     * This will restore all settings needed for the app to run properly
     * and also start some of our services
     *
     * @param state the state to be restored from
     */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

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

        restoreSettings();
    }

    /**
     * Restore preferences and load them into our variables
     */
    private void restoreSettings() {
        SharedPreferences preferences = getSharedPreferences(SETTINGS_NAME, 0);
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
    private void persistSettings() {
        if (!getClass().equals(SettingsActivity.class)) {
            return;
        }

        Log.d(TAG, "Persisting settings: " + settings);

        // We need an Editor object to make preference changes.
        SharedPreferences preferences = getSharedPreferences(SETTINGS_NAME, 0);
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

    @Override
    protected void onStop() {
        super.onStop();
        persistSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    /**
     * React on menu item changes etc.
     *
     * @param item the currently selected item
     * @return true if the switch was an success, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Class clazz;

        switch (item.getItemId()) {
            case R.id.main_menu_item:
                clazz = VerfluchterActivity.class;
                break;
            case R.id.settings_menu_item:
                clazz = SettingsActivity.class;
                break;
            case R.id.about_menu_item:
                clazz = AboutViewActivity.class;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        //do the switch only if it's needed
        if (!clazz.equals(this.getClass())) {
            startActivityForResult(new Intent(this, clazz), 0);
            return true;
        }
        return false;
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

    protected <T> void setSetting(String key, T value) {
        settings.put(key, value);
    }

    protected void showToast(String message, int duration) {
        Context context = getApplicationContext();

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    protected void showToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }
}
