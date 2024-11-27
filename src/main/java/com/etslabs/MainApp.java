package com.etslabs;

import com.etslabs.Controllers.MainController;

import javafx.application.Application;
import javafx.stage.Stage;
public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainController mainController = new MainController(primaryStage);
        mainController.init();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
