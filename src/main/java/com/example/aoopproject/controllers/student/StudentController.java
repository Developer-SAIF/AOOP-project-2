package com.example.aoopproject.controllers.student;

import com.example.aoopproject.database.DatabaseConnection;
import com.example.aoopproject.models.SharedFile;
import com.example.aoopproject.models.User;
import com.example.aoopproject.models.UserSession;
import com.example.aoopproject.services.MessagePollingService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javafx.scene.control.TextArea;
import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.example.aoopproject.models.Message;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class StudentController implements Initializable {

    @FXML
    public Tab dashboardTab;

    @FXML
    public Tab messageTab;

    @FXML
    public Tab documentsTab;

    @FXML
    public Tab qnaTab;

    @FXML
    public Tab settingsTab;

    @FXML
    public Tab notificationsTab;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        // Journal part starts here

        journalEntries = FXCollections.observableArrayList();
        journalListView.setItems(journalEntries);

        try {
            String userHome = System.getProperty("user.home");
            journalsFilePath = Paths.get(userHome, ".aoopproject", "journals.json");

            if (!Files.exists(journalsFilePath.getParent())) {
                Files.createDirectories(journalsFilePath.getParent());
            }

            if (!Files.exists(journalsFilePath)) {
                Files.createFile(journalsFilePath);
                Files.writeString(journalsFilePath, "[]"); // Initialize with an empty JSON array
                System.out.println("Created new journals.json file at: " + journalsFilePath); // Log the creation
            }
            loadJournals();

        } catch (IOException e) {
            System.err.println("Error creating or loading journals: " + e.getMessage());
        }

        // Journal part ends here

        // Notice part starts here

        notices = FXCollections.observableArrayList();
        noticeListView.setItems(notices);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        refreshNotices();

        // Notice part ends here

        // Message part starts here
        initializeMessaging();
        // Message part ends here

        // File sharing part starts here

        setupListView();
        loadFiles();
        setupAddLinkButton();

        // File sharing part ends here
    }

    // Journal tab controller starts here

    @FXML
    public Tab journalTab;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea journalTextArea;

    @FXML
    private ListView<JournalEntry> journalListView;
    private ObservableList<JournalEntry> journalEntries;

    private Path journalsFilePath;

    @FXML
    private void addJournal() {
        LocalDate date = datePicker.getValue();
        String text = journalTextArea.getText();
        if (date != null && !text.isEmpty()) {
            JournalEntry entry = new JournalEntry(date, text);
            journalEntries.add(entry);
            saveJournals();
            clearFields();
        }
    }

    @FXML
    private void editJournal() {
        JournalEntry selectedEntry = journalListView.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            datePicker.setValue(selectedEntry.getDate());
            journalTextArea.setText(selectedEntry.getText());
            journalEntries.remove(selectedEntry);
        }
    }

    @FXML
    private void deleteJournal() {
        JournalEntry selectedEntry = journalListView.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            journalEntries.remove(selectedEntry);
            saveJournals();
        }
    }

    private void saveJournals() {
        JSONArray jsonArray = new JSONArray();
        for (JournalEntry entry : journalEntries) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("date", entry.getDate().toString());
            jsonObject.put("text", entry.getText());
            jsonArray.put(jsonObject);
        }

        try (FileWriter file = new FileWriter(journalsFilePath.toFile())) {
            System.out.println("Saving to journals.json file at: " + journalsFilePath); // Log the file location
            System.out.println("Saving journals: " + jsonArray); // Log before saving
            file.write(jsonArray.toString());
        } catch (IOException e) {
            System.err.println("Error saving journals: " + e.getMessage());
        }
    }

    private void loadJournals() {
        try {
            String content = new String(Files.readAllBytes(journalsFilePath));
            System.out.println("Loaded journals content: " + content); // Log the loaded content
            if (content.isBlank()) {
                return; // or handle empty file as needed
            }
            JSONArray jsonArray = new JSONArray(content);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                LocalDate date = LocalDate.parse(jsonObject.getString("date"));
                String text = jsonObject.getString("text");
                journalEntries.add(new JournalEntry(date, text));
            }
            System.out.println("Journal entries loaded: " + journalEntries.size()); // Log the number of entries loaded
        } catch (IOException | JSONException e) {
            System.err.println("Error loading journals: " + e.getMessage());
        }
    }

    private void clearFields() {
        datePicker.setValue(null);
        journalTextArea.clear();
    }

    public static class JournalEntry {
        private LocalDate date;
        private String text;

        public JournalEntry(LocalDate date, String text) {
            this.date = date;
            this.text = text;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return date + ": " + text;
        }
    }

    // Journal tab controller ends here

    // Notices tab controller starts here

    @FXML
    public Tab noticesTab;

    @FXML
    private ListView<Hyperlink> noticeListView;

    @FXML
    private StackPane noticeContainer;

    @FXML
    private ProgressIndicator loadingIndicator;
    private ObservableList<Hyperlink> notices;



    private void fetchNoticesAsync() {
        Thread fetchThread = new Thread(() -> {
            try {
                Document doc = Jsoup.connect("https://www.uiu.ac.bd/notice/")
                        .timeout(15000)
                        .get();

                Elements noticeElements = doc.select("#notice-container .notice .details .title a");

                Platform.runLater(() -> {
                    notices.clear();
                    for (Element element : noticeElements) {
                        String title = element.text();
                        String link = element.attr("href");

                        Hyperlink hyperlink = new Hyperlink(title);
                        hyperlink.setWrapText(true);
                        hyperlink.setOnAction(e -> openUrlInBackground(link));

                        notices.add(hyperlink);
                    }

                    noticeContainer.getChildren().remove(loadingIndicator);

                    if (notices.isEmpty()) {
                        Hyperlink noNoticesLink = new Hyperlink("No notices available at this time");
                        noNoticesLink.setDisable(true);
                        notices.add(noNoticesLink);
                    }
                });

            } catch (IOException e) {
                Platform.runLater(() -> {
                    notices.clear();
                    Hyperlink errorLink = new Hyperlink("Error loading notices. Please check your internet connection.");
                    errorLink.setDisable(true);
                    notices.add(errorLink);
                    noticeContainer.getChildren().remove(loadingIndicator);
                });
            }
        });
        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    private void openUrlInBackground(String url) {
        CompletableFuture.runAsync(() -> {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Could not open the notice");
                    alert.setContentText("Failed to open the URL in browser. Please try again.");
                    alert.showAndWait();
                });
            }
        });
    }

    @FXML
    private void refreshNotices() {

        noticeContainer.getChildren().remove(loadingIndicator);

        notices.clear();
        fetchNoticesAsync();
    }

    // Notices tab controller ends here

    // Messages tab controller starts here

    @FXML
    private ListView<String> userListView;
    @FXML
    private ListView<String> messageListView;
    @FXML
    private TextArea messageInput;

    private WebSocketClient webSocketClient;
    private String selectedUser;
    private ObservableList<String> onlineUsers = FXCollections.observableArrayList();
    private Map<String, String> userIdToNicknameMap = new HashMap<>();
    private volatile boolean isShuttingDown = false;
    private MessagePollingService messagePollingService;

    private void initializeMessaging() {
        userListView.setItems(onlineUsers);
        loadAllUsers();

        // Initialize message polling service
        messagePollingService = new MessagePollingService(this);
        messagePollingService.start();

        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedUser = getNicknameToUserId(newValue);
                loadMessageHistory();
            }
        });

        // Add cleanup for polling service
        Platform.runLater(() -> {
            Stage stage = (Stage) messageTab.getTabPane().getScene().getWindow();
            stage.setOnCloseRequest(event -> cleanup());
        });
    }

    private void loadAllUsers() {
        List<User> users = User.getAllUsers();
        for (User user : users) {
            // Don't add current user to the list
            if (!user.getUserId().equals(UserSession.getInstance().getUserId())) {
                String nickname = user.getNickname();
                onlineUsers.add(nickname);
                userIdToNicknameMap.put(user.getUserId(), nickname);
            }
        }
    }

    private void updateUserStatus(JSONObject jsonMessage) {
        String userId = jsonMessage.getString("userId");
        boolean online = jsonMessage.getBoolean("online");
        String nickname = getUserNickname(userId);

        if (online && !onlineUsers.contains(nickname)) {
            onlineUsers.add(nickname);
            userIdToNicknameMap.put(userId, nickname);
        } else if (!online) {
            onlineUsers.remove(nickname);
            userIdToNicknameMap.remove(userId);
        }
    }

    private String getUserNickname(String userId) {
        String query = "SELECT Nickname FROM Users WHERE ID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("Nickname");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching nickname: " + e.getMessage());
        }
        return userId; // Fallback to userId if nickname can't be fetched
    }

    private String getNicknameToUserId(String nickname) {
        for (Map.Entry<String, String> entry : userIdToNicknameMap.entrySet()) {
            if (entry.getValue().equals(nickname)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void displayNewMessage(JSONObject jsonMessage) {
        String fromUserId = jsonMessage.getString("from");
        String content = jsonMessage.getString("content");
        String fromNickname = getUserNickname(fromUserId);

        Platform.runLater(() -> {
            if (fromUserId.equals(selectedUser)) {
                messageListView.getItems().add(fromNickname + ": " + content);
                messageListView.scrollTo(messageListView.getItems().size() - 1);
            }
        });
    }

    private void loadMessageHistory() {
        if (selectedUser == null) return;

        messageListView.getItems().clear();
        List<Message> messages = Message.getMessageHistory(
                UserSession.getInstance().getUserId(),
                selectedUser
        );

        for (Message message : messages) {
            String formattedMessage = formatMessage(message);
            messageListView.getItems().add(formattedMessage);
        }

        // Scroll to bottom of message list
        messageListView.scrollTo(messageListView.getItems().size() - 1);
    }


    private void connectWebSocket() {
        if (isShuttingDown) {
            return;
        }

        try {
            if (webSocketClient != null && !webSocketClient.isClosed()) {
                webSocketClient.close();
            }

            webSocketClient = new WebSocketClient(new URI("ws://localhost:8887")) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Connected to WebSocket server");

                    // Send initial presence message
                    JSONObject presenceMsg = new JSONObject();
                    presenceMsg.put("type", "presence");
                    presenceMsg.put("userId", UserSession.getInstance().getUserId());
                    presenceMsg.put("online", true);
                    send(presenceMsg.toString());
                }

                @Override
                public void onMessage(String message) {
                    handleWebSocketMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    if (!isShuttingDown) {
                        System.out.println("WebSocket connection closed. Attempting to reconnect...");
                        Platform.runLater(() -> {
                            onlineUsers.clear();
                            reconnectWebSocket();
                        });
                    }
                }

                @Override
                public void onError(Exception e) {
                    System.err.println("WebSocket error: " + e.getMessage());
                }
            };

            webSocketClient.connect();

        } catch (URISyntaxException e) {
            System.err.println("Invalid WebSocket URI: " + e.getMessage());
        }
    }

    private void reconnectWebSocket() {
        if (isShuttingDown) {
            return;
        }

        if (webSocketClient == null || webSocketClient.isClosed()) {
            System.out.println("Attempting to reconnect to WebSocket server...");
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    if (!isShuttingDown) {
                        Platform.runLater(() -> connectWebSocket());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    private void handleWebSocketMessage(String message) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String type = jsonMessage.getString("type");

            Platform.runLater(() -> {
                switch (type) {
                    case "presence":
                        updateUserStatus(jsonMessage);
                        break;
                    case "message":
                        displayNewMessage(jsonMessage);
                        break;
                }
            });
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
        }
    }

    @FXML
    private void sendMessage() {
        if (selectedUser == null || messageInput.getText().trim().isEmpty()) {
            return;
        }

        try {
            Message message = new Message(
                    UserSession.getInstance().getUserId(),
                    selectedUser,
                    messageInput.getText().trim()
            );

            // Save to database
            Message.saveMessage(message);

            // Send via WebSocket
            if (webSocketClient != null && webSocketClient.isOpen()) {
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("type", "message");
                jsonMessage.put("from", message.getSenderId());
                jsonMessage.put("to", message.getReceiverId());
                jsonMessage.put("content", message.getContent());
                webSocketClient.send(jsonMessage.toString());

                // Update UI
                messageListView.getItems().add(formatMessage(message));
                messageInput.clear();
            } else {
                System.err.println("WebSocket is not connected");
                reconnectWebSocket();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatMessage(Message message) {
        String senderNickname = getUserNickname(message.getSenderId());
        String timestamp = message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        return String.format("[%s] %s: %s", timestamp, senderNickname, message.getContent());
    }


    public void cleanup() {
        isShuttingDown = true;
        if (messagePollingService != null) {
            messagePollingService.cancel();
        }
        if (webSocketClient != null && !webSocketClient.isClosed()) {
            webSocketClient.close();
        }
    }

    // Messages tab controller ends here

    // File sharing tab controller starts here

    @FXML
    private TextField linkUrlField;

    @FXML
    private TextField linkNameField;

    @FXML
    private Button addLinkButton;

    @FXML
    private ListView<SharedFile> filesListView;

    @FXML
    private Label statusLabel;

    private final ObservableList<SharedFile> filesList = FXCollections.observableArrayList();

    private void setupListView() {
        filesListView.setItems(filesList);
        filesListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SharedFile file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create custom cell layout
                    HBox container = new HBox(10); // 10 pixels spacing
                    VBox detailsBox = new VBox(5);

                    Label nameLabel = new Label(file.getFileName());
                    Label uploaderLabel = new Label("Uploaded by: " + file.getUploaderId());
                    detailsBox.getChildren().addAll(nameLabel, uploaderLabel);

                    Button openButton = new Button("Open Link");
                    openButton.setOnAction(e -> file.openInBrowser());

                    container.getChildren().addAll(detailsBox, openButton);

                    // Add delete button only for uploader
                    if (file.getUploaderId().equals(UserSession.getInstance().getUserId())) {
                        Button deleteButton = new Button("Delete");
                        deleteButton.setOnAction(e -> handleDeleteFile(file));
                        container.getChildren().add(deleteButton);
                    }

                    HBox.setHgrow(detailsBox, Priority.ALWAYS);
                    setGraphic(container);
                }
            }
        });
    }

    private void setupAddLinkButton() {
        addLinkButton.setOnAction(e -> handleAddLink());
    }

    private void handleAddLink() {
        String url = linkUrlField.getText().trim();
        String name = linkNameField.getText().trim();

        if (url.isEmpty() || name.isEmpty()) {
            showStatus("Please enter both link name and URL", true);
            return;
        }

        if (!isValidUrl(url)) {
            showStatus("Please enter a valid URL", true);
            return;
        }

        SharedFile newFile = new SharedFile(
                UUID.randomUUID().toString(),
                name,
                url,
                UserSession.getInstance().getUserId(),
                LocalDateTime.now()
        );

        if (newFile.uploadFile()) {
            filesList.add(newFile);
            linkUrlField.clear();
            linkNameField.clear();
            showStatus("Link added successfully!", false);
        } else {
            showStatus("Failed to add link. Please try again.", true);
        }
    }

    private void handleDeleteFile(SharedFile file) {
        if (file.deleteFile(UserSession.getInstance().getUserId())) {
            filesList.remove(file);
            showStatus("Link deleted successfully!", false);
        } else {
            showStatus("Failed to delete link", true);
        }
    }

    private void loadFiles() {
        filesList.clear();
        filesList.addAll(SharedFile.getAllFiles());
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        statusLabel.setVisible(true);
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // File sharing tab controller ends here
}