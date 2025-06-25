package com.autodialer.service;

import android.content.Intent;
import android.telecom.Call;
import android.telecom.InCallService;
import android.util.Log;

import com.autodialer.InCallActivity;

public class AutoDialerInCallService extends InCallService {
    private static final String TAG = "AutoDialerInCallService";

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        Log.d(TAG, "Chamada adicionada: " + call.getDetails().getHandle());
        
        // Iniciar a atividade de chamada em andamento
        Intent intent = new Intent(this, InCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        Log.d(TAG, "Chamada removida: " + call.getDetails().getHandle());
    }
}
