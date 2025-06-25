package com.autodialer.network;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class AutoDialerWebSocketClient extends WebSocketClient {
    private static final String TAG = "AutoDialerWebSocketClient";
    private WebSocketListener listener;

    public interface WebSocketListener {
        void onMessage(String message);
        void onError(Exception ex);
    }

    public AutoDialerWebSocketClient(String serverUrl, WebSocketListener listener) {
        super(URI.create(serverUrl));
        this.listener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.d(TAG, "WebSocket conectado");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "Mensagem recebida: " + message);
        if (listener != null) {
            listener.onMessage(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "WebSocket fechado: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "Erro WebSocket: " + ex.getMessage());
        if (listener != null) {
            listener.onError(ex);
        }
    }

    public void disconnect() {
        try {
            this.close();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desconectar WebSocket: " + e.getMessage());
        }
    }
}
