package com.example.aoopproject.controllers.student;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;


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
}