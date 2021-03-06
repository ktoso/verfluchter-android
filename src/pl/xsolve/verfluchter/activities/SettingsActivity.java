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
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.List;

import static pl.xsolve.verfluchter.tools.AutoSettings.*;
import static pl.xsolve.verfluchter.tools.SoulTools.isTrue;

/**
 * @author Konrad Ktoso Malawski
 */
public class SettingsActivity extends CommonViewActivity {

    private final static String TAG = SettingsActivity.class.getSimpleName();

    @InjectView(R.id.save_settings_button)
    Button saveButton;
    @InjectView(R.id.domain_edittext)
    EditText domainEditText;
    @InjectView(R.id.basic_login_edittext)
    EditText basicLoginEditText;
    @InjectView(R.id.basic_password_edittext)
    EditText basicPasswordEditText;
    @InjectView(R.id.login_edittext)
    EditText loginEditText;
    @InjectView(R.id.password_edittext)
    EditText passwordEditText;
    @InjectView(R.id.use_timer_service_check_checkbox)
    CheckBox useWorkTimeNotifierCheckBox;
    @InjectView(R.id.use_refresher_service_check_checkbox)
    CheckBox useRefresherCheckBox;
    @InjectView(R.id.use_sound_checkbox)
    CheckBox useSoundCheckBox;
    @InjectView(R.id.working_hour_start_picker)
    TimePicker workingHourStartPicker;
    @InjectView(R.id.working_hour_end_picker)
    TimePicker workingHourEndPicker;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.settings);

        domainEditText.setText(autoSettings.getSetting(SERVER_DOMAIN_S, String.class));

        basicLoginEditText.setText(autoSettings.getSetting(BASIC_AUTH_USER_S, String.class));

        loginEditText.setText(autoSettings.getSetting(MY_AUTH_USER_S, String.class));

        useWorkTimeNotifierCheckBox.setChecked(getSetting(USE_REMINDER_SERVICE_B, Boolean.class));

        useRefresherCheckBox.setChecked(getSetting(USE_REFRESHER_SERVICE_B, Boolean.class));

        useSoundCheckBox.setChecked(isTrue(getSetting(USE_SOUND_B, Boolean.class)));

        workingHourStartPicker.setIs24HourView(true);
        workingHourStartPicker.setCurrentHour(getSetting(WORKING_HOURS_START_HOUR_I, Integer.class));
        workingHourStartPicker.setCurrentMinute(getSetting(WORKING_HOURS_START_MIN_I, Integer.class));

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

        if (errors.size() == 0) {
            //no errors, save and go back to main screen
            autoSettings.persistSettings();

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
