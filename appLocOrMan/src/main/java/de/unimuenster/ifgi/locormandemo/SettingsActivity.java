package de.unimuenster.ifgi.locormandemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class SettingsActivity extends AppCompatActivity {

    private boolean mLoggingAppend = true;
    private String mLogfileName = "";


    private EditText mLogfileNameEditText;
    private SwitchCompat mAppendSwitch;

    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mSharedPrefsEditor;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);








        mLogfileNameEditText = (EditText) findViewById(R.id.logfileName);
        mAppendSwitch = (SwitchCompat) findViewById(R.id.appendLoggingSwitch);
        Button updateLoggingStateButton = (Button) findViewById(R.id.updateLoggingSettings);

        // load shared prefs
        mSharedPrefs = getSharedPreferences(GlobalConstants.SHARED_PREFS_NAME,MODE_PRIVATE);
        mSharedPrefsEditor = mSharedPrefs.edit();
        mLogfileName = mSharedPrefs.getString(GlobalConstants.SHARED_PREFS_LOGFILE_NAME_KEY,GlobalConstants.DEFAULT_LOGFILE_NAME);
        mLoggingAppend = mSharedPrefs.getBoolean(GlobalConstants.SHARED_PREFS_LOGFILE_APPEND_KEY, true);

        // update UI
        mAppendSwitch.setChecked(mLoggingAppend);
        mLogfileNameEditText.setText(mLogfileName, TextView.BufferType.EDITABLE);

        updateLoggingStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFields();
                updateSharedPrefs();
            }
        });
    }

    private void updateSharedPrefs() {
        mSharedPrefsEditor.putString(GlobalConstants.SHARED_PREFS_LOGFILE_NAME_KEY, mLogfileName);
        mSharedPrefsEditor.putBoolean(GlobalConstants.SHARED_PREFS_LOGFILE_APPEND_KEY, mLoggingAppend);
        mSharedPrefsEditor.apply();
    }

    private void updateFields() {
        mLogfileName = mLogfileNameEditText.getText().toString();
        mLoggingAppend = mAppendSwitch.isChecked();
    }
}
