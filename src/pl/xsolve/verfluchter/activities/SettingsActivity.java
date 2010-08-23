package pl.xsolve.verfluchter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import pl.xsolve.verfluchter.R;
import pl.xsolve.verfluchter.services.RefreshService;
import pl.xsolve.verfluchter.services.WorkTimeNotifierService;
import pl.xsolve.verfluchter.tools.SoulTools;

/**
 * @author Konrad Ktoso Malawski
 */
public class SettingsActivity extends CommonViewActivity {

    Button saveButton;
    EditText domainEditText;
    EditText basicLoginEditText;
    EditText basicPasswordEditText;
    EditText loginEditText;
    EditText passwordEditText;
    CheckBox useWorkTimeNotifierCheckBox;
    CheckBox useRefresherCheckBox;
    TimePicker workingHourStartPicker;
    TimePicker workingHourEndPicker;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.settings);

        saveButton = (Button) findViewById(R.id.save_settings_button);

        domainEditText = (EditText) findViewById(R.id.domain_edittext);
        domainEditText.setText(super.<CharSequence>getSetting(SERVER_DOMAIN_S));

        basicLoginEditText = (EditText) findViewById(R.id.basic_login_edittext);
        basicLoginEditText.setText(super.<CharSequence>getSetting(BASIC_AUTH_USER_S));

        basicPasswordEditText = (EditText) findViewById(R.id.basic_password_edittext);

        loginEditText = (EditText) findViewById(R.id.login_edittext);
        loginEditText.setText(super.<CharSequence>getSetting(MY_AUTH_USER_S));

        passwordEditText = (EditText) findViewById(R.id.password_edittext);

        useWorkTimeNotifierCheckBox = (CheckBox) findViewById(R.id.timer_service_check_checkbox);
        useWorkTimeNotifierCheckBox.setChecked(super.<Boolean>getSetting(USE_REMINDER_SERVICE_B));

        useRefresherCheckBox = (CheckBox) findViewById(R.id.refresher_service_check_checkbox);
        useRefresherCheckBox.setChecked(super.<Boolean>getSetting(USE_REFRESHER_SERVICE_B));

        workingHourStartPicker = (TimePicker) findViewById(R.id.working_hour_start_picker);
        workingHourStartPicker.setIs24HourView(true);
        workingHourStartPicker.setCurrentHour(super.<Integer>getSetting(WORKING_HOURS_START_HOUR_I));
        workingHourStartPicker.setCurrentMinute(super.<Integer>getSetting(WORKING_HOURS_START_MIN_I));

        workingHourEndPicker = (TimePicker) findViewById(R.id.working_hour_end_picker);
        workingHourEndPicker.setIs24HourView(true);
        workingHourEndPicker.setCurrentHour(super.<Integer>getSetting(WORKING_HOURS_END_HOUR_I));
        workingHourEndPicker.setCurrentMinute(super.<Integer>getSetting(WORKING_HOURS_END_MIN_I));

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
        String domain = domainEditText.getText().toString();
        domain = domain.replaceAll("http://", "").replaceAll("https://", "");
        setSetting(SERVER_DOMAIN_S, domain);

        setSetting(BASIC_AUTH_USER_S, basicLoginEditText.getText().toString());
        String basicPass = basicPasswordEditText.getText().toString();
        if (!SoulTools.isEmpty(basicPass)) {
            setSetting(MY_AUTH_PASS_S, basicPass);
        }

        setSetting(MY_AUTH_USER_S, loginEditText.getText().toString());

        String pass = passwordEditText.getText().toString();
        if (!SoulTools.isEmpty(pass)) {
            setSetting(MY_AUTH_PASS_S, pass);
        }

        Integer startHour = workingHourStartPicker.getCurrentHour();
        setSetting(WORKING_HOURS_START_HOUR_I, startHour);
        Integer startMin = workingHourStartPicker.getCurrentMinute();
        setSetting(WORKING_HOURS_START_MIN_I, startMin);

        Integer endHour = workingHourEndPicker.getCurrentHour();
        setSetting(WORKING_HOURS_END_HOUR_I, endHour);
        Integer endMin = workingHourEndPicker.getCurrentMinute();
        setSetting(WORKING_HOURS_END_MIN_I, endMin);

        boolean useWorkTimeNotifier = useWorkTimeNotifierCheckBox.isChecked();
        setSetting(USE_REMINDER_SERVICE_B, useWorkTimeNotifier);
        if (useWorkTimeNotifier) {
            stopService(new Intent(this, WorkTimeNotifierService.class));
            startService(new Intent(this, WorkTimeNotifierService.class));
            showToast("Uaktywniono serwis powiadamiania o przekroczonym czasie pracy.");
        } else {
            stopService(new Intent(this, WorkTimeNotifierService.class));
            showToast("Wyłączono serwis powiadamiania o przekroczonym czasie pracy.");
        }

        boolean useRefresher = useRefresherCheckBox.isChecked();
        setSetting(USE_REFRESHER_SERVICE_B, useRefresher);
        if (useRefresher) {
            stopService(new Intent(this, RefreshService.class));
            startService(new Intent(this, RefreshService.class));
            showToast("Uaktywniono serwis automatycznego odświeżania danych.");
        } else {
            stopService(new Intent(this, RefreshService.class));
            showToast("Wyłączono serwis automatycznego odświeżania danych.");
        }

        startActivityForResult(new Intent(this, VerfluchterActivity.class), 0);
    }
}
