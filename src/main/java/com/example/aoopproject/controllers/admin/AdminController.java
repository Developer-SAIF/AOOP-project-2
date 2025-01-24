package com.example.aoopproject.controllers.admin;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class AdminController {

    private WebSocketClient webSocketClient;

    public void cleanup() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.close();
        }
    }
}
