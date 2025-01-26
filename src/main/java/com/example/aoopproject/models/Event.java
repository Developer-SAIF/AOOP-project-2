package com.example.aoopproject.models;

import java.time.LocalDate;

public class Event {
    private String description;
    private LocalDate date;

    public Event(String description, LocalDate date) {
        this.description = description;
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }
}
