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
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.base.Joiner;
import pl.xsolve.verfluchter.R;
import pl.xsolve.verfluchter.rest.RestResponse;
import pl.xsolve.verfluchter.services.WorkTimeNotifierService;
import pl.xsolve.verfluchter.tasks.ChangeWorkingStatusAsyncTask;
import pl.xsolve.verfluchter.tasks.ChangeWorkingStatusListener;
import pl.xsolve.verfluchter.tasks.RefreshDataAsyncTask;
import pl.xsolve.verfluchter.tasks.RefreshDataListener;
import pl.xsolve.verfluchter.tasks.general.CanDisplayErrorMessages;
import pl.xsolve.verfluchter.tasks.general.CanDisplayProgress;
import pl.xsolve.verfluchter.tools.*;
import roboguice.inject.InjectView;

import java.util.*;

import static pl.xsolve.verfluchter.tools.AutoSettings.USE_REMINDER_SERVICE_B;
import static pl.xsolve.verfluchter.tools.SoulTools.hasText;
import static pl.xsolve.verfluchter.tools.SoulTools.isTrue;

/**
 * @author Konrad Ktoso Malawski
 */
public class VerfluchterActivity extends CommonViewActivity
        implements RefreshDataListener, ChangeWorkingStatusListener,
        CanDisplayErrorMessages, CanDisplayProgress {

//---------------------------- Fields ----------------------------------------------------------

    // logger tag
    private final static String TAG = VerfluchterActivity.class.getSimpleName();

    // UI components
    @InjectView(R.id.start_work_button)
    Button startWorkButton;
    @InjectView(R.id.stop_work_button)
    Button stopWorkButton;
    @InjectView(R.id.refresh_button)
    ImageButton refreshButton;
    @InjectView(R.id.hours_stats_textview)
    TextView hoursStatsTextView;
    @InjectView(R.id.work_time_today)
    TextView workTimeToday;
    @InjectView(R.id.work_time_today_label)
    TextView workTimeTodayLabel;
    @InjectView(R.id.currently_working_textview)
    TextView currentlyWorking;

    // other UI components (dialogs etc)
    ProgressDialog progressDialog;

    private Timer workTimeUpdaterTimer = new Timer();

    // Global Broadcast Receiver
    BroadcastReceiver broadcastReceiver;

    // Need handler for callbacks to the UI thread
    // http://developer.android.com/guide/appendix/faq/commontasks.html#threading
    final Handler handler = new Handler();

    List<String> dziennie = new LinkedList<String>();
    List<String> tygodniowo = new LinkedList<String>();
    List<String> miesiecznie = new LinkedList<String>();

    static AutoSettings settingsForTasksReference;

    //------------------------ Basic cache ----------------------------
    // Loaded work hours data
    static Date updatedAt;
    // cache for our last fetched response
    static String cachedPlainTextResponse;
    //when did I click the start work button?
    static HourMin workStartedAt = null;
    //this pair represents the hours and mins worked today
    static HourMin workHourMin = new HourMin(0, 0);
    // am I currently working?
    static boolean amICurrentlyWorking = false;

//---------------------------- Methods ---------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsForTasksReference = autoSettings;

        setContentView(R.layout.main);

        // goto settings if it seems that the setup is incorrect etc...
        if (!hasText(getSetting(AutoSettings.BASIC_AUTH_PASS_S, String.class))) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 0);
            return;
        }

        initListeners();

        if (isTrue(getSetting(USE_REMINDER_SERVICE_B, Boolean.class))) {
            stopService(new Intent(this, WorkTimeNotifierService.class));
            startService(new Intent(this, WorkTimeNotifierService.class));
        }

        if (updatedAt == null) {
            new RefreshDataAsyncTask(this).execute();
        } else if (cachedPlainTextResponse != null) {
            updateWorkedToday(workHourMin);
            setAmICurrentlyWorking(amICurrentlyWorking);
            updateWorkedHoursStats(cachedPlainTextResponse);
        }

        restartWorkTimeUpdater();
    }

    /**
     * Initialize all button listeners
     */
    private void initListeners() {
        // our global intent listener
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (WorkTimeNotifierService.INTENT_HEY_STOP_WORKING.equals(action)) {
                    Log.d(TAG, "Received " + WorkTimeNotifierService.INTENT_HEY_STOP_WORKING);
                    showToast(getString(R.string.hey_stop_working_title));
                }
//            else if (RefreshService.INTENT_FRESH_DATA.equals(action)) {
//                Log.d(TAG, "Received " + RefreshService.INTENT_FRESH_DATA);
//                updateWorkedHoursStats(intent.getStringExtra("cachedPlainTextResponse"));
//            }
            }
        };

        // important global intent filter registration!
        IntentFilter filter = new IntentFilter(WorkTimeNotifierService.INTENT_HEY_STOP_WORKING);
        filter.addAction(WorkTimeNotifierService.INTENT_HEY_STOP_WORKING);
        registerReceiver(broadcastReceiver, filter);

        final VerfluchterActivity self = VerfluchterActivity.this;

        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new RefreshDataAsyncTask(self).execute();
            }
        });

        startWorkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new ChangeWorkingStatusAsyncTask(self, true).execute();
            }
        });

        stopWorkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new ChangeWorkingStatusAsyncTask(self, false).execute();
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
        currentlyWorking.setText(text);
    }

    private void updateWorkedHoursStats(String plainTextResponse) {
        boolean byloDziennie = false, byloTygodniowo = false, byloMiesiecznie = false;

        // cache the response
        updatedAt = new Date();
        cachedPlainTextResponse = plainTextResponse;

        // so we dont get into each others way
        workTimeUpdaterTimer.cancel();

        dziennie.clear();
        tygodniowo.clear();
        miesiecznie.clear();

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
                String[] strings = line.split(" ");
                updateWorkedToday(SoulTools.convertTimeStringToHourMin(strings[1]));


                strings[0] = strings[0].replaceAll(":", "");
                dziennie.add(new StringBuilder(strings[0]).append(" ")
                        .append(SoulTools.getDisplayDay(strings[0]))
                        .append(": ")
                        .append(new HourMin(strings[1]).pretty())
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
            restartWorkTimeUpdater();
        } else if (newestEntry.contains(SoulTools.getYesterdayString())) {
            workTimeTodayLabel.setText(R.string.workedYesterdayLabel);
            restartWorkTimeUpdater();
        } else {
            workTimeTodayLabel.setText(R.string.workedLastTimeLabel);
            restartWorkTimeUpdater();
        }

        // update the long stats view
        // todo make this better
        hoursStatsTextView.setText(
                getString(R.string.dailyHoursLabel) + "\n" + Joiner.on("\n").join(dziennie) + "\n\n"
                        + getString(R.string.weeklyHoursLabel) + "\n" + Joiner.on("\n").join(tygodniowo) + "\n\n"
                        + getString(R.string.monthlyHoursLabel) + "\n" + Joiner.on("\n").join(miesiecznie));
    }

    private void restartWorkTimeUpdater() {
        workTimeUpdaterTimer.cancel();

        workTimeUpdaterTimer = new Timer();
        workTimeUpdaterTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // stop adding time if you've stopped working
                if (!amICurrentlyWorking) {
                    this.cancel();
                    return;
                }

                handler.post(addOneMinuteWorkedTimeRunnable);
            }
        }, Constants.MINUTE, Constants.MINUTE);
    }

    private synchronized void updateWorkedToday(HourMin hourAndMin) {
        workHourMin = hourAndMin;
        workTimeToday.setText(hourAndMin.pretty());
    }

    public static AutoSettings getAutoSettings() {
        return settingsForTasksReference;
    }

    public static boolean isCurrentlyWorking() {
        return amICurrentlyWorking;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        workTimeUpdaterTimer.cancel();
    }

//--------------------------- Async Task handling ---------------------------------------------

    @Override
    public void afterRefreshData(final RestResponse restResponse) {
        handler.post(new Runnable() {
            public void run() {
                progressDialog = ProgressDialog.show(VerfluchterActivity.this,
                        "",
                        "Trwa przetważanie odpowiedzi serwera. Jeszcze tylko chwilkę.",
                        true);
                String responseMessage = restResponse.getResponse();

                setAmICurrentlyWorking(responseMessage);
                String plainTextResponse = Html.fromHtml(responseMessage).toString();

                if (plainTextResponse != null) {
                    updateWorkedHoursStats(plainTextResponse);
                }

                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void afterChangeWorkingStatus(final RestResponse restResponse) {
        handler.post(new Runnable() {
            public void run() {
                progressDialog = ProgressDialog.show(VerfluchterActivity.this,
                        "",
                        "Trwa przetwarzanie odpowiedi serwera. Jeszcze tylko momencik.",
                        true);
                String responseMessage = restResponse.getResponse();

                setAmICurrentlyWorking(responseMessage);
                String plainTextResponse = Html.fromHtml(responseMessage).toString();

                if (plainTextResponse != null) {
                    updateWorkedHoursStats(plainTextResponse);
                }

                progressDialog.dismiss();
            }
        });
    }

    /**
     * This Runnable will be launched using handler.post()
     * which returns us into the UI Thread and allows us to update the UI right away,
     * even thought the initial call came from inside of an Timer instance.
     */
    final Runnable addOneMinuteWorkedTimeRunnable = new Runnable() {
        @Override
        public void run() {
            // if called using an handler  => back in the UI Thread :-)
            updateWorkedToday(workHourMin.addMin(1));
        }
    };

//---------------------------- Interface implementations ---------------------------------------

    @Override
    public void showProgressDialog(final String title, final String message, final boolean indeterminate) {
        handler.post(new Runnable() {
            public void run() {
                progressDialog = ProgressDialog.show(VerfluchterActivity.this, title, message, indeterminate, true);
            }
        });
    }

    @Override
    public void showProgressDialog(final String title, final String message, final boolean indeterminate, final boolean cancelable, final DialogInterface.OnCancelListener cancelListener) {
        handler.post(new Runnable() {
            public void run() {
                progressDialog = ProgressDialog.show(VerfluchterActivity.this, title, message, indeterminate, cancelable, cancelListener);
            }
        });
    }

    @Override
    public void hideProgressDialog() {
        handler.post(new Runnable() {
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void showErrorMessage(final String error) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                showToast(error, Toast.LENGTH_LONG);
            }
        });
    }
}