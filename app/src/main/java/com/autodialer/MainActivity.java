package com.autodialer;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.autodialer.service.AutoDialerService;
import com.autodialer.utils.PermissionHelper;
import com.autodialer.utils.RootHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSIONS = 1001;
    private static final int REQUEST_DEFAULT_DIALER = 1002;
    private static final int REQUEST_OVERLAY_PERMISSION = 1003;
    private static final int REQUEST_QR_SCAN = 1004;
    
    private TextView statusText;
    private TextView rootStatusText;
    private TextView permissionsText;
    private Button setDefaultButton;
    private Button requestPermissionsButton;
    private Button startServiceButton;
    private Button stopServiceButton;
    private Button settingsButton;
    private Button qrScanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        checkAllStatus();
    }

    private void initViews() {
        statusText = findViewById(R.id.statusText);
        rootStatusText = findViewById(R.id.rootStatusText);
        permissionsText = findViewById(R.id.permissionsText);
        setDefaultButton = findViewById(R.id.setDefaultButton);
        requestPermissionsButton = findViewById(R.id.requestPermissionsButton);
        startServiceButton = findViewById(R.id.startServiceButton);
        stopServiceButton = findViewById(R.id.stopServiceButton);
        settingsButton = findViewById(R.id.settingsButton);
        qrScanButton = findViewById(R.id.qrScanButton);
    }

    private void setupClickListeners() {
        setDefaultButton.setOnClickListener(v -> showDialerOptions());
        requestPermissionsButton.setOnClickListener(v -> requestAllPermissions());
        startServiceButton.setOnClickListener(v -> startAutoDialerService());
        stopServiceButton.setOnClickListener(v -> stopAutoDialerService());
        settingsButton.setOnClickListener(v -> openSettings());
        qrScanButton.setOnClickListener(v -> openQRScanner());
    }

    private void checkAllStatus() {
        checkRootStatus();
        checkPermissions();
        checkDefaultDialer();
        updateServiceButtons();
    }

    private void checkRootStatus() {
        boolean hasRoot = RootHelper.isRootAvailable();
        rootStatusText.setText(hasRoot ? "✅ ROOT disponível" : "❌ ROOT não disponível");
        
        if (hasRoot) {
            rootStatusText.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            rootStatusText.setTextColor(getColor(android.R.color.holo_red_dark));
        }
    }

    private void checkPermissions() {
        Log.d(TAG, "Verificando permissões...");
        
        boolean allGranted = PermissionHelper.hasAllPermissions(this);
        String[] missingPermissions = PermissionHelper.getMissingPermissions(this);
        
        Log.d(TAG, "Todas as permissões concedidas: " + allGranted);
        Log.d(TAG, "Permissões faltando: " + missingPermissions.length);
        
        if (allGranted) {
            permissionsText.setText("✅ Todas as permissões concedidas");
            permissionsText.setTextColor(getColor(android.R.color.holo_green_dark));
            requestPermissionsButton.setVisibility(View.GONE);
        } else {
            permissionsText.setText("❌ Permissões pendentes (" + missingPermissions.length + ")");
            permissionsText.setTextColor(getColor(android.R.color.holo_red_dark));
            requestPermissionsButton.setVisibility(View.VISIBLE);
            
            // Log das permissões faltando
            for (String permission : missingPermissions) {
                Log.d(TAG, "Permissão faltando: " + permission);
            }
        }
    }

    private void checkDefaultDialer() {
        Log.d(TAG, "Verificando discador padrão...");
        
        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        if (telecomManager == null) {
            Log.e(TAG, "TelecomManager é null");
            statusText.setText("❌ Erro ao verificar discador");
            return;
        }
        
        String defaultDialer = telecomManager.getDefaultDialerPackage();
        String currentPackage = getPackageName();
        
        Log.d(TAG, "Discador padrão atual: " + defaultDialer);
        Log.d(TAG, "Nosso pacote: " + currentPackage);
        
        if (currentPackage.equals(defaultDialer)) {
            statusText.setText("✅ Configurado como discador padrão");
            statusText.setTextColor(getColor(android.R.color.holo_green_dark));
            setDefaultButton.setVisibility(View.GONE);
            registerPhoneAccount();
        } else {
            statusText.setText("❌ Não é o discador padrão");
            statusText.setTextColor(getColor(android.R.color.holo_red_dark));
            setDefaultButton.setVisibility(View.VISIBLE);
        }
    }

    private void registerPhoneAccount() {
        try {
            TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
            
            ComponentName componentName = new ComponentName(this, 
                "com.autodialer.telecom.AutoDialerConnectionService");
            
            PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(componentName, "AutoDialer");
            
            PhoneAccount phoneAccount = PhoneAccount.builder(phoneAccountHandle, "AutoDialer")
                    .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
                    .build();
            
            telecomManager.registerPhoneAccount(phoneAccount);
            Log.d(TAG, "PhoneAccount registrado com sucesso");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao registrar PhoneAccount: " + e.getMessage());
        }
    }

    private void showDialerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Definir como Discador Padrão");
        builder.setMessage("Escolha o método para definir este app como discador padrão:");
        
        builder.setPositiveButton("Método Automático", (dialog, which) -> {
            requestDefaultDialerAutomatic();
        });
        
        builder.setNeutralButton("Configurações do Sistema", (dialog, which) -> {
            openSystemDialerSettings();
        });
        
        builder.setNegativeButton("Cancelar", null);
        
        builder.show();
    }

    private void requestDefaultDialerAutomatic() {
        Log.d(TAG, "Solicitando para ser discador padrão automaticamente...");
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Usar RoleManager
                RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
                if (roleManager != null) {
                    if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                        if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                            startActivityForResult(intent, REQUEST_DEFAULT_DIALER);
                        } else {
                            Toast.makeText(this, "Já é o discador padrão", Toast.LENGTH_SHORT).show();
                            checkDefaultDialer();
                        }
                    } else {
                        Log.w(TAG, "Role DIALER não disponível, tentando método alternativo");
                        requestDefaultDialerLegacy();
                    }
                } else {
                    Log.w(TAG, "RoleManager não disponível, tentando método alternativo");
                    requestDefaultDialerLegacy();
                }
            } else {
                // Android 9 e anteriores
                requestDefaultDialerLegacy();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao solicitar discador padrão: " + e.getMessage());
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
            openSystemDialerSettings();
        }
    }

    private void requestDefaultDialerLegacy() {
        try {
            Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
            startActivityForResult(intent, REQUEST_DEFAULT_DIALER);
        } catch (Exception e) {
            Log.e(TAG, "Erro no método legacy: " + e.getMessage());
            openSystemDialerSettings();
        }
    }

    private void openSystemDialerSettings() {
        try {
            // Tentar abrir configurações específicas do discador
            Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            startActivity(intent);
            
            Toast.makeText(this, 
                "Vá em 'App de telefone' e selecione 'Auto Dialer'", 
                Toast.LENGTH_LONG).show();
                
        } catch (Exception e) {
            try {
                // Fallback para configurações gerais de aplicativos
                Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                startActivity(intent);
                
                Toast.makeText(this, 
                    "Procure por 'Aplicativos padrão' > 'App de telefone' e selecione 'Auto Dialer'", 
                    Toast.LENGTH_LONG).show();
                    
            } catch (Exception e2) {
                Toast.makeText(this, 
                    "Abra Configurações > Aplicativos > Aplicativos padrão > App de telefone", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestAllPermissions() {
        Log.d(TAG, "Solicitando permissões...");
        
        String[] permissions = PermissionHelper.getAllPermissions();
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        
        // Solicitar permissão de sobreposição separadamente
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        }
    }

    private void startAutoDialerService() {
        Intent serviceIntent = new Intent(this, AutoDialerService.class);
        serviceIntent.setAction("START_SERVICE");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        Toast.makeText(this, "Serviço de discagem iniciado", Toast.LENGTH_SHORT).show();
        updateServiceButtons();
    }

    private void stopAutoDialerService() {
        Intent serviceIntent = new Intent(this, AutoDialerService.class);
        stopService(serviceIntent);
        
        Toast.makeText(this, "Serviço de discagem parado", Toast.LENGTH_SHORT).show();
        updateServiceButtons();
    }

    private void updateServiceButtons() {
        boolean isServiceRunning = AutoDialerService.isServiceRunning();
        boolean canStartService = canStartService();
        
        startServiceButton.setEnabled(!isServiceRunning && canStartService);
        stopServiceButton.setEnabled(isServiceRunning);
        
        Log.d(TAG, "Service running: " + isServiceRunning + ", Can start: " + canStartService);
    }

    private boolean canStartService() {
        boolean hasPermissions = PermissionHelper.hasAllPermissions(this);
        
        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        boolean isDefaultDialer = getPackageName().equals(telecomManager.getDefaultDialerPackage());
        
        Log.d(TAG, "Has permissions: " + hasPermissions + ", Is default dialer: " + isDefaultDialer);
        
        return hasPermissions && isDefaultDialer;
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openQRScanner() {
        if (!PermissionHelper.hasPermission(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
            return;
        }
        
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivityForResult(intent, REQUEST_QR_SCAN);
    }

    private void handleQRResult(Intent data) {
        String qrType = data.getStringExtra("qr_type");
        
        switch (qrType) {
            case "autodialer":
                handleAutoDialerQR(data);
                break;
            case "config":
                handleConfigQR(data);
                break;
            case "numbers":
                handleNumbersQR(data);
                break;
            case "server":
                handleServerQR(data);
                break;
            case "phone":
                handlePhoneQR(data);
                break;
            default:
                String content = data.getStringExtra("text_content");
                Toast.makeText(this, "QR Code lido: " + content, Toast.LENGTH_LONG).show();
        }
    }

    private void handleAutoDialerQR(Intent data) {
        String action = data.getStringExtra("action");
        String qrData = data.getStringExtra("data");
        
        Log.d(TAG, "AutoDialer QR - Action: " + action + ", Data: " + qrData);
        
        if ("START_CALLING".equals(action)) {
            // Processar lista de números do QR
            ArrayList<String> numbers = new ArrayList<>();
            String[] numberArray = qrData.split(";");
            for (String number : numberArray) {
                numbers.add(number.trim());
            }
            
            Intent serviceIntent = new Intent(this, AutoDialerService.class);
            serviceIntent.setAction("START_CALLING");
            serviceIntent.putStringArrayListExtra("numbers", numbers);
            startService(serviceIntent);
            
            Toast.makeText(this, "Iniciando chamadas via QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleConfigQR(Intent data) {
        String configData = data.getStringExtra("config_data");
        
        // Processar configurações: conference_size=6;retry_attempts=3;call_timeout=30
        String[] configs = configData.split(";");
        SharedPreferences prefs = getSharedPreferences("AutoDialerSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        for (String config : configs) {
            String[] keyValue = config.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                
                try {
                    int intValue = Integer.parseInt(value);
                    editor.putInt(key, intValue);
                } catch (NumberFormatException e) {
                    editor.putString(key, value);
                }
            }
        }
        
        editor.apply();
        Toast.makeText(this, "Configurações aplicadas via QR Code", Toast.LENGTH_SHORT).show();
    }

    private void handleNumbersQR(Intent data) {
        String numbersData = data.getStringExtra("numbers_data");
        
        // Abrir DialerActivity com os números
        Intent intent = new Intent(this, DialerActivity.class);
        intent.putExtra("numbers_list", numbersData);
        startActivity(intent);
    }

    private void handleServerQR(Intent data) {
        String serverUrl = data.getStringExtra("server_url");
        
        // Salvar URL do servidor
        SharedPreferences prefs = getSharedPreferences("AutoDialerSettings", MODE_PRIVATE);
        prefs.edit().putString("server_url", serverUrl).apply();
        
        Toast.makeText(this, "URL do servidor configurada", Toast.LENGTH_SHORT).show();
    }

    private void handlePhoneQR(Intent data) {
        String phoneNumber = data.getStringExtra("phone_number");
        
        // Abrir DialerActivity com o número
        Intent intent = new Intent(this, DialerActivity.class);
        intent.setData(Uri.fromParts("tel", phoneNumber, null));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "onActivityResult: " + requestCode + ", result: " + resultCode);
        
        if (requestCode == REQUEST_DEFAULT_DIALER) {
            // Aguardar um pouco antes de verificar
            new android.os.Handler().postDelayed(() -> {
                checkDefaultDialer();
                updateServiceButtons();
                
                // Verificar se foi definido com sucesso
                TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
                if (getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
                    Toast.makeText(this, "✅ Definido como discador padrão com sucesso!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "❌ Não foi possível definir como discador padrão", Toast.LENGTH_LONG).show();
                }
            }, 1000);
        } else if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            checkPermissions();
            updateServiceButtons();
        } else if (requestCode == REQUEST_QR_SCAN) {
            if (resultCode == RESULT_OK && data != null) {
                handleQRResult(data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
        
        if (requestCode == REQUEST_PERMISSIONS) {
            // Aguardar um pouco antes de verificar
            new android.os.Handler().postDelayed(() -> {
                checkPermissions();
                updateServiceButtons();
            }, 500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - verificando status");
        checkAllStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        } else if (id == R.id.action_refresh) {
            checkAllStatus();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
