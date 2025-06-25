package com.autodialer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.autodialer.MainActivity;
import com.autodialer.R;
import com.autodialer.model.CallTask;
import com.autodialer.network.AutoDialerWebSocketClient;
import com.autodialer.utils.CallManager;
import com.autodialer.utils.RootHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AutoDialerService extends Service {
    private static final String TAG = "AutoDialerService";
    private static final String CHANNEL_ID = "AutoDialerChannel";
    private static final int NOTIFICATION_ID = 1001;
    
    private static boolean isRunning = false;
    
    private Handler mainHandler;
    private Queue<CallTask> callQueue;
    private List<String> activeConference;
    private CallManager callManager;
    private AutoDialerWebSocketClient webSocketClient;
    
    private boolean isDialing = false;
    private int maxConferenceSize = 6;
    private int retryAttempts = 3;
    private int callTimeout = 30000;
    private int delayBetweenCalls = 5000;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Serviço criado");
        
        mainHandler = new Handler(Looper.getMainLooper());
        callQueue = new LinkedList<>();
        activeConference = new ArrayList<>();
        callManager = new CallManager(this);
        
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Inicializar WebSocket para comunicação com servidor
        initWebSocket();
        
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Comando recebido: " + (intent != null ? intent.getAction() : "null"));
        
        if (intent != null) {
            String action = intent.getAction();
            
            if ("START_SERVICE".equals(action)) {
                startAutoDialer();
            } else if ("STOP_SERVICE".equals(action)) {
                stopAutoDialer();
            } else if ("START_CALLING".equals(action)) {
                handleStartCalling(intent);
            } else if ("STOP_CALLING".equals(action)) {
                handleStopCalling();
            } else if ("ADD_TO_CONFERENCE".equals(action)) {
                handleAddToConference(intent);
            }
        }
        
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Auto Dialer Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Serviço de discagem automática");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Auto Dialer Ativo")
                .setContentText("Serviço de discagem automática em execução")
                .setSmallIcon(R.drawable.ic_phone)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void initWebSocket() {
        try {
            webSocketClient = new AutoDialerWebSocketClient("ws://localhost:8080/ws", new AutoDialerWebSocketClient.WebSocketListener() {
                @Override
                public void onMessage(String message) {
                    handleWebSocketMessage(message);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "Erro WebSocket: " + ex.getMessage());
                }
            });
            
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar WebSocket: " + e.getMessage());
        }
    }

    private void handleWebSocketMessage(String message) {
        // Processar comandos recebidos do servidor web
        Log.d(TAG, "Mensagem WebSocket: " + message);
        
        // Implementar parsing de comandos JSON
        // Exemplo: {"action": "START_CALLING", "numbers": [...]}
    }

    private void startAutoDialer() {
        Log.d(TAG, "Iniciando auto dialer");
        isDialing = true;
        updateNotification("Auto Dialer - Aguardando comandos");
    }

    private void stopAutoDialer() {
        Log.d(TAG, "Parando auto dialer");
        isDialing = false;
        callQueue.clear();
        endAllCalls();
        updateNotification("Auto Dialer - Parado");
    }

    private void handleStartCalling(Intent intent) {
        ArrayList<String> numbers = intent.getStringArrayListExtra("numbers");
        if (numbers != null && !numbers.isEmpty()) {
            
            // Converter números em tarefas de chamada
            for (String number : numbers) {
                CallTask task = new CallTask(number, 0, retryAttempts);
                callQueue.offer(task);
            }
            
            Log.d(TAG, "Adicionadas " + numbers.size() + " chamadas à fila");
            updateNotification("Processando " + callQueue.size() + " chamadas");
            
            // Iniciar processamento
            processNextCall();
        }
    }

    private void handleStopCalling() {
        callQueue.clear();
        endAllCalls();
        updateNotification("Chamadas interrompidas");
    }

    private void handleAddToConference(Intent intent) {
        String number = intent.getStringExtra("number");
        if (number != null) {
            addToConference(number);
        }
    }

    private void processNextCall() {
        if (!isDialing || callQueue.isEmpty()) {
            updateNotification("Todas as chamadas processadas");
            return;
        }

        // Verificar se a conferência está cheia
        if (activeConference.size() >= maxConferenceSize) {
            // Aguardar espaço na conferência
            mainHandler.postDelayed(this::processNextCall, delayBetweenCalls);
            return;
        }

        CallTask task = callQueue.poll();
        if (task != null) {
            updateNotification("Ligando para " + task.getNumber());
            makeCall(task);
        }
    }

    private void makeCall(CallTask task) {
        String phoneNumber = task.getNumber();
        Log.d(TAG, "Fazendo chamada para: " + phoneNumber);

        try {
            // Método 1: Usar TelecomManager (padrão)
            TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
            Uri uri = Uri.fromParts("tel", phoneNumber, null);
            
            telecomManager.placeCall(uri, null);
            
            // Monitorar resultado da chamada
            monitorCall(task);
            
        } catch (SecurityException e) {
            Log.w(TAG, "Falha no método padrão, tentando com root");
            makeCallWithRoot(task);
        }
    }

    private void makeCallWithRoot(CallTask task) {
        String phoneNumber = task.getNumber();
        
        if (!RootHelper.isRootAvailable()) {
            Log.e(TAG, "Root não disponível para chamada: " + phoneNumber);
            retryOrNext(task);
            return;
        }

        try {
            // Usar comandos root para fazer chamada
            String command = "am start -a android.intent.action.CALL -d tel:" + phoneNumber;
            boolean success = RootHelper.executeRootCommand(command);
            
            if (success) {
                Log.d(TAG, "Chamada iniciada com root: " + phoneNumber);
                monitorCall(task);
            } else {
                Log.e(TAG, "Falha ao executar comando root");
                retryOrNext(task);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro na chamada com root: " + e.getMessage());
            retryOrNext(task);
        }
    }

    private void monitorCall(CallTask task) {
        // Aguardar resposta da chamada
        mainHandler.postDelayed(() -> {
            if (callManager.isCallAnswered(task.getNumber())) {
                onCallAnswered(task);
            } else {
                onCallFailed(task);
            }
        }, callTimeout);
    }

    private void onCallAnswered(CallTask task) {
        Log.d(TAG, "Chamada atendida: " + task.getNumber());
        addToConference(task.getNumber());
        
        // Processar próxima chamada após delay
        mainHandler.postDelayed(this::processNextCall, delayBetweenCalls);
    }

    private void onCallFailed(CallTask task) {
        Log.d(TAG, "Chamada falhou: " + task.getNumber());
        retryOrNext(task);
    }

    private void addToConference(String phoneNumber) {
        if (activeConference.size() < maxConferenceSize) {
            activeConference.add(phoneNumber);
            Log.d(TAG, "Adicionado à conferência: " + phoneNumber + " (" + activeConference.size() + "/" + maxConferenceSize + ")");
            
            // Implementar lógica de conferência usando TelecomManager
            callManager.addToConference(phoneNumber);
            
            updateNotification("Conferência: " + activeConference.size() + "/" + maxConferenceSize);
        }
    }

    private void retryOrNext(CallTask task) {
        task.incrementAttempts();
        
        if (task.getAttempts() < task.getMaxRetries()) {
            // Recolocar na fila para retry
            callQueue.offer(task);
            Log.d(TAG, "Reagendando chamada: " + task.getNumber() + " (tentativa " + task.getAttempts() + ")");
        } else {
            Log.d(TAG, "Máximo de tentativas atingido para: " + task.getNumber());
        }
        
        // Processar próxima chamada
        mainHandler.postDelayed(this::processNextCall, delayBetweenCalls);
    }

    private void endAllCalls() {
        Log.d(TAG, "Encerrando todas as chamadas");
        
        // Usar CallManager para encerrar chamadas
        callManager.endAllCalls();
        activeConference.clear();
    }

    private void updateNotification(String text) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Auto Dialer")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_phone)
                .setOngoing(true)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }

    public static boolean isServiceRunning() {
        return isRunning;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Serviço destruído");
        
        isRunning = false;
        isDialing = false;
        
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
        
        endAllCalls();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
