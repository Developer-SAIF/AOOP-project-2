package com.example.aoopproject.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageServer extends WebSocketServer {
    private static final int PORT = 8887;
    private static MessageServer instance;
    private final Map<String, WebSocket> userConnections = new HashMap<>();
    private final Map<WebSocket, String> connectionUsers = new HashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool(); // Dynamic thread pool

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
        threadPool.submit(() -> {
            String userId = connectionUsers.get(conn);
            if (userId != null) {
                userConnections.remove(userId);
                connectionUsers.remove(conn);
                broadcastUserStatus(userId, false);
            }
        });
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        threadPool.submit(() -> {
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
                System.err.println("Error processing message: " + e.getMessage());
            }
        });
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
        System.out.println("Server started successfully.");
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
                    server.threadPool.shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));

        } catch (Exception e) {
            System.err.println("Could not start server: " + e.getMessage());
            System.exit(1);
        }
    }
}
