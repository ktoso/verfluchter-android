package pl.xsolve.verfluchter.rest;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import org.apache.http.cookie.Cookie;
import pl.xsolve.verfluchter.activities.CommonViewActivity;
import pl.xsolve.verfluchter.tools.AutoSettings;
import pl.xsolve.verfluchter.tools.SoulTools;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static pl.xsolve.verfluchter.activities.CommonViewActivity.*;
import static pl.xsolve.verfluchter.tools.AutoSettings.*;

/**
 * An common class for all AsyncTasks that need to use our rest client
 *
 * @author Konrad Ktoso Malawski
 */
public abstract class RestAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected String TAG = "RestAsyncTask";

    protected AutoSettings settings = AutoSettings.getInstance();

    private Cookie verfluchtesCookie = null;

    private RestResponse callLogin() throws IOException {
        String domain = settings.getSettingString(SERVER_DOMAIN_S);

        RestClient restClient = new RestClient();
        setupLoginAuth(restClient);
        setupBasicAuth(restClient);

        return restClient.execute(domain + "/login", RequestMethod.POST);
    }

    protected RestResponse callWebService(String path, RequestMethod method) throws IOException {
        return callWebService(path, method, Collections.<Pair<String, String>>emptyList());
    }

    protected RestResponse callWebService(String path, RequestMethod method, List<Pair<String, String>> params) throws IOException {
        RestClient restClient = new RestClient();
        setupBasicAuth(restClient);
//        setupLoginAuth(restClient);
        setupCookieAuth(restClient);

        path = SoulTools.unNullify(path);
        String domain = settings.getSettingString(SERVER_DOMAIN_S) + path;

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
//            showToast(message); //todo inform the user somehow
        }
    }

    private void setupBasicAuth(RestClient restClient) {
        Log.v(TAG, "Setting up basic auth: " + settings.getSettingString(BASIC_AUTH_USER_S) + ":" + settings.getSettingString(BASIC_AUTH_PASS_S));
        restClient.setupBasicAuth(settings.getSettingString(BASIC_AUTH_USER_S), settings.getSettingString(BASIC_AUTH_PASS_S));
    }

    private void setupLoginAuth(RestClient restClient) {
        restClient.addParam("username", settings.getSettingString(MY_AUTH_USER_S));
        restClient.addParam("password", settings.getSettingString(MY_AUTH_PASS_S));
    }

    private void setupCookieAuth(RestClient restClient) {
        if (verfluchtesCookie != null) {
            restClient.addCookie(verfluchtesCookie);
        }
    }
}
