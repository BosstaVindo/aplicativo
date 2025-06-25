package com.autodialer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.autodialer.service.AutoDialerService;

public class CommandReceiver extends BroadcastReceiver {
    private static final String TAG = "CommandReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.autodialer.COMMAND".equals(intent.getAction())) {
            String command = intent.getStringExtra("command");
            Log.d(TAG, "Comando recebido: " + command);

            // Repassar comando para o serviço
            Intent serviceIntent = new Intent(context, AutoDialerService.class);
            serviceIntent.setAction(command);
            
            // Copiar extras do intent original
            if (intent.getExtras() != null) {
                serviceIntent.putExtras(intent.getExtras());
            }
            
            context.startService(serviceIntent);
        }
    }
}
