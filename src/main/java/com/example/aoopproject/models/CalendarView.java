package com.example.aoopproject.models;

import com.example.aoopproject.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class CalendarView extends VBox {

    private YearMonth currentYearMonth;
    private List<Button> dayButtons;
    private Map<LocalDate, List<Event>> events;
    private LocalDate selectedDate;
    private ObservableList<String> examItems;
    private ListView<String> scheduledExamsListView;

    public CalendarView() {
        currentYearMonth = YearMonth.now();
        dayButtons = new ArrayList<>();
        events = new HashMap<>();
        examItems = FXCollections.observableArrayList();

        // Load events from MySQL
        loadEvents();

        // Create the calendar layout
        GridPane calendarGrid = createCalendarGrid();
        updateCalendar();

        // Add title and navigation buttons
        HBox navigationBox = createNavigationBox();
        getChildren().addAll(navigationBox, calendarGrid);

        // Make sure CalendarView grows with the window
        setVgrow(calendarGrid, Priority.ALWAYS);
        setVgrow(this, Priority.ALWAYS);

        // Scheduled Exams ListView
        scheduledExamsListView = new ListView<>(examItems);
        getChildren().add(scheduledExamsListView);

        // Load scheduled exams initially
        loadScheduledExams();
    }

    private GridPane createCalendarGrid() {
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
                Button dayButton = createDayButton();
                GridPane.setConstraints(dayButton, j, i + 1);
                calendarGrid.getChildren().add(dayButton);
                dayButtons.add(dayButton);
            }
        }
        return calendarGrid;
    }

    private Button createDayButton() {
        Button dayButton = new Button();
        dayButton.setPrefSize(40, 40);

        dayButton.setOnAction(e -> {
            if (!dayButton.getText().isEmpty()) {
                int day = Integer.parseInt(dayButton.getText());
                selectedDate = currentYearMonth.atDay(day);
                showEventDialog(selectedDate);
            }
        });
        return dayButton;
    }

    private HBox createNavigationBox() {
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
        HBox.setHgrow(prevButton, Priority.ALWAYS); // Allow buttons to grow
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        HBox.setHgrow(nextButton, Priority.ALWAYS);

        return navigationBox;
    }

    private void updateCalendar() {
        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        LocalDate firstDayOfCalendar = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate lastDayOfCalendar = currentYearMonth.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        dayButtons.forEach(button -> {
            button.setText("");
            button.setDisable(true);
        });

        final LocalDate[] dateIterator = {firstDayOfCalendar};
        dayButtons.forEach(button -> {
            if (!dateIterator[0].isAfter(lastDayOfCalendar)) {
                button.setText(String.valueOf(dateIterator[0].getDayOfMonth()));
                button.setDisable(false);
                button.setStyle("");
                if (events.containsKey(dateIterator[0])) {
                    button.setStyle("-fx-background-color: yellow;");
                }
                dateIterator[0] = dateIterator[0].plusDays(1);
            }
        });
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

        List<Event> eventList = events.getOrDefault(date, new ArrayList<>());
        for (Event event : eventList) {
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
        Event newEvent = new Event(description, date);
        events.get(date).add(newEvent);
        saveEventToDatabase(newEvent);
    }

    private void saveEventToDatabase(Event event) {
        try (Connection connection = DatabaseConnection.getConnection();
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
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT event_date, description FROM events");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                LocalDate date = resultSet.getDate("event_date").toLocalDate();
                String description = resultSet.getString("description");
                events.computeIfAbsent(date, k -> new ArrayList<>()).add(new Event(description, date));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateCalendar();
    }

    private void loadScheduledExams() {
        examItems.clear();
        String query = "SELECT examDate, subjectName FROM examschedules " +
                "JOIN subjects ON examschedules.subjectID = subjects.subjectID";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                LocalDateTime examDate = resultSet.getTimestamp("examDate").toLocalDateTime();
                String subjectName = resultSet.getString("subjectName");
                examItems.add(examDate + ": " + subjectName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        scheduledExamsListView.setItems(examItems);
    }
}
