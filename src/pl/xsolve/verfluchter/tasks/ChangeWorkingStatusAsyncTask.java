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

package pl.xsolve.verfluchter.tasks;

import android.util.Log;
import pl.xsolve.verfluchter.activities.VerfluchterActivity;
import pl.xsolve.verfluchter.rest.RequestMethod;
import pl.xsolve.verfluchter.rest.RestResponse;
import pl.xsolve.verfluchter.tasks.general.RestAsyncTask;
import pl.xsolve.verfluchter.tools.SoulTools;

import java.io.IOException;

/**
 * This is an AsyncTask to change the timer's status from working to not working and vice versa.
 * It will display an ProgressDialog and fetch the external resources.
 *
 * @author Konrad Ktoso Malawski
 * @see <a href="http://developer.android.com/reference/android/os/AsyncTask.html">AsyncTask JavaDoc</a>
 */
public class ChangeWorkingStatusAsyncTask extends RestAsyncTask<Void, Integer, RestResponse> {

    // logger tag
    private static final String TAG = ChangeWorkingStatusAsyncTask.class.getSimpleName();

    private ChangeWorkingStatusListener listener;
    private boolean changeWorkingStatusTo;

    public ChangeWorkingStatusAsyncTask(ChangeWorkingStatusListener changeWorkingStatusListener, boolean changeWorkingStatusTo) {
        super(VerfluchterActivity.getAutoSettings());

        this.listener = changeWorkingStatusListener;
        this.changeWorkingStatusTo = changeWorkingStatusTo;
    }

    @Override
    protected void onPreExecute() {
        String doingWhat = changeWorkingStatusTo ? "Trwa włączanie" : "Trwa zatrzymywanie";
        listener.showProgressDialog("", doingWhat + " licznika czasu. Zaczekaj proszę chwilkę..", true);
    }

    @Override
    protected RestResponse doInBackground(Void... nothing) {
        acquireLoginCookie();

        return callChangeWorkingStatus(changeWorkingStatusTo);
    }

    private RestResponse callChangeWorkingStatus(boolean changeWorkingStatusTo) {
        RestResponse response = null;

        // call url setup
        String action = changeWorkingStatusTo ? "begin" : "end";
        String callPath = "/timetable/" + action;

        try {
            response = callWebService(callPath, RequestMethod.POST);

            SoulTools.throwExceptionIfResponseNotOk(response);
            Log.d(TAG, "Got response");
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

        return response;
    }

    /**
     * 
     * @param restResponse the fetched response, or null if some error was encountered
     */
    @Override
    protected void onPostExecute(RestResponse restResponse) {
        listener.hideProgressDialog();

        if (hadErrors()) {
            for (String error : errors) {
                listener.showErrorMessage(error);
            }
            return;
        }

        listener.afterChangeWorkingStatus(restResponse);
    }
}
