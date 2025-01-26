package com.example.aoopproject.models;

import com.example.aoopproject.database.LocalDatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarView extends VBox {

    private YearMonth currentYearMonth;
    private ArrayList<Button> dayButtons;
    private Map<LocalDate, List<com.example.onlineexamsystem.Student.Event>> events;
    private LocalDate selectedDate;

    public CalendarView() {
        currentYearMonth = YearMonth.now();
        dayButtons = new ArrayList<>();
        events = new HashMap<>();

        // Load events from MySQL
        loadEvents();

        // Create the calendar layout
        GridPane calendarGrid = new GridPane();
        calendarGrid.setPadding(new Insets(10));
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);

        // Add day names
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < dayNames.length; i++) {
            Text dayName = new Text(dayNames[i]);
            GridPane.setConstraints(dayName, i, 0);
            calendarGrid.getChildren().add(dayName);
        }

        // Add day buttons
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                Button dayButton = new Button();
                dayButton.setPrefSize(40, 40);
                GridPane.setConstraints(dayButton, j, i + 1);
                calendarGrid.getChildren().add(dayButton);
                dayButtons.add(dayButton);

                int finalI = i;
                int finalJ = j;
                dayButton.setOnAction(e -> {
                    if (!dayButton.getText().isEmpty()) {
                        int day = Integer.parseInt(dayButton.getText());
                        selectedDate = currentYearMonth.atDay(day);
                        showEventDialog(selectedDate);
                    }
                });
            }
        }

        // Update calendar
        updateCalendar();

        // Add title and calendar grid to layout
        Label titleLabel = new Label();
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: darkblue;");
        updateTitle(titleLabel);

        Button prevButton = new Button("<");
        prevButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
            updateTitle(titleLabel);
        });

        Button nextButton = new Button(">");
        nextButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
            updateTitle(titleLabel);
        });

        HBox navigationBox = new HBox(10, prevButton, titleLabel, nextButton);
        navigationBox.setPadding(new Insets(10, 0, 10, 0));

        getChildren().addAll(navigationBox, calendarGrid);
    }

    private void updateCalendar() {
        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // Adjust Sunday to 0, Monday to 1, etc.
        int daysInMonth = currentYearMonth.lengthOfMonth();

        for (Button dayButton : dayButtons) {
            dayButton.setText("");
            dayButton.setDisable(true);
        }

        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = currentYearMonth.atDay(i);
            int buttonIndex = dayOfWeek + i - 1;
            if (buttonIndex < dayButtons.size()) {  // Ensure index is within bounds
                Button dayButton = dayButtons.get(buttonIndex);
                dayButton.setText(String.valueOf(i));
                dayButton.setDisable(false);
                dayButton.setStyle(""); // Reset style
                if (events.containsKey(date)) {
                    dayButton.setStyle("-fx-background-color: yellow;");
                }
            }
        }
    }

    private void updateTitle(Label titleLabel) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        titleLabel.setText(currentYearMonth.format(formatter));
    }

    private void showEventDialog(LocalDate date) {
        VBox eventLayout = new VBox(10);
        eventLayout.setPadding(new Insets(10));

        Label dateLabel = new Label("Date: " + date);

        // List existing events
        ListView<String> eventListView = new ListView<>();
        ObservableList<String> eventItems = FXCollections.observableArrayList();

        List<com.example.onlineexamsystem.Student.Event> eventList = events.getOrDefault(date, new ArrayList<>());
        for (com.example.onlineexamsystem.Student.Event event : eventList) {
            eventItems.add(event.getDescription());
        }
        eventListView.setItems(eventItems);

        // Event input field and add button
        TextField eventInput = new TextField();
        eventInput.setPromptText("Enter event description");
        Button addEventButton = new Button("Add Event");
        addEventButton.setOnAction(e -> {
            if (!eventInput.getText().isEmpty()) {
                addEvent(date, eventInput.getText());
                eventItems.add(eventInput.getText());
                eventInput.clear();
                updateCalendar();
            }
        });

        eventLayout.getChildren().addAll(dateLabel, eventListView, eventInput, addEventButton);

        Stage eventStage = new Stage();
        eventStage.setScene(new Scene(eventLayout));
        eventStage.setTitle("Events on " + date);
        eventStage.show();
    }

    private void addEvent(LocalDate date, String description) {
        if (!events.containsKey(date)) {
            events.put(date, new ArrayList<>());
        }
        com.example.onlineexamsystem.Student.Event newEvent = new com.example.onlineexamsystem.Student.Event(description, date);
        events.get(date).add(newEvent);
        saveEventToDatabase(newEvent);
    }

    private void saveEventToDatabase(com.example.onlineexamsystem.Student.Event event) {
        try (Connection connection = LocalDatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO events (event_date, description) VALUES (?, ?)")) {

            preparedStatement.setDate(1, Date.valueOf(event.getDate()));
            preparedStatement.setString(2, event.getDescription());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadEvents() {
        try (Connection connection = LocalDatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT event_date, description FROM events");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                LocalDate date = resultSet.getDate("event_date").toLocalDate();
                String description = resultSet.getString("description");
                events.computeIfAbsent(date, k -> new ArrayList<>()).add(new com.example.onlineexamsystem.Student.Event(description, date));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateCalendar();
    }
}
