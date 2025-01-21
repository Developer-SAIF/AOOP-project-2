package com.example.aoopproject.controllers.student;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javafx.scene.control.TextArea;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class StudentController {

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
    public void initialize() {

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

        // Initialize the notices list
        notices = FXCollections.observableArrayList();
        noticeListView.setItems(notices);

        // Create and add loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        // Initial load of notices
        refreshNotices();

        // Notice part ends here
    }

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
            System.out.println("Saving journals: " + jsonArray.toString()); // Log before saving
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
        // Remove loading indicator if it exists
        noticeContainer.getChildren().remove(loadingIndicator);
        // Clear notices and start fetch
        notices.clear();
        fetchNoticesAsync();
    }

    // Notices tab controller ends here
}