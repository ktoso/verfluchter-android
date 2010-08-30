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
 * This is an AsyncTask to update our working hour data.
 * It will display an ProgressDialog and fetch the external resources.
 *
 * @author Konrad Ktoso Malawski
 * @see <a href="http://developer.android.com/reference/android/os/AsyncTask.html">AsyncTask JavaDoc</a>
 */
public class RefreshDataAsyncTask extends RestAsyncTask<Void, Integer, RestResponse> {

    private static final String TAG = RefreshDataAsyncTask.class.getSimpleName();

    private RefreshDataListener listener;

    public RefreshDataAsyncTask(RefreshDataListener refreshDataListener) {
        super(VerfluchterActivity.getAutoSettings());

        this.listener = refreshDataListener;
    }

    /**
     * Invoked on the UI thread immediately after the task is executed.
     * This step is normally used to setup the task, for instance by showing a progress bar in the user interface.
     */
    @Override
    protected void onPreExecute() {
        listener.showProgressDialog("", "Trwa aktualizowanie danych. Zaczekaj proszę momencik...", true);
    }

    @Override
    protected RestResponse doInBackground(Void... nothing) {
        acquireLoginCookie();

        return callRefreshData();
    }

    /**
     * We may call the listener (as it handles the updates by it's handler
     * in order to update our download progress etc!
     *
     * @return the RestResponse fetched from the web
     */
    private RestResponse callRefreshData() {
        RestResponse response = null;

        try {
            response = callWebService("", RequestMethod.GET);

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

        listener.afterRefreshData(restResponse);
    }
}
