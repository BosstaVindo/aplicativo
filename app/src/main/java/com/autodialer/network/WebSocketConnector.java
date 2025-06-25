
package com.autodialer;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;

public class WebSocketConnector {

    private WebSocket webSocket;

    public void connectToServer(String serverUrl) {
        if (serverUrl == null || !serverUrl.contains("session=")) {
            Log.e("WebSocket", "URL inválida: " + serverUrl);
            return;
        }

        String session = serverUrl.split("session=")[1];
        String wsUrl = "wss://autodialer-system.onrender.com/api/websocket?session=" + session;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("WebSocket", "Conectado com sucesso!");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocket", "Mensagem recebida: " + text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Erro na conexão: " + t.getMessage());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d("WebSocket", "Conexão encerrando: " + reason);
            }
        });
    }
}
