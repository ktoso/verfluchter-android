package pl.xsolve.verfluchter.tasks.general;

import android.os.AsyncTask;
import pl.xsolve.verfluchter.rest.RestResponse;

public class SampleAsyncTask extends AsyncTask<Void, Integer, RestResponse>{
                                          // params, progress, result
    @Override
    protected void onPreExecute() {
        //in UI thread
        super.onPreExecute();
    }

    @Override
    protected RestResponse doInBackground(Void... voids) {
        //NOT in UI thread
        for (int i = 0; i<100; i++){
            publishProgress(i);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        //in UI thread
        System.out.println("progress = " + values[0]);
        super.onProgressUpdate(values);
    }

    @Override                    // result
    protected void onPostExecute(RestResponse restResponse) {
        //in UI thread
        super.onPostExecute(restResponse);
    }

    @Override
    protected void onCancelled() {
        //in UI thread
        super.onCancelled();
    }
}
