package com.autodialer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRScannerActivity extends AppCompatActivity {
    private static final String TAG = "QRScannerActivity";

    private DecoratedBarcodeView barcodeView;
    private Button backButton;
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        initViews();
        setupScanner();
    }

    private void initViews() {
        barcodeView = findViewById(R.id.barcode_scanner);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void setupScanner() {
        barcodeView.setStatusText("Posicione o QR Code dentro do quadro");

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (!isScanning) return;

                isScanning = false;
                String qrContent = result.getText();

                Log.d(TAG, "QR Code lido: " + qrContent);
                processQRCode(qrContent);
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Pontos de resultado possíveis (opcional)
            }
        });
    }

    private void processQRCode(String content) {
        try {
            // Processar diferentes tipos de QR Code
            if (content.startsWith("AUTODIALER:")) {
                processAutoDialerQR(content);
            } else if (content.startsWith("CONFIG:")) {
                processConfigQR(content);
            } else if (content.startsWith("NUMBERS:")) {
                processNumbersQR(content);
            } else if (content.startsWith("SERVER:")) {
                processServerQR(content);
            } else {
                // QR Code genérico - tratar como número de telefone ou URL
                processGenericQR(content);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar QR Code: " + e.getMessage());
            Toast.makeText(this, "Erro ao processar QR Code", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void processAutoDialerQR(String content) {
        // Formato: AUTODIALER:ACTION:DATA
        String[] parts = content.split(":", 3);
        if (parts.length >= 3) {
            String action = parts[1];
            String data = parts[2];

            Intent resultIntent = new Intent();
            resultIntent.putExtra("qr_type", "autodialer");
            resultIntent.putExtra("action", action);
            resultIntent.putExtra("data", data);

            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Comando AutoDialer: " + action, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void processConfigQR(String content) {
        // Formato: CONFIG:conference_size=6;retry_attempts=3;call_timeout=30
        String configData = content.substring(7); // Remove "CONFIG:"

        Intent resultIntent = new Intent();
        resultIntent.putExtra("qr_type", "config");
        resultIntent.putExtra("config_data", configData);

        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Configuração recebida", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void processNumbersQR(String content) {
        // Formato: NUMBERS:11999999999,João;11888888888,Maria
        String numbersData = content.substring(8); // Remove "NUMBERS:"

        Intent resultIntent = new Intent();
        resultIntent.putExtra("qr_type", "numbers");
        resultIntent.putExtra("numbers_data", numbersData);

        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Lista de números recebida", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void processServerQR(String content) {
        // Formato: SERVER:ws://192.168.1.100:8080/ws
        String serverUrl = content.substring(7); // Remove "SERVER:"

        Intent resultIntent = new Intent();
        resultIntent.putExtra("qr_type", "server");
        resultIntent.putExtra("server_url", serverUrl);

        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "URL do servidor: " + serverUrl, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void processGenericQR(String content) {
        // Verificar se é um número de telefone
        String cleanNumber = content.replaceAll("[^0-9+]", "");
        if (cleanNumber.length() >= 10) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("qr_type", "phone");
            resultIntent.putExtra("phone_number", cleanNumber);

            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Número: " + cleanNumber, Toast.LENGTH_SHORT).show();
        } else {
            // Tratar como texto genérico
            Intent resultIntent = new Intent();
            resultIntent.putExtra("qr_type", "text");
            resultIntent.putExtra("text_content", content);

            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Texto: " + content, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
