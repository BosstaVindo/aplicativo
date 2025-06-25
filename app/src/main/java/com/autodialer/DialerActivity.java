package com.autodialer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DialerActivity extends AppCompatActivity {
    private static final String TAG = "DialerActivity";
    
    private EditText phoneNumberEdit;
    private Button callButton;
    private Button backButton;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);

        initViews();
        setupClickListeners();
        handleIntent(getIntent());
    }

    private void initViews() {
        phoneNumberEdit = findViewById(R.id.phoneNumberEdit);
        callButton = findViewById(R.id.callButton);
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
    }

    private void setupClickListeners() {
        callButton.setOnClickListener(v -> makeCall());
        backButton.setOnClickListener(v -> finish());
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            Uri data = intent.getData();
            
            Log.d(TAG, "Intent recebido - Action: " + action + ", Data: " + data);
            
            if (Intent.ACTION_DIAL.equals(action) || Intent.ACTION_CALL.equals(action)) {
                if (data != null) {
                    String phoneNumber = data.getSchemeSpecificPart();
                    phoneNumberEdit.setText(phoneNumber);
                    
                    // Se for ACTION_CALL, fazer a chamada automaticamente
                    if (Intent.ACTION_CALL.equals(action)) {
                        makeCall();
                    }
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void makeCall() {
        String phoneNumber = phoneNumberEdit.getText().toString().trim();
        
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Digite um número de telefone", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Usar TelecomManager para fazer a chamada
            TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
            Uri uri = Uri.fromParts("tel", phoneNumber, null);
            
            telecomManager.placeCall(uri, null);
            
            Log.d(TAG, "Chamada iniciada para: " + phoneNumber);
            Toast.makeText(this, "Ligando para " + phoneNumber, Toast.LENGTH_SHORT).show();
            
        } catch (SecurityException e) {
            Log.e(TAG, "Erro de permissão ao fazer chamada: " + e.getMessage());
            Toast.makeText(this, "Erro: Permissão negada", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao fazer chamada: " + e.getMessage());
            Toast.makeText(this, "Erro ao fazer chamada", Toast.LENGTH_SHORT).show();
        }
    }
}
