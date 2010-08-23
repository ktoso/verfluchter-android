package pl.xsolve.verfluchter.activities;

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
import pl.xsolve.verfluchter.rest.RestClient;
import pl.xsolve.verfluchter.rest.RestResponse;
import pl.xsolve.verfluchter.services.RefreshService;
import pl.xsolve.verfluchter.services.WorkTimeNotifierService;
import pl.xsolve.verfluchter.tools.SoulTools;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
            } else if (RefreshService.INTENT_FRESH_DATA.equals(action)) {
                Log.d(TAG, "Received " + RefreshService.INTENT_FRESH_DATA);
                updateWorkedHoursStats(intent.getStringExtra("plainTextedResponse"));
            }
        }
    };

    // Login cookie ;-)
    Cookie verfluchtesCookie = null;

    //this pair represents the hours and mins worked today
    Pair<Integer, Integer> todayTime = new Pair<Integer, Integer>(0, 0);

    // am I currently working?
    boolean amICurrentlyWorking = false;

    // Loaded work hours data
    Date updatedAt = null;
    List<String> dziennie = new LinkedList<String>();
    List<String> tygodniowo = new LinkedList<String>();
    List<String> miesiecznie = new LinkedList<String>();

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
        Log.v(TAG, settings.toString());

        if (getSettingBoolean(USE_REMINDER_SERVICE_B)) {
            stopService(new Intent(this, WorkTimeNotifierService.class));
            startService(new Intent(this, WorkTimeNotifierService.class));
            showToast("Uaktywniono serwis powiadamiania o przekroczonym czasie pracy.");

            stopService(new Intent(this, RefreshService.class));
            startService(new Intent(this, RefreshService.class));
        }

        onRefreshButtonClicked();
    }

    private void sentUpdateWorkedHoursStatsIntent(String plainTextedResponse) {
        Intent intent = new Intent(this, VerfluchterActivity.class);
        intent.putExtra("plainTextedResponse", plainTextedResponse);
        sendBroadcast(intent);
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
                onRefreshButtonClicked();
            }
        });

        startWorkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onChangeWorkingStatusClicked(true);
            }
        });

        stopWorkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onChangeWorkingStatusClicked(false);
            }
        });
    }

    private void onChangeWorkingStatusClicked(boolean changeWorkingStatusTo) {
        if (verfluchtesCookie == null) {
            acquireLoginCookie();
        }

        String plainTextedResponse = callChangeWorkingStatus(changeWorkingStatusTo);

        if (plainTextedResponse != null) {
            updateWorkedHoursStats(plainTextedResponse);
        }
    }

    private void onRefreshButtonClicked() {
        if (verfluchtesCookie == null) {
            acquireLoginCookie();
        }

        String plainTextedResponse = callRefreshData();

        if (plainTextedResponse != null) {
            updateWorkedHoursStats(plainTextedResponse);
        }
    }

    private void setAmICurrentlyWorking(String plainTextedResponse) {
        if (plainTextedResponse.contains("Rozpocznij")) {
            setAmICurrentlyWorking(false);
        } else if (plainTextedResponse.contains("Zakończ")) {
            setAmICurrentlyWorking(true);
        }
    }

    private void setAmICurrentlyWorking(boolean amIWorking) {
        amICurrentlyWorking = amIWorking;

        String text = amIWorking ? getString(R.string.am_i_working_yes) : getString(R.string.am_i_working_no);
        currentlyWorkingText.setText(text);
    }

    private void acquireLoginCookie() {
        RestResponse response = null;
        try {
            response = callLogin();

            verfluchtesCookie = response.getCookie("verfluchter");
            Log.d(TAG, "Got login cookie: verfluchter=" + verfluchtesCookie.getValue());
//            Log.v(TAG, "Got response: " + responseMessage);
        } catch (IOException e) {
            e.printStackTrace();
            if (response != null) {
                Log.e(TAG, "Failed while getting response, error code: " + response.getResponseCode() + ", message: " + response.getErrorMessage());
            }
        } catch (NullPointerException e) {
            String message = "Failed while getting the servers response, response is null.";
            Log.e(TAG, message);
            showToast(message);
        }
    }

    public String callRefreshData() {
        String responseMessage = null;
        RestResponse response = null;
        try {
            response = callWebService("", RequestMethod.GET);

            responseMessage = response.getResponse();
            Log.d(TAG, "Got response");//: " + responseMessage);

            failIfResponseNotOk(response);

            setAmICurrentlyWorking(responseMessage);

            return Html.fromHtml(responseMessage).toString();
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
            showToast(message);
        } catch (Exception e) {
            String message = "Response error code was: " + response.getResponseCode();
            Log.e(TAG, message);
            showToast(message);
            e.printStackTrace();

        }
        return null;
    }

    private String callChangeWorkingStatus(boolean amIWorking) {
        String responseMessage = null;
        RestResponse response = null;
        try {
            String action = amIWorking ? "begin" : "end";
            String callPath = "/timetable/" + action;
            response = callWebService(callPath, RequestMethod.POST);

            responseMessage = response.getResponse();
            Log.d(TAG, "Got response");//: " + responseMessage);

            failIfResponseNotOk(response);

            setAmICurrentlyWorking(responseMessage);
            return Html.fromHtml(responseMessage).toString();
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
            showToast(message);
        } catch (Exception e) {
            String message = "Response error code was: " + response.getResponseCode();
            Log.e(TAG, message);
            showToast(message);
            e.printStackTrace();

        }
        return null;
    }

    private void failIfResponseNotOk(RestResponse response) throws Exception {
        if (!SoulTools.isResponseOK(response)) {
            Log.d(TAG, "But the response code is not 200...");
            Log.v(TAG, response == null ? "Response was null." : response.getResponse());
            throw new Exception();
        }
    }

    private void updateWorkedHoursStats(String plainTextedResponse) {
        boolean byloDziennie = false, byloTygodniowo = false, byloMiesiecznie = false;

        dziennie.clear();
        tygodniowo.clear();
        miesiecznie.clear();

        updatedAt = new Date();
        String todayDateString = SoulTools.getTodayDateString();

        for (String line : plainTextedResponse.split("\n")) {
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

    private RestResponse callWebService(String path, RequestMethod method) throws IOException {
        return callWebService(path, method, Collections.<Pair<String, String>>emptyList());
    }

    private RestResponse callWebService(String path, RequestMethod method, List<Pair<String, String>> params) throws IOException {
        RestClient restClient = new RestClient();
        setupBasicAuth(restClient);
//        setupLoginAuth(restClient);
        setupCookieAuth(restClient);

        path = SoulTools.unNullify(path);
        String domain = getDomain() + path;

        // add request parameters
        for (Pair<String, String> param : params) {
            restClient.addParam(param.first, param.second);
        }

        return restClient.execute(domain, method);
    }

    private void setupBasicAuth(RestClient restClient) {
        Log.v(TAG, "Setting up basic auth: " + getSettingString(BASIC_AUTH_USER_S) + ":" + getSettingString(BASIC_AUTH_PASS_S));
        restClient.setupBasicAuth(getSettingString(BASIC_AUTH_USER_S), getSettingString(BASIC_AUTH_PASS_S));
    }

    private void setupLoginAuth(RestClient restClient) {
        restClient.addParam("username", getSettingString(MY_AUTH_USER_S));
        restClient.addParam("password", getSettingString(MY_AUTH_PASS_S));
    }

    private void setupCookieAuth(RestClient restClient) {
        if (verfluchtesCookie != null) {
            restClient.addCookie(verfluchtesCookie);
        }
    }

    private RestResponse callLogin() throws IOException {
        String domain = getDomain();

        RestClient restClient = new RestClient();
        setupLoginAuth(restClient);
        setupBasicAuth(restClient);

        return restClient.execute(domain + "/login", RequestMethod.POST);
    }

    private String getDomain() {
        String domain = getSettingString(SERVER_DOMAIN_S);
        if (SoulTools.isEmpty(domain)) {
            showToast("Server domain is not set. Please fix this in the settings screen.");
            return null;
        }
        return SoulTools.removeHttpsPrefix(domain);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}