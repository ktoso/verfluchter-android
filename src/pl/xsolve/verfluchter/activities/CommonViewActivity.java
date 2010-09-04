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

package pl.xsolve.verfluchter.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.xsolve.verfluchter.R;
import pl.xsolve.verfluchter.tasks.general.CanDisplayErrorMessages;
import pl.xsolve.verfluchter.tools.AutoSettings;
import roboguice.activity.GuiceActivity;

/**
 * Common class for all "View" Activities, is supports menu handling etc
 *
 * @author Konrad Ktoso Malawski
 */
public abstract class CommonViewActivity extends GuiceActivity
        implements CanDisplayErrorMessages {

    // logger tag
    private final static String TAG = CommonViewActivity.class.getSimpleName();

    @Inject
    protected AutoSettings autoSettings;

    /**
     * This will restore all autoSettings needed for the app to run properly
     * and also start some of our services
     *
     * @param state the state to be restored from
     */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        autoSettings.restoreSettings();
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
        Log.v(TAG, "Switching view...");

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

    @Override
    public void showErrorMessage(String error) {
        showToast(error, Toast.LENGTH_LONG);
    }

    //------------delegates------------------------------------------------------------------------

    public <T> T getSetting(String key, Class<T> clazz) {
        return autoSettings.getSetting(key, clazz);
    }

    public <T> void setSetting(String key, T value) {
        autoSettings.setSetting(key, value);
    }
}
