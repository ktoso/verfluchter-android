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

package pl.xsolve.verfluchter.rest;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import org.apache.http.cookie.Cookie;
import pl.xsolve.verfluchter.tools.AutoSettings;
import pl.xsolve.verfluchter.tools.SoulTools;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static pl.xsolve.verfluchter.tools.AutoSettings.*;

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
        String domain = autoSettings.getSetting(SERVER_DOMAIN_S, String.class);

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
        String domain = autoSettings.getSetting(SERVER_DOMAIN_S, String.class) + path;

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
        Log.v(TAG, "Setting up basic auth: " + autoSettings.getSetting(BASIC_AUTH_USER_S, String.class) + ":" + autoSettings.getSetting(BASIC_AUTH_PASS_S, String.class));
        restClient.setupBasicAuth(autoSettings.getSetting(BASIC_AUTH_USER_S, String.class), autoSettings.getSetting(BASIC_AUTH_PASS_S, String.class));
    }

    private void setupLoginAuth(RestClient restClient) {
        restClient.addParam("username", autoSettings.getSetting(MY_AUTH_USER_S, String.class));
        restClient.addParam("password", autoSettings.getSetting(MY_AUTH_PASS_S, String.class));
        restClient.addParam("remember", String.valueOf(1));
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
