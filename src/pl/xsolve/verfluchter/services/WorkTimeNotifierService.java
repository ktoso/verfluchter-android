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

package pl.xsolve.verfluchter.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import pl.xsolve.verfluchter.tools.SoulTools;

import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Konrad Ktoso Malawski
 */
public class WorkTimeNotifierService extends Service {

    private static final String TAG = "WorkTimeNotifierService";
    private static final long INTERVAL = 60000;

    private Timer timer = new Timer();
    public static final String INTENT_HEY_STOP_WORKING = "HEY_STOP_WORKING";

    /**
     * Called on service creation, will start the timer
     */
    @Override
    public void onCreate() {
        super.onCreate();
        start();

        Log.v(TAG, "Service started");
    }

    /**
     * Called on service destruction, will stop the timer
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();

        Log.v(TAG, "Service stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                process();
            }
        }, 0, INTERVAL);
    }

    private void process() {
        Log.v(TAG, "WorkTimeNotifierService is processing.");

        GregorianCalendar now = new GregorianCalendar ();
        if(SoulTools.itsWeekend(now)){
            return;
        }

        if (SoulTools.workTimeIsOver(now)) {
            Intent intent = new Intent();
            intent.setAction(INTENT_HEY_STOP_WORKING);
            sendBroadcast(intent);
        }
    }

    private void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }
}