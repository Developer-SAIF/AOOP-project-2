package com.example.aoopproject.controllers.student;

import com.example.aoopproject.database.DatabaseConnection;
import com.example.aoopproject.models.*;
import com.example.aoopproject.services.MessagePollingService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javafx.scene.control.TextArea;
import java.awt.Desktop;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
    public Tab aiTab;
    public AnchorPane participateExamAnchorPane;


    @FXML
    private DatePicker examCalendar;
    @FXML
    private ListView<String> scheduledExamsListView;

    @FXML
    private CalendarView calendarView;


    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize all exam-related components first
            initializeExamComponents();

            // Journal initialization
            initializeJournalComponents();

            // Notice initialization
            initializeNoticeComponents();

            // Message initialization
            initializeMessaging();

            // File sharing initialization
            initializeFileSharingComponents();

            // AI helper initialization
            initializeAiHelper();

//            //initialize Calendar View
//            initializeCalenderView();

            //initialize participate tab;
            loadExamStartToTab();

        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeExamComponents() {
        // Initialize exam tab components with null checks
        if (mainTabPane != null) {
            mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        }

        // Initialize welcome labels
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + UserSession.getInstance().getUserId());
        }
        if (nameLabel != null) {
            nameLabel.setText(getUserNickname());
        }
        if (gradeLabel != null) {
            gradeLabel.setText("Current Grade: " + calculateCurrentGrade());
        }

        // Initialize performance chart
        if (performanceChart != null) {
            setupPerformanceChart();
        }

        // Initialize list views with null checks
        if (availableExamsListView != null) {
            availableExamsListView.setItems(getAvailableExams());
            setupExamListViewCellFactory();
        }

        if (previousResultsListView != null) {
            previousResultsListView.setItems(getPreviousResults());
        }

        if (achievementsListView != null) {
            achievementsListView.setItems(getAchievements());
        }

        // Setup countdown timer
        setupExamCountdown();

        // Setup motivational quote
        setupMotivationalQuote();
    }

    private void setupExamListViewCellFactory() {
        availableExamsListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10);
                    Label examLabel = new Label(item);
                    Button registerButton = new Button("Register");

                    ExamRegistrationHandler handler = new ExamRegistrationHandler();
                    registerButton.setOnAction(e -> handler.handleExamRegistration(item));

                    container.getChildren().addAll(examLabel, registerButton);
                    setGraphic(container);
                }
            }
        });
}

    private void setupExamCountdown() {
        if (countdownLabel != null) {
            Timeline examCountdown = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateNextExamLabel())
            );
            examCountdown.setCycleCount(Timeline.INDEFINITE);
            examCountdown.play();
        }
    }

    private void setupMotivationalQuote() {
        if (quoteLabel != null) {
            String[] quotes = {
                "Study hard what interests you the most in the most undisciplined, irreverent and original manner possible.",
                "The expert in anything was once a beginner.",
                "Success is not final, failure is not fatal: it is the courage to continue that counts."
            };
            Random random = new Random();
            quoteLabel.setText(quotes[random.nextInt(quotes.length)]);
        }
    }

    private void initializeJournalComponents() {
        journalEntries = FXCollections.observableArrayList();
        if (journalListView != null) {
            journalListView.setItems(journalEntries);
        }

        try {
            String userHome = System.getProperty("user.home");
            journalsFilePath = Paths.get(userHome, ".aoopproject", "journals.json");

            if (!Files.exists(journalsFilePath.getParent())) {
                Files.createDirectories(journalsFilePath.getParent());
            }

            if (!Files.exists(journalsFilePath)) {
                Files.createFile(journalsFilePath);
                Files.writeString(journalsFilePath, "[]");
            }
            loadJournals();
        } catch (IOException e) {
            System.err.println("Error creating or loading journals: " + e.getMessage());
        }
    }

    private void initializeNoticeComponents() {
        notices = FXCollections.observableArrayList();
        if (noticeListView != null) {
            noticeListView.setItems(notices);
        }

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        refreshNotices();
    }

    private void initializeFileSharingComponents() {
        setupListView();
        loadFiles();
        setupAddLinkButton();
    }

    private void initializeAiHelper() {
        if (outputArea != null) {
            outputArea.setWrapText(true);
            outputArea.setEditable(false);
        }
    }

    public void initializeCalenderView() {
        calendarView = new CalendarView();
        //calendarContainer.getChildren().add(calendarView);

    }

    private String calculateCurrentGrade() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT AVG(score) as average_score FROM responses WHERE userID = ?")) {

            stmt.setString(1, UserSession.getInstance().getUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double avgScore = rs.getDouble("average_score");
                return String.format("%.1f%%", avgScore);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating grade: " + e.getMessage());
        }
        return "N/A";
    }

    private String getUserNickname() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT Nickname FROM Users WHERE ID = ?")) {

            stmt.setString(1, UserSession.getInstance().getUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Nickname");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user nickname: " + e.getMessage());
        }
        return "Student";
    }


    public class ExamRegistrationHandler {

        private boolean isAlreadyRegistered(String examInfo) throws SQLException {
            String query = "SELECT COUNT(*) FROM exam_registrations WHERE student_id = ? AND exam_info = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                // Convert String ID to Integer if needed, or use setString if keeping as string
                stmt.setString(1, UserSession.getInstance().getUserId());
                stmt.setString(2, examInfo);

                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getInt(1) > 0;
            }
        }

        private void registerStudentForExam(String examInfo) throws SQLException {
            String query = "INSERT INTO exam_registrations (student_id, exam_info, registration_date) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                // Convert String ID to Integer if needed, or use setString if keeping as string
                stmt.setString(1, UserSession.getInstance().getUserId());
                stmt.setString(2, examInfo);
                stmt.setTimestamp(3, Timestamp.from(Instant.now()));

                stmt.executeUpdate();
            }
        }

        public void handleExamRegistration(String examInfo) {
            try {
                if (isAlreadyRegistered(examInfo)) {
                    showAlert(
                            Alert.AlertType.WARNING,
                            "Already Registered",
                            "You are already registered for this exam: " + examInfo
                    );
                    return;
                }

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Exam Registration");
                confirmAlert.setHeaderText("Register for Exam");
                confirmAlert.setContentText("Do you want to register for: " + examInfo);

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    registerStudentForExam(examInfo);
                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Registration Successful",
                            "You have been successfully registered for: " + examInfo
                    );
                }
            } catch (SQLException e) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Registration Error",
                        "An error occurred while registering for the exam: " + e.getMessage()
                );
                e.printStackTrace();
            }
        }

        private void showAlert(Alert.AlertType alertType, String title, String content) {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }

    // Journal tab controller starts here

    @FXML
    public Tab journalTab;

    @FXML
    private MFXDatePicker datePicker;

    @FXML
    private MFXTextField journalTextArea;

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
    private MFXButton addLinkButton;

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

    // Exam Tab controller starts here

    @FXML
    public Tab examTab;
    @FXML private TabPane mainTabPane;
    @FXML private Label welcomeLabel;
    @FXML private Label nameLabel;
    @FXML private Label gradeLabel;
    @FXML private Label countdownLabel;
    @FXML private Label quoteLabel;
    @FXML private LineChart<Number, Number> performanceChart;
    @FXML private ListView<String> achievementsListView;
    @FXML private ListView<String> announcementsListView;
    @FXML private ListView<String> availableExamsListView;
    @FXML private ListView<String> previousResultsListView;



    private ObservableList<String> getPreviousResults() {
        ObservableList<String> results = FXCollections.observableArrayList();
        String query = "SELECT E.examDate, S.subjectName, R.score FROM responses R " +
                "JOIN exams E ON R.examID = E.examID " +
                "JOIN subjects S ON E.subjectID = S.subjectID " +
                "WHERE R.userID = ? ORDER BY E.examDate DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Replace 1 with the actual student userID
            preparedStatement.setInt(1, 1);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                LocalDateTime examDate = resultSet.getTimestamp("examDate").toLocalDateTime();
                String subjectName = resultSet.getString("subjectName");
                int score = resultSet.getInt("score");

                results.add(String.format("Date: %s, Subject: %s, Score: %d", examDate, subjectName, score));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    private void setupPerformanceChart() {
        NumberAxis xAxis = (NumberAxis) performanceChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) performanceChart.getYAxis();
        xAxis.setLabel("Exam Number");
        yAxis.setLabel("Score");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Scores");
        series.setData(getPerformanceData());

        performanceChart.getData().add(series);
    }

    private void updateNextExamLabel() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExamDate = getNextExamDateFromDatabase();
        if (nextExamDate != null) {
            long daysRemaining = ChronoUnit.DAYS.between(now, nextExamDate);
            countdownLabel.setText("Next exam in " + daysRemaining + " days");
        } else {
            countdownLabel.setText("No upcoming exams");
        }
    }

    private LocalDateTime getNextExamDateFromDatabase() {
        String query = """
            SELECT examDate 
            FROM examschedules 
            WHERE examDate > NOW() 
            ORDER BY examDate ASC 
            LIMIT 1""";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Convert SQL timestamp to LocalDateTime
                return rs.getTimestamp("examDate").toLocalDateTime();
            }

        } catch (SQLException e) {
            System.err.println("Error fetching next exam date: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private ObservableList<XYChart.Data<Number, Number>> getPerformanceData() {
        ObservableList<XYChart.Data<Number, Number>> performanceData = FXCollections.observableArrayList();

        String query = """
            SELECT e.examID, 
                   SUM(r.score) as totalScore,
                   COUNT(DISTINCT q.questionID) as totalQuestions
            FROM responses r
            JOIN exams e ON r.examID = e.examID
            JOIN examquestions q ON r.questionID = q.questionID
            WHERE r.userID = ?
            GROUP BY e.examID
            ORDER BY e.examID""";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Get current user's ID from UserSession
            int currentUserId = Integer.parseInt(UserSession.getInstance().getUserId());
            stmt.setInt(1, currentUserId);

            ResultSet rs = stmt.executeQuery();

            int examCount = 1; // X-axis counter
            while (rs.next()) {
                int totalScore = rs.getInt("totalScore");
                int totalQuestions = rs.getInt("totalQuestions");

                // Calculate percentage score
                double percentageScore = (totalQuestions > 0)
                        ? ((double) totalScore / totalQuestions) * 100
                        : 0.0;

                // Add data point (exam number, percentage score)
                performanceData.add(new XYChart.Data<>(examCount++, percentageScore));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching performance data: " + e.getMessage());
            e.printStackTrace();
        }

        return performanceData;
    }

    private ObservableList<String> getAvailableExams() {
        ObservableList<String> availableExams = FXCollections.observableArrayList();

        String query = """
        SELECT e.examID, e.examDate, s.subjectName
        FROM examschedules e
        JOIN subjects s ON e.subjectID = s.subjectID
        WHERE e.examDate > NOW()
        AND e.examID NOT IN (
            -- Exclude exams already taken by the student
            SELECT examID 
            FROM responses 
            WHERE userID = ?
        )
        ORDER BY e.examDate ASC
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Get current student's ID from UserSession
            stmt.setString(1, UserSession.getInstance().getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String examInfo = String.format("%s - %s",
                        rs.getString("subjectName"),
                        rs.getTimestamp("examDate").toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                );
                availableExams.add(examInfo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally add an error message to the list
            availableExams.add("Error loading available exams");
        }

        return availableExams;
    }

    private ObservableList<String> getAchievements() {
        // Implementation remains the same
        return FXCollections.observableArrayList();
    }

    // Method to load the ExamStart FXML into the Participate Exam tab
    public void loadExamStartToTab() {
        try {
            // Get the first upcoming exam ID (or you can modify this logic based on your needs)
            int examId = ExamManager.getInstance().getUpcomingExams()
                    .stream()
                    .findFirst()
                    .map(ExamManager.ExamInfo::getExamId)
                    .orElse(-1);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StartingExam.fxml"));
            AnchorPane examStartPane = loader.load();

            ExamStartController controller = loader.getController();
            controller.initializeExam(examId);

            participateExamAnchorPane.getChildren().setAll(examStartPane);

            // Set anchor constraints
            AnchorPane.setTopAnchor(examStartPane, 0.0);
            AnchorPane.setBottomAnchor(examStartPane, 0.0);
            AnchorPane.setLeftAnchor(examStartPane, 0.0);
            AnchorPane.setRightAnchor(examStartPane, 0.0);

            if (examId == -1) {
                // Handle case when no exam is available
                System.out.println("No upcoming exams found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading exam start view: " + e.getMessage());
        }
    }

    // ExamController ends here


    // Ai helper tab controller starts here

    private static final String API_URL = "https://api.scaleway.ai/36ff389f-76e7-4451-b35d-6748110f4dec/v1/chat/completions";
    private static final String API_KEY = "8d10c4c7-b397-47b6-8fe2-0533b1c2725c";

    @FXML
    private MFXTextField inputField;

    @FXML
    private TextArea outputArea;

    @FXML
    private void getAnswer() {
        String question = inputField.getText().trim();
        if (question.isEmpty()) {
            outputArea.setText("Please enter a question first.");
            return;
        }

        outputArea.setText("Thinking...");
        inputField.setDisable(true);

        CompletableFuture.runAsync(() -> {
            try {
                String response = sendRequest(question);
                Platform.runLater(() -> {
                    outputArea.setText(parseResponse(response));
                    inputField.setDisable(false);
                    inputField.clear();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    String errorMessage = "Error: " + e.getMessage();
                    if (e.getMessage().contains("403")) {
                        errorMessage += "\nAPI authentication failed. Please check your API key.";
                    }
                    outputArea.setText(errorMessage);
                    inputField.setDisable(false);
                });
            }
        });
    }

    private String sendRequest(String question) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);

        String jsonInput = String.format("""
            {
                "model": "llama-3.3-70b-instruct",
                "messages": [
                    {"role": "system", "content": "You are a study helper assistant"},
                    {"role": "user", "content": "%s"}
                ],
                "max_tokens": 1088,
                "temperature": 0.7,
                "top_p": 0.7,
                "presence_penalty": 0,
                "stream": false
            }""", question.replace("\"", "\\\""));

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (conn.getResponseCode() != 200) {
            throw new IOException("Server returned HTTP response code: " +
                    conn.getResponseCode() + " " + conn.getResponseMessage() +
                    " for URL: " + API_URL);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return response.toString();
    }

    private String parseResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    // Ai helper tab controller ends here
}