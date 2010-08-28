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

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.common.base.Joiner;
import org.apache.http.cookie.Cookie;
import pl.xsolve.verfluchter.R;
import pl.xsolve.verfluchter.rest.RequestMethod;
import pl.xsolve.verfluchter.rest.RestAsyncTask;
import pl.xsolve.verfluchter.rest.RestResponse;
import pl.xsolve.verfluchter.services.RefreshService;
import pl.xsolve.verfluchter.services.WorkTimeNotifierService;
import pl.xsolve.verfluchter.tools.Constants;
import pl.xsolve.verfluchter.tools.SoulTools;

import java.io.IOException;
import java.util.*;

import static pl.xsolve.verfluchter.tools.AutoSettings.*;
import static pl.xsolve.verfluchter.tools.SoulTools.isTrue;

/**
 * @author Konrad Ktoso Malawski
 */
public class VerfluchterActivity extends CommonViewActivity {

    // UI components
    Button startWorkButton;
    Button stopWorkButton;
    ImageButton refreshButton;
    TextView hoursStatsTextView;
    TextView workTimeTodayLabel;
    TextView workTimeToday;
    TextView currentlyWorkingText;

    private Timer workTimeUpdaterTimer = new Timer();

    // Global Broadcast Receiver
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WorkTimeNotifierService.INTENT_HEY_STOP_WORKING.equals(action)) {
                Log.d(TAG, "Received " + WorkTimeNotifierService.INTENT_HEY_STOP_WORKING);
                showToast(getString(R.string.hey_stop_working_title));
            }
//            else if (RefreshService.INTENT_FRESH_DATA.equals(action)) {
//                Log.d(TAG, "Received " + RefreshService.INTENT_FRESH_DATA);
//                updateWorkedHoursStats(intent.getStringExtra("plainTextResponse"));
//            }
        }
    };

    // Login cookie ;-)
    Cookie verfluchtesCookie = null;

    //this pair represents the hours and mins worked today
    Pair<Integer, Integer> workedTime = new Pair<Integer, Integer>(0, 0);

    // am I currently working?
    boolean amICurrentlyWorking = false;

    // Loaded work hours data
    static Date updatedAt = null;
    List<String> dziennie = new LinkedList<String>();
    List<String> tygodniowo = new LinkedList<String>();
    List<String> miesiecznie = new LinkedList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(isTrue(getSetting(SETUP_DUE_B, Boolean.class))){
            startActivityForResult(new Intent(this, SettingsActivity.class), 0);
            return;
        }

        hoursStatsTextView = (TextView) findViewById(R.id.hours_stats_textview);
        workTimeTodayLabel = (TextView) findViewById(R.id.workTimeTodayLabel);
        workTimeToday = (TextView) findViewById(R.id.workTimeToday);
        stopWorkButton = (Button) findViewById(R.id.stopWork);
        startWorkButton = (Button) findViewById(R.id.startWork);
        refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        currentlyWorkingText = (TextView) findViewById(R.id.currently_working_text);

        initListeners();
        Log.v(TAG, autoSettings.print());

        if (isTrue(getSetting(USE_REMINDER_SERVICE_B, Boolean.class))) {
            stopService(new Intent(this, WorkTimeNotifierService.class));
            startService(new Intent(this, WorkTimeNotifierService.class));
            showToast("Uaktywniono serwis powiadamiania o przekroczonym czasie pracy.");

            stopService(new Intent(this, RefreshService.class));
            startService(new Intent(this, RefreshService.class));
        }

        new RefreshDataAsyncTask().execute();
    }

    /**
     * Initialize all button listeners
     */
    private void initListeners() {
        //super important global intent filter registration!
        IntentFilter filter = new IntentFilter(WorkTimeNotifierService.INTENT_HEY_STOP_WORKING);
        filter.addAction(WorkTimeNotifierService.INTENT_HEY_STOP_WORKING);
        registerReceiver(broadcastReceiver, filter);

        refreshButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                new RefreshDataAsyncTask().execute();
            }
        });

        startWorkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new ChangeWorkingStatusAsyncTask(true).execute();
            }
        });

        stopWorkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new ChangeWorkingStatusAsyncTask(false).execute();
            }
        });
    }

    private void setAmICurrentlyWorking(String plainTextResponse) {
        if (plainTextResponse.contains("Rozpocznij")) {
            setAmICurrentlyWorking(false);
        } else if (plainTextResponse.contains("Zakończ")) {
            setAmICurrentlyWorking(true);
        }
    }

    private void setAmICurrentlyWorking(boolean amIWorking) {
        amICurrentlyWorking = amIWorking;

        String text = amIWorking ? getString(R.string.am_i_working_yes) : getString(R.string.am_i_working_no);
        currentlyWorkingText.setText(text);
    }

    private void updateWorkedHoursStats(String plainTextResponse) {
        boolean byloDziennie = false, byloTygodniowo = false, byloMiesiecznie = false;

        dziennie.clear();
        tygodniowo.clear();
        miesiecznie.clear();

        updatedAt = new Date();

        for (String line : plainTextResponse.split("\n")) {
            if (line.trim().equals("")) {
                continue;
            }

            if (!byloDziennie) {
                if (line.contains("Dziennie")) {
                    byloDziennie = true;
                }
                continue;
            }
            if (line.contains("Tygodniowo")) {
                byloTygodniowo = true;
                continue;
            }
            if (line.contains("Miesiecznie")) {
                byloMiesiecznie = true;
                continue;
            }

            if (!byloTygodniowo && !byloMiesiecznie) {
                //todo add "TODAY" checking, not just first record, and <b/> it :-)
                String[] strings = line.split(" ");
                updateWorkedToday(SoulTools.convertTimeStringToIntPair(strings[1]));

                dziennie.add(new StringBuilder(strings[0].replaceAll(":", "")).append(" ")
                        .append(SoulTools.getDisplayDay(strings[0]))
                        .append(": ")
                        .append(strings[1].replaceAll(":", "h "))
                        .append("m")
                        .toString());
            } else if (!byloMiesiecznie) {
                tygodniowo.add(line);
            } else {
                if (!line.contains("Mantis")) {
                    miesiecznie.add(line);
                } else {
                    break;
                }
            }
        }

        Collections.reverse(dziennie);
        Collections.reverse(tygodniowo);
        Collections.reverse(miesiecznie);

        // update the "worked today|yesterday" stuff
        String newestEntry = dziennie.get(0);
        if (newestEntry.contains(SoulTools.getTodayDateString())) {
            workTimeTodayLabel.setText(R.string.workedTodayLabel);
        }else if(newestEntry.contains(SoulTools.getYesterdayString())){
            workTimeTodayLabel.setText(R.string.workedYesterdayLabel);
        }else{
            workTimeTodayLabel.setText(R.string.workedLastTimeLabel);
        }

        // setup and restart the timer to add minutes to the worked time without fetching any resources
        // yeah, if someone stops the timer on the website - we'll have wrong data. But thats why we have the refresh,
        // or better cal it "resync" button. And also... why would anyone use the website if you have verfluchter-android? :-)
        restartWorkTimeUpdater();

        // update the long stats view
        hoursStatsTextView.setText(
                getString(R.string.dailyHoursLabel) + "\n" + Joiner.on("\n").join(dziennie) + "\n\n"
                        + getString(R.string.weeklyHoursLabel) + "\n" + Joiner.on("\n").join(tygodniowo) + "\n\n"
                        + getString(R.string.monthlyHoursLabel) + "\n" + Joiner.on("\n").join(miesiecznie));//todo make this better


    }

    private void restartWorkTimeUpdater() {
        workTimeUpdaterTimer.cancel();

        workTimeUpdaterTimer = new Timer();
        workTimeUpdaterTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!amICurrentlyWorking) {
                    this.cancel();
                }

                updateWorkedTimeByOneMin();
            }
        }, Constants.MINUTE, Constants.MINUTE);
    }

    private void updateWorkedTimeByOneMin() {
        int m = workedTime.second + 1;
        int h;
        if (m == 60) {
            h = workedTime.first + 1;
            m = 0;
        } else {
            h = workedTime.first;
        }
        updateWorkedToday(new Pair<Integer, Integer>(h, m));
    }

    private void updateWorkedToday(Pair<Integer, Integer> hourAndMin) {
        workedTime = hourAndMin;
        workTimeToday.setText(String.format("%dh %dm", hourAndMin.first, hourAndMin.second));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //--------------------------- Async Tasks -------------------------------------------------------------------------

    /**
     * This is an AsyncTask to update our working hour data.
     * It will display an ProgressDialog and fetch the external resources.
     * <p/>
     * Info about the extends <_, _, _>:
     * The 3 Type Params mean as follows:
     * 1. Params, the type of the parameters sent to the task upon execution.
     * 2. Progress, the type of the progress units published during the background computation.
     * 3. Result, the type of the result of the background computation.
     *
     * @author Konrad Ktoso Malawski
     * @see <a href="http://developer.android.com/reference/android/os/AsyncTask.html">AsyncTask JavaDoc</a>
     */
    public class RefreshDataAsyncTask extends RestAsyncTask<Void, Integer, RestResponse> {

        public RefreshDataAsyncTask() {
            super(VerfluchterActivity.autoSettings);
        }

        ProgressDialog dialog;

        /**
         * Invoked on the UI thread immediately after the task is executed.
         * This step is normally used to setup the task, for instance by showing a progress bar in the user interface.
         */
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(VerfluchterActivity.this, "", "Fetching external resource. Please wait...", true);
        }

        @Override
        protected RestResponse doInBackground(Void... nothing) {
            if (verfluchtesCookie == null) {
                acquireLoginCookie();
            }

            return callRefreshData();
        }

        public RestResponse callRefreshData() {
            RestResponse response = null;
            try {
                response = callWebService("", RequestMethod.GET);

//                responseMessage = response.getResponse();
                Log.d(TAG, "Got response");//: " + responseMessage);

                SoulTools.failIfResponseNotOk(response);
                return response;
            } catch (IOException e) {
                String message = response == null ? "Failed while getting the servers response."
                        : "Failed while getting response, error code: " + response.getResponseCode() + ", message: " + response.getErrorMessage();
                Log.e(TAG, message);
                enqueueErrorMessage(message);
            } catch (NullPointerException e) {
                String message = "Failed while getting the servers response.";
                Log.e(TAG, message);
                enqueueErrorMessage(message);
            } catch (Exception e) {
                String message = "Other exception Response was: " + response;
                Log.e(TAG, message);
                enqueueErrorMessage(message);
                e.printStackTrace();
            }
            return null;
        }

        /**
         * This is called in the UI Thread
         *
         * @param restResponse the result of this task
         */
        @Override
        protected void onPostExecute(RestResponse restResponse) {
            dialog.dismiss();

            if (hadErrors()) {
                for (String error : errors) {
                    showToast(error);
                }
                return;
            }

            dialog = ProgressDialog.show(VerfluchterActivity.this, "", "Parsing response. Please wait...", true);
            String responseMessage = restResponse.getResponse();

            setAmICurrentlyWorking(responseMessage);
            String plainTextResponse = Html.fromHtml(responseMessage).toString();

            if (plainTextResponse != null) {
                updateWorkedHoursStats(plainTextResponse);
            }

            dialog.dismiss();
        }
    }

    /**
     * This is an AsyncTask to change the timer's status from working to not working and vice versa.
     * It will display an ProgressDialog and fetch the external resources.
     *
     * @author Konrad Ktoso Malawski
     * @see <a href="http://developer.android.com/reference/android/os/AsyncTask.html">AsyncTask JavaDoc</a>
     */
    private class ChangeWorkingStatusAsyncTask extends RestAsyncTask<Void, Integer, RestResponse> {

        ProgressDialog dialog;

        boolean changeWorkingStatusTo;

        public ChangeWorkingStatusAsyncTask(boolean changeWorkingStatusTo) {
            super(VerfluchterActivity.autoSettings);
            this.changeWorkingStatusTo = changeWorkingStatusTo;
        }

        @Override
        protected void onPreExecute() {
            String doingWhat = changeWorkingStatusTo ? "Starting" : "Stopping";
            dialog = ProgressDialog.show(VerfluchterActivity.this, "", doingWhat + " work timer. \nPlease wait...", true);
        }

        @Override
        protected RestResponse doInBackground(Void... nothing) {
            if (verfluchtesCookie == null) {
                acquireLoginCookie();
            }

            return callChangeWorkingStatus(changeWorkingStatusTo);
        }

        private RestResponse callChangeWorkingStatus(boolean changeWorkingStatusTo) {
            RestResponse response = null;
            try {
                String action = changeWorkingStatusTo ? "begin" : "end";
                String callPath = "/timetable/" + action;
                response = callWebService(callPath, RequestMethod.POST);

                SoulTools.failIfResponseNotOk(response);
                Log.d(TAG, "Got response");

                return response;
            } catch (IOException e) {
                String message = response == null ? "Failed while getting the servers response."
                        : "Failed while getting response, error code: " + response.getResponseCode() + ", message: " + response.getErrorMessage();
                Log.e(TAG, message);
                enqueueErrorMessage(message);
            } catch (NullPointerException e) {
                String message = "Failed while getting the servers response.";
                Log.e(TAG, message);
                enqueueErrorMessage(message);
            } catch (Exception e) {
                String message = "Other exception Response was: " + response;
                Log.e(TAG, message);
                enqueueErrorMessage(message);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(RestResponse restResponse) {
            dialog.dismiss();

            if (hadErrors()) {
                for (String error : errors) {
                    showToast(error);
                }
                return;
            }

            dialog = ProgressDialog.show(VerfluchterActivity.this, "", "Parsing response. \nPlease wait...", true);
            String responseMessage = restResponse.getResponse();

            setAmICurrentlyWorking(responseMessage);
            String plainTextResponse = Html.fromHtml(responseMessage).toString();

            if (plainTextResponse != null) {
                updateWorkedHoursStats(plainTextResponse);
            }

            dialog.dismiss();
        }
    }
}