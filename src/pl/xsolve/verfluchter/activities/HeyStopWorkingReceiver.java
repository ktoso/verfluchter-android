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
