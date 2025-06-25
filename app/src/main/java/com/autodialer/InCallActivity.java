package com.autodialer;

import android.os.Bundle;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InCallActivity extends AppCompatActivity {
    private static final String TAG = "InCallActivity";
    
    private TextView callStatusText;
    private TextView phoneNumberText;
    private Button endCallButton;
    private Button muteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incall);

        initViews();
        setupClickListeners();
        updateCallInfo();
    }

    private void initViews() {
        callStatusText = findViewById(R.id.callStatusText);
        phoneNumberText = findViewById(R.id.phoneNumberText);
        endCallButton = findViewById(R.id.endCallButton);
        muteButton = findViewById(R.id.muteButton);
    }

    private void setupClickListeners() {
        endCallButton.setOnClickListener(v -> endCall());
        muteButton.setOnClickListener(v -> toggleMute());
    }

    private void updateCallInfo() {
        // Implementar lógica para mostrar informações da chamada
        callStatusText.setText("Chamada em andamento");
        phoneNumberText.setText("Número desconhecido");
    }

    private void endCall() {
        // Implementar lógica para encerrar chamada
        Log.d(TAG, "Encerrando chamada");
        finish();
    }

    private void toggleMute() {
        // Implementar lógica para mute/unmute
        Log.d(TAG, "Toggle mute");
    }
}
