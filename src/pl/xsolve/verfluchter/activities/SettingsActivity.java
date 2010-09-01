package pl.xsolve.verfluchter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import pl.xsolve.verfluchter.R;
import pl.xsolve.verfluchter.services.RefreshService;
import pl.xsolve.verfluchter.services.WorkTimeNotifierService;
import pl.xsolve.verfluchter.tools.SoulTools;

import java.util.ArrayList;
import java.util.List;

import static pl.xsolve.verfluchter.tools.AutoSettings.*;
import static pl.xsolve.verfluchter.tools.SoulTools.isTrue;

/**
 * @author Konrad Ktoso Malawski
 */
public class SettingsActivity extends CommonViewActivity {

    private final static String TAG = SettingsActivity.class.getSimpleName();

    Button saveButton;
    EditText domainEditText;
    EditText basicLoginEditText;
    EditText basicPasswordEditText;
    EditText loginEditText;
    EditText passwordEditText;
    CheckBox useWorkTimeNotifierCheckBox;
    CheckBox useRefresherCheckBox;
    CheckBox useSoundCheckBox;
    TimePicker workingHourStartPicker;
    TimePicker workingHourEndPicker;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.settings);

        saveButton = (Button) findViewById(R.id.save_settings_button);

        domainEditText = (EditText) findViewById(R.id.domain_edittext);
        domainEditText.setText(autoSettings.getSetting(SERVER_DOMAIN_S, String.class));

        basicLoginEditText = (EditText) findViewById(R.id.basic_login_edittext);
        basicLoginEditText.setText(autoSettings.getSetting(BASIC_AUTH_USER_S, String.class));

        basicPasswordEditText = (EditText) findViewById(R.id.basic_password_edittext);

        loginEditText = (EditText) findViewById(R.id.login_edittext);
        loginEditText.setText(autoSettings.getSetting(MY_AUTH_USER_S, String.class));

        passwordEditText = (EditText) findViewById(R.id.password_edittext);

        useWorkTimeNotifierCheckBox = (CheckBox) findViewById(R.id.timer_service_check_checkbox);
        useWorkTimeNotifierCheckBox.setChecked(getSetting(USE_REMINDER_SERVICE_B, Boolean.class));

        useRefresherCheckBox = (CheckBox) findViewById(R.id.refresher_service_check_checkbox);
        useRefresherCheckBox.setChecked(getSetting(USE_REFRESHER_SERVICE_B, Boolean.class));

        useSoundCheckBox = (CheckBox) findViewById(R.id.use_sound_checkbox);
        useSoundCheckBox.setChecked(isTrue(getSetting(USE_SOUND_B, Boolean.class)));

        workingHourStartPicker = (TimePicker) findViewById(R.id.working_hour_start_picker);
        workingHourStartPicker.setIs24HourView(true);
        workingHourStartPicker.setCurrentHour(getSetting(WORKING_HOURS_START_HOUR_I, Integer.class));
        workingHourStartPicker.setCurrentMinute(getSetting(WORKING_HOURS_START_MIN_I, Integer.class));

        workingHourEndPicker = (TimePicker) findViewById(R.id.working_hour_end_picker);
        workingHourEndPicker.setIs24HourView(true);
        workingHourEndPicker.setCurrentHour(getSetting(WORKING_HOURS_END_HOUR_I, Integer.class));
        workingHourEndPicker.setCurrentMinute(getSetting(WORKING_HOURS_END_MIN_I, Integer.class));

        initListeners();
    }

    private void initListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onSaveClick();
            }
        });
    }

    private void onSaveClick() {
        Log.v(TAG, autoSettings.print());
        List<String> errors = new ArrayList<String>();

        String domain = domainEditText.getText().toString();
        domain = domain.replaceAll("http://", "").replaceAll("https://", "");
        autoSettings.setSetting(SERVER_DOMAIN_S, domain);

        autoSettings.setSetting(BASIC_AUTH_USER_S, basicLoginEditText.getText().toString());

        String basicPass = basicPasswordEditText.getText().toString();
        if (SoulTools.hasText(basicPass)) {
            autoSettings.setSetting(BASIC_AUTH_PASS_S, basicPass);
        }

        autoSettings.setSetting(MY_AUTH_USER_S, loginEditText.getText().toString());

        String pass = passwordEditText.getText().toString();
        if (SoulTools.hasText(pass)) {
            autoSettings.setSetting(MY_AUTH_PASS_S, pass);
        }

        Integer startHour = workingHourStartPicker.getCurrentHour();
        Integer startMin = workingHourStartPicker.getCurrentMinute();
        Integer endHour = workingHourEndPicker.getCurrentHour();
        Integer endMin = workingHourEndPicker.getCurrentMinute();

        // simple validation
        if (SoulTools.validateTimeRange(startHour, startMin, endHour, endMin)) {
            autoSettings.setSetting(WORKING_HOURS_START_HOUR_I, startHour);
            autoSettings.setSetting(WORKING_HOURS_START_MIN_I, startMin);
            autoSettings.setSetting(WORKING_HOURS_END_HOUR_I, endHour);
            autoSettings.setSetting(WORKING_HOURS_END_MIN_I, endMin);
        } else {
            errors.add(getString(R.string.invalidWorkTimeRange));
        }


        boolean useWorkTimeNotifier = useWorkTimeNotifierCheckBox.isChecked();
        autoSettings.setSetting(USE_REMINDER_SERVICE_B, useWorkTimeNotifier);
        if (useWorkTimeNotifier) {
            stopService(new Intent(this, WorkTimeNotifierService.class));
            startService(new Intent(this, WorkTimeNotifierService.class));
            showToast(getResources().getString(R.string.activated_work_time_check_service));
        } else {
            stopService(new Intent(this, WorkTimeNotifierService.class));
            showToast(getResources().getString(R.string.deactivated_work_time_check_service));
        }

        boolean useRefresher = useRefresherCheckBox.isChecked();
        autoSettings.setSetting(USE_REFRESHER_SERVICE_B, useRefresher);
        if (useRefresher) {
            stopService(new Intent(this, RefreshService.class));
            startService(new Intent(this, RefreshService.class));
            showToast("Uaktywniono serwis automatycznego odświeżania danych.");
        } else {
            stopService(new Intent(this, RefreshService.class));
            showToast("Wyłączono serwis automatycznego odświeżania danych.");
        }

        boolean useSound = useSoundCheckBox.isChecked();
        autoSettings.setSetting(USE_SOUND_B, useSound);

        if (errors.size() == 0) {
            //no errors, save and go back to main screen
            autoSettings.persistSettings();
            autoSettings.print();

            startActivityForResult(new Intent(this, VerfluchterActivity.class), 0);
        } else {
            Log.v(TAG, "Some errors in the form...");
            for (String error : errors) {
                Log.v(TAG, "--- " + error);
                showErrorMessage(error);
            }
        }
    }
}
