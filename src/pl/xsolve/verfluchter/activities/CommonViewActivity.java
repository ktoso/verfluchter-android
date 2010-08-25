package pl.xsolve.verfluchter.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import pl.xsolve.verfluchter.AutoSettings;
import pl.xsolve.verfluchter.R;

/**
 * Common class for all "View" Activities, is supports menu handling etc
 *
 * @author Konrad Ktoso Malawski
 */
public abstract class CommonViewActivity extends Activity {
    static final String TAG = "CommonViewActivity";

    protected final AutoSettings autoSettings = AutoSettings.getInstance();

    /**
     * This will restore all autoSettings needed for the app to run properly
     * and also start some of our services
     *
     * @param state the state to be restored from
     */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    protected void showToast(String message, int duration) {
        Context context = getApplicationContext();

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    protected void showToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    //------------delegates------------------------------------------------------------------------
    public String getSettingString(String key) {
        return autoSettings.getSettingString(key);
    }

    public Boolean getSettingBoolean(String key) {
        return autoSettings.getSettingBoolean(key);
    }

    public <T> void setSetting(String key, T value) {
        autoSettings.setSetting(key, value);
    }
}
