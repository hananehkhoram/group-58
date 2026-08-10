package com.workshop.model.News;

import java.time.LocalDate;

public class News {
    private final int id;
    private final String title;
    private final String content;
    private final LocalDate date;

    public News(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = LocalDate.now();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getDate() {
        return date;
    }
}
