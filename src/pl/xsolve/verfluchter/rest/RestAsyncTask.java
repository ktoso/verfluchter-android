package pl.xsolve.verfluchter.rest;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import org.apache.http.cookie.Cookie;
import pl.xsolve.verfluchter.AutoSettings;
import pl.xsolve.verfluchter.tools.SoulTools;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static pl.xsolve.verfluchter.AutoSettings.*;

/**
 * An common class for all AsyncTasks that need to use our rest client
 *
 * @author Konrad Ktoso Malawski
 */
public abstract class RestAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected RestClient restClient = new RestClient();

    protected String TAG = "RestAsyncTask";

    protected AutoSettings autoSettings;

    protected List<String> errors = new LinkedList<String>();

    private Cookie verfluchtesCookie = null;

    protected RestAsyncTask(AutoSettings autoSettings) {
        this.autoSettings = autoSettings;
    }

    private RestResponse callLogin() throws IOException {
        String domain = autoSettings.getSettingString(SERVER_DOMAIN_S);

        setupLoginAuth(restClient);
        setupBasicAuth(restClient);

        return restClient.execute(domain + "/login", RequestMethod.POST);
    }

    protected RestResponse callWebService(String path, RequestMethod method) throws IOException {
        return callWebService(path, method, Collections.<Pair<String, String>>emptyList());
    }

    protected RestResponse callWebService(String path, RequestMethod method, List<Pair<String, String>> params) throws IOException {
        setupBasicAuth(restClient);
//        setupLoginAuth(restClient);
        setupCookieAuth(restClient);

        path = SoulTools.unNullify(path);
        String domain = autoSettings.getSettingString(SERVER_DOMAIN_S) + path;

        // add request parameters
        for (Pair<String, String> param : params) {
            restClient.addParam(param.first, param.second);
        }

        return restClient.execute(domain, method);
    }

    protected void acquireLoginCookie() {
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
            enqueueErrorMessage(message);
        }
    }

    private void setupBasicAuth(RestClient restClient) {
        Log.v(TAG, "Setting up basic auth: " + autoSettings.getSettingString(BASIC_AUTH_USER_S) + ":" + autoSettings.getSettingString(BASIC_AUTH_PASS_S));
        restClient.setupBasicAuth(autoSettings.getSettingString(BASIC_AUTH_USER_S), autoSettings.getSettingString(BASIC_AUTH_PASS_S));
    }

    private void setupLoginAuth(RestClient restClient) {
        restClient.addParam("username", autoSettings.getSettingString(MY_AUTH_USER_S));
        restClient.addParam("password", autoSettings.getSettingString(MY_AUTH_PASS_S));
    }

    private void setupCookieAuth(RestClient restClient) {
        if (verfluchtesCookie != null) {
            restClient.addCookies(verfluchtesCookie);
        }
    }

    /**
     * Enque an error message to be displayed when the Task gets hold of the UI thread
     *
     * @param errorMessage the error message to be displayed in the Toast
     * @return the number of total errors to be displayed
     */
    protected int enqueueErrorMessage(String errorMessage) {
        errors.add(errorMessage);
        return errors.size();
    }

    protected boolean hadErrors() {
        return errors.size() > 0;
    }


}
