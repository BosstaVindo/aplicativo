// Receptor para monitorar estado das chamadas
package com.autodialer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static boolean isIncoming = false;
    private static String savedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (number != null && number.length() > 0) {
                savedNumber = number;
                isIncoming = true;
            } else {
                isIncoming = false;
            }

            if (lastState.equals(TelephonyManager.EXTRA_STATE_RINGING) &&
                state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                // Chamada foi atendida
                onCallAnswered(context, savedNumber);
            }

            if (lastState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) &&
                state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                // Chamada foi encerrada
                onCallEnded(context, savedNumber);
            }

            lastState = state;
        }
    }

    private void onCallAnswered(Context context, String number) {
        Log.d(TAG, "Chamada atendida: " + number);
        
        // Notificar o serviço que a chamada foi atendida
        Intent serviceIntent = new Intent(context, AutoDialerService.class);
        serviceIntent.putExtra("action", "CALL_ANSWERED");
        serviceIntent.putExtra("number", number);
        context.startService(serviceIntent);
        
        // Reportar para o servidor
        reportCallStatus(context, number, "answered");
    }

    private void onCallEnded(Context context, String number) {
        Log.d(TAG, "Chamada encerrada: " + number);
        
        // Notificar o serviço que a chamada foi encerrada
        Intent serviceIntent = new Intent(context, AutoDialerService.class);
        serviceIntent.putExtra("action", "CALL_ENDED");
        serviceIntent.putExtra("number", number);
        context.startService(serviceIntent);
        
        // Reportar para o servidor
        reportCallStatus(context, number, "ended");
    }

    private void reportCallStatus(Context context, String number, String status) {
        // Implementar envio de status para o servidor web
        // Pode usar HTTP request ou WebSocket
    }
}
