package pl.xsolve.verfluchter.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Konrad Ktoso Malawski
 */
public class HeyStopWorkingReceiver extends BroadcastReceiver {
    private final static String TAG = "HeyStopWorkingReceiver";

    private final static String MESSAGE = "Are you sure you want to exit?";
    private final static String CONTINUE = "Pracuj dalej";
    private final static String FINISH = "Zakończ pracę";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Revieved an intent!");

//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setMessage(MESSAGE)
//                .setCancelable(false)
//                .setPositiveButton(CONTINUE, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                    }
//                })
//                .setNegativeButton(FINISH, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//
//        AlertDialog alert = builder.create();
//        alert.show();
    }
}
