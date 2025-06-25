package com.autodialer.telecom;

import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.util.Log;

public class AutoDialerConnectionService extends ConnectionService {
    private static final String TAG = "AutoDialerConnectionService";
    
    // Constantes de apresentação
    private static final int PRESENTATION_ALLOWED = 1;

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "Criando conexão de saída para: " + request.getAddress());
        
        AutoDialerConnection connection = new AutoDialerConnection();
        connection.setAddress(request.getAddress(), PRESENTATION_ALLOWED);
        connection.setCallerDisplayName("Auto Dialer", PRESENTATION_ALLOWED);
        
        return connection;
    }

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "Criando conexão de entrada");
        return null; // Não suportamos chamadas de entrada
    }

    private class AutoDialerConnection extends Connection {
        
        public AutoDialerConnection() {
            setConnectionCapabilities(CAPABILITY_SUPPORT_HOLD | CAPABILITY_HOLD);
            setAudioModeIsVoip(false);
        }

        @Override
        public void onAnswer() {
            Log.d(TAG, "Chamada atendida");
            setActive();
        }

        @Override
        public void onReject() {
            Log.d(TAG, "Chamada rejeitada");
            setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
            destroy();
        }

        @Override
        public void onDisconnect() {
            Log.d(TAG, "Chamada desconectada");
            setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
            destroy();
        }

        @Override
        public void onHold() {
            Log.d(TAG, "Chamada em espera");
            setOnHold();
        }

        @Override
        public void onUnhold() {
            Log.d(TAG, "Chamada retirada da espera");
            setActive();
        }
    }
}
