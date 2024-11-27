package com.example.lab4;

import com.example.Model.TaskResult;
import com.example.Service.WebScraperService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Controller {
    @FXML
    private TextField urlField;
    @FXML
    private TableView<TaskResult> resultTable;
    @FXML
    private TableColumn<TaskResult, String> urlColumn;
    @FXML
    private TableColumn<TaskResult, String> statusColumn;
    @FXML
    private TableColumn<TaskResult, Long> durationColumn;
    @FXML
    private TableColumn<TaskResult, Integer> dataSizeColumn;
    @FXML
    private VBox threadPanelContainer;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private Button startButton;

    private final ObservableList<TaskResult> results = FXCollections.observableArrayList();
    private final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();
    private final WebScraperService scraperService = new WebScraperService();

    private long totalExecutionTime = 0;

    @FXML
    public void initialize() {
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        dataSizeColumn.setCellValueFactory(new PropertyValueFactory<>("dataSize"));
        resultTable.setItems(results);
    }

    @FXML
    public void addUrl() {
        String url = urlField.getText().trim();
        if (!url.isEmpty()) {
            urlQueue.offer(url);
            addThreadPanel("Waiting: " + url); // Додаємо панель для нового URL
            urlField.clear();
        }
    }

    @FXML
    public void startScraping() {
        new Thread(() -> {
            long startTime = System.currentTimeMillis(); // Початок обчислення часу

            // Запускаємо обробку URL
            scraperService.scrapeUrls(urlQueue, this::updateResults, this::updateThreadPanel);

            long endTime = System.currentTimeMillis(); // Кінець обчислення часу
            long executionTime = endTime - startTime; // Час виконання цього запуску
            totalExecutionTime += executionTime; // Додаємо до загального часу

            // Оновлюємо мітку з часом у головному потоці
            Platform.runLater(() -> totalTimeLabel.setText("Total Execution Time: " + totalExecutionTime + " ms"));
        }).start();
    }

    @FXML
    public void stopService() {
        scraperService.shutdownService();
        Platform.runLater(() -> startButton.setDisable(true));
    }

    private void updateResults(TaskResult result) {
        Platform.runLater(() -> {
            results.add(result);
            System.out.println("Result added: " + result.getUrl());
        });
    }

    private void updateThreadPanel(String threadName, String status) {
        long threadId = Thread.currentThread().getId(); // Отримуємо ID потоку
        Platform.runLater(() -> {
            Label label = new Label(threadName + " [ID: " + threadId + "]: " + status);
            threadPanelContainer.getChildren().add(label);
        });
    }

    private void addThreadPanel(String status) {
        Platform.runLater(() -> {
            Label label = new Label(status);
            threadPanelContainer.getChildren().add(label);
        });
    }
}

