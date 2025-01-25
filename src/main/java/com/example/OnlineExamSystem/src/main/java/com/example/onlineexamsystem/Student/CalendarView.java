package com.example.OnlineExamSystem.src.main.java.com.example.onlineexamsystem.Student;

import com.example.onlineexamsystem.Instructor.Database.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import java.sql.*;
import java.sql.Date;
import java.time.*;
import java.util.*;

public class CalendarView extends VBox {
    private YearMonth currentYearMonth;
    private GridPane calendar;
    private Text calendarTitle;
    private Map<LocalDate, List<String>> eventMap;

    public CalendarView() {
        this.currentYearMonth = YearMonth.now();
        this.eventMap = new HashMap<>();
        setupCalendarView();
        loadEvents();
    }

    private void setupCalendarView() {
        // Calendar header with navigation
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER);

        Button previousMonth = new Button("<<");
        previousMonth.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        Button nextMonth = new Button(">>");
        nextMonth.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        calendarTitle = new Text();

        header.getChildren().addAll(previousMonth, calendarTitle, nextMonth);

        // Calendar grid
        calendar = new GridPane();
        calendar.setPadding(new Insets(10));
        calendar.setHgap(10);
        calendar.setVgap(10);

        // Day labels
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setStyle("-fx-font-weight: bold");
            calendar.add(dayLabel, i, 0);
        }

        this.getChildren().addAll(header, calendar);
        this.setSpacing(10);
        this.setPadding(new Insets(10));

        updateCalendar();
    }

    private void updateCalendar() {
        calendarTitle.setText(currentYearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")));

        // Clear existing calendar cells
        calendar.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < currentYearMonth.lengthOfMonth(); i++) {
            LocalDate date = firstOfMonth.plusDays(i);
            VBox dayCell = createDayCell(date);

            calendar.add(dayCell, (dayOfWeek + i) % 7, ((dayOfWeek + i) / 7) + 1);
        }
    }

    private VBox createDayCell(LocalDate date) {
        VBox dayCell = new VBox(5);
        dayCell.setPadding(new Insets(5));
        dayCell.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0.5px;");

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayCell.getChildren().add(dayLabel);

        // Add event indicators
        List<String> events = eventMap.get(date);
        if (events != null) {
            for (String event : events) {
                Label eventLabel = new Label(event);
                eventLabel.setStyle("-fx-background-color: #e6e6e6; -fx-padding: 2px;");
                eventLabel.setWrapText(true);
                dayCell.getChildren().add(eventLabel);
            }
        }

        // Highlight current day
        if (date.equals(LocalDate.now())) {
            dayCell.setStyle(dayCell.getStyle() + "-fx-background-color: #f0f0f0;");
        }

        // Add event handling
        dayCell.setOnMouseClicked(e -> showDayEvents(date));

        return dayCell;
    }

    private void loadEvents() {
        eventMap.clear();
        String query = "SELECT event_date, description FROM events";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDate eventDate = rs.getDate("event_date").toLocalDate();
                String description = rs.getString("description");

                eventMap.computeIfAbsent(eventDate, k -> new ArrayList<>()).add(description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading events: " + e.getMessage());
        }

        updateCalendar();
    }

    private void showDayEvents(LocalDate date) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Events for " + date);
        dialog.setHeaderText(null);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Show existing events
        List<String> events = eventMap.getOrDefault(date, new ArrayList<>());
        if (!events.isEmpty()) {
            Label existingEvents = new Label("Existing Events:");
            existingEvents.setStyle("-fx-font-weight: bold");
            content.getChildren().add(existingEvents);

            for (String event : events) {
                content.getChildren().add(new Label(event));
            }
        }

        // Add new event section
        TextField newEventField = new TextField();
        newEventField.setPromptText("New event description");
        Button addButton = new Button("Add Event");
        addButton.setOnAction(e -> {
            String description = newEventField.getText().trim();
            if (!description.isEmpty()) {
                addEvent(date, description);
                dialog.close();
            }
        });

        content.getChildren().addAll(new Separator(), newEventField, addButton);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void addEvent(LocalDate date, String description) {
        String query = "INSERT INTO events (event_date, description) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(date));
            stmt.setString(2, description);
            stmt.executeUpdate();

            loadEvents();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error adding event: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}