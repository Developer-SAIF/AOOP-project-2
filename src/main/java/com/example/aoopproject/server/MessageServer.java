package com.example.aoopproject.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class MessageServer extends WebSocketServer {
    private static final int PORT = 8887;
    private static MessageServer instance;
    private Map<String, WebSocket> userConnections = new HashMap<>();
    private Map<WebSocket, String> connectionUsers = new HashMap<>();

    public static synchronized MessageServer getInstance() {
        if (instance == null) {
            instance = new MessageServer();
        }
        return instance;
    }

    public MessageServer() {
        super(new InetSocketAddress(PORT));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection opened");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String userId = connectionUsers.get(conn);
        if (userId != null) {
            userConnections.remove(userId);
            connectionUsers.remove(conn);
            broadcastUserStatus(userId, false);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String type = jsonMessage.getString("type");

            switch (type) {
                case "register":
                    handleRegistration(conn, jsonMessage);
                    break;
                case "message":
                    handleMessage(jsonMessage);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRegistration(WebSocket conn, JSONObject jsonMessage) {
        String userId = jsonMessage.getString("userId");
        userConnections.put(userId, conn);
        connectionUsers.put(conn, userId);
        broadcastUserStatus(userId, true);
    }

    private void handleMessage(JSONObject jsonMessage) {
        String to = jsonMessage.getString("to");
        WebSocket recipientConn = userConnections.get(to);
        if (recipientConn != null && recipientConn.isOpen()) {
            recipientConn.send(jsonMessage.toString());
        }
    }

    private void broadcastUserStatus(String userId, boolean online) {
        JSONObject statusMessage = new JSONObject();
        statusMessage.put("type", "status");
        statusMessage.put("userId", userId);
        statusMessage.put("online", online);
        broadcast(statusMessage.toString());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error occurred on connection: " + ex.getMessage());
    }

    @Override
    public void onStart() {

    }

    public static void main(String[] args) {
        MessageServer server = MessageServer.getInstance();
        try {
            server.start();
            System.out.println("Message server started on port: " + PORT);

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down message server...");
                try {
                    server.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));

        } catch (Exception e) {
            System.err.println("Could not start server: " + e.getMessage());
            if (e instanceof BindException) {
                System.err.println("Port " + PORT + " is already in use. Please stop any other instances of MessageServer first.");
            }
            System.exit(1);
        }
    }
}