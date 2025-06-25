package com.autodialer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AutoDialerSettings";
    
    private EditText conferenceSizeEdit;
    private EditText retryAttemptsEdit;
    private EditText callTimeoutEdit;
    private EditText delayBetweenCallsEdit;
    private EditText serverUrlEdit;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSettings();
        setupClickListeners();
    }

    private void initViews() {
        conferenceSizeEdit = findViewById(R.id.conferenceSizeEdit);
        retryAttemptsEdit = findViewById(R.id.retryAttemptsEdit);
        callTimeoutEdit = findViewById(R.id.callTimeoutEdit);
        delayBetweenCallsEdit = findViewById(R.id.delayBetweenCallsEdit);
        serverUrlEdit = findViewById(R.id.serverUrlEdit);
        saveButton = findViewById(R.id.saveButton);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        conferenceSizeEdit.setText(String.valueOf(prefs.getInt("conference_size", 6)));
        retryAttemptsEdit.setText(String.valueOf(prefs.getInt("retry_attempts", 3)));
        callTimeoutEdit.setText(String.valueOf(prefs.getInt("call_timeout", 30)));
        delayBetweenCallsEdit.setText(String.valueOf(prefs.getInt("delay_between_calls", 5)));
        serverUrlEdit.setText(prefs.getString("server_url", "ws://localhost:8080/ws"));
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            editor.putInt("conference_size", Integer.parseInt(conferenceSizeEdit.getText().toString()));
            editor.putInt("retry_attempts", Integer.parseInt(retryAttemptsEdit.getText().toString()));
            editor.putInt("call_timeout", Integer.parseInt(callTimeoutEdit.getText().toString()));
            editor.putInt("delay_between_calls", Integer.parseInt(delayBetweenCallsEdit.getText().toString()));
            editor.putString("server_url", serverUrlEdit.getText().toString());
            
            editor.apply();
            
            Toast.makeText(this, "Configurações salvas", Toast.LENGTH_SHORT).show();
            finish();
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Erro: Valores inválidos", Toast.LENGTH_SHORT).show();
        }
    }
}
