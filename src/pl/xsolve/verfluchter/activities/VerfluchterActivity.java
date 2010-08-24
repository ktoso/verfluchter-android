package pl.xsolve.verfluchter.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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
import pl.xsolve.verfluchter.rest.RestClient;
import pl.xsolve.verfluchter.rest.RestResponse;
import pl.xsolve.verfluchter.services.RefreshService;
import pl.xsolve.verfluchter.services.WorkTimeNotifierService;
import pl.xsolve.verfluchter.tools.SoulTools;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static pl.xsolve.verfluchter.tools.AutoSettings.*;

/**
 * @author Konrad Ktoso Malawski
 */
public class VerfluchterActivity extends CommonViewActivity {

    // UI components
    Button startWorkButton;
    Button stopWorkButton;
    ImageButton refreshButton;
    TextView hoursStatsTextView;
    TextView workTimeToday;
    TextView currentlyWorkingText;


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
    Pair<Integer, Integer> todayTime = new Pair<Integer, Integer>(0, 0);

    // am I currently working?
    boolean amICurrentlyWorking = false;

    // Loaded work hours data
    static Date updatedAt = null;
    List<String> dziennie = new LinkedList<String>();
    List<String> tygodniowo = new LinkedList<String>();
    List<String> miesiecznie = new LinkedList<String>();

    private static String plainTextResponse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        hoursStatsTextView = (TextView) findViewById(R.id.hours_stats_textview);
        workTimeToday = (TextView) findViewById(R.id.workTimeToday);
        stopWorkButton = (Button) findViewById(R.id.stopWork);
        startWorkButton = (Button) findViewById(R.id.startWork);
        refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        currentlyWorkingText = (TextView) findViewById(R.id.currently_working_text);

        initListeners();
        Log.v(TAG, autoSettings.toString());

        if (getSettingBoolean(USE_REMINDER_SERVICE_B)) {
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
        String todayDateString = SoulTools.getTodayDateString();

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

        hoursStatsTextView.setText(
                getString(R.string.dailyHoursLabel) + "\n" + Joiner.on("\n").join(dziennie) + "\n\n"
                        + getString(R.string.weeklyHoursLabel) + "\n" + Joiner.on("\n").join(tygodniowo) + "\n\n"
                        + getString(R.string.monthlyHoursLabel) + "\n" + Joiner.on("\n").join(miesiecznie));//todo make this better

        updateFooter();
    }

    private void updateWorkedToday(Pair<Integer, Integer> hourAndMin) {
        todayTime = hourAndMin;
        workTimeToday.setText(String.format("%dh %dm", hourAndMin.first, hourAndMin.second));
    }

    private void updateFooter() {
//        footer.setText("Last updated at: " + updatedAt);
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
            String responseMessage = null;
            RestResponse response = null;
            try {
                response = callWebService("", RequestMethod.GET);

//                responseMessage = response.getResponse();
                Log.d(TAG, "Got response");//: " + responseMessage);

                SoulTools.failIfResponseNotOk(response);
                return response;

//                setAmICurrentlyWorking(responseMessage);
//                return Html.fromHtml(responseMessage).toString();
            } catch (IOException e) {
                e.printStackTrace();
                if (response != null) {
                    Log.e(TAG, "Failed while getting response, error code: " + response.getResponseCode() + ", message: " + response.getErrorMessage());
                } else {
                    Log.e(TAG, "Failed while getting the servers response.");
                }
            } catch (NullPointerException e) {
                String message = "Failed while getting the servers response.";
                Log.e(TAG, message);
//                showToast(message); //todo inform user somehow
            } catch (Exception e) {
                String message = "Response error code was: " + response.getResponseCode();
                Log.e(TAG, message);
//                showToast(message); //todo inform user somehow
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
            if (restResponse != null) {
                dialog = ProgressDialog.show(VerfluchterActivity.this, "", "Parsing response. Please wait...", true);

                String responseMessage = restResponse.getResponse();

                setAmICurrentlyWorking(responseMessage);
                String plainTextResponse = Html.fromHtml(responseMessage).toString();

                if (plainTextResponse != null) {
                    updateWorkedHoursStats(plainTextResponse);
                }
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
            String responseMessage = null;
            RestResponse response = null;
            try {
                String action = changeWorkingStatusTo ? "begin" : "end";
                String callPath = "/timetable/" + action;
                response = callWebService(callPath, RequestMethod.POST);

                SoulTools.failIfResponseNotOk(response);
                Log.d(TAG, "Got response");

                return response;
            } catch (IOException e) {
                e.printStackTrace();
                if (response != null) {
                    Log.e(TAG, "Failed while getting response, error code: " + response.getResponseCode() + ", message: " + response.getErrorMessage());
                } else {
                    Log.e(TAG, "Failed while getting the servers response.");
                }
            } catch (NullPointerException e) {
                String message = "Failed while getting the servers response.";
                Log.e(TAG, message);
//                showToast(message); //todo inform user somehow
            } catch (Exception e) {
                String message = "Response error code was: " + response.getResponseCode();
                Log.e(TAG, message);
//                showToast(message); //todo inform user somehow
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(RestResponse restResponse) {
            dialog.dismiss();
            if (restResponse != null) {

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
}