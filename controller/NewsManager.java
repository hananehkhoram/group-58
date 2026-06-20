package controller;

import model.News.News;

import java.util.ArrayList;
import java.util.List;

public class NewsManager {
    private static List<News> allNews = new ArrayList<>();

    public static List<News> getAllNews() {
        return allNews;
    }

    public static void addNews(String title, String content) {
        int nextId = allNews.size() + 1;
        allNews.add(new News(nextId, title, content));
    }
}
