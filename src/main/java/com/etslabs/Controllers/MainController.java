package com.etslabs.Controllers;

import java.io.File;

import com.etslabs.Models.ImageModel;
import com.etslabs.Models.Perspective;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {
    private final Stage primaryStage;
    private final Scene scene;
    private final ImageModel imageModel = new ImageModel();
    private final Perspective perspective = new Perspective(imageModel);
    private final ObjectProperty<javafx.scene.image.Image> clipboardImage = new SimpleObjectProperty<>();
    private final DoubleProperty clipboardScaleX = new SimpleDoubleProperty(1.0);
    private final DoubleProperty clipboardScaleY = new SimpleDoubleProperty(1.0);
    private final DoubleProperty clipboardTranslateX = new SimpleDoubleProperty(0.0);
    private final DoubleProperty clipboardTranslateY = new SimpleDoubleProperty(0.0);

    // Controllers
    private PerspectiveController perspectiveController;
    private ThumbnailController thumbnailController1;
    private ThumbnailController thumbnailController2;
    private ThumbnailController activeThumbnailController;

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.scene = createScene();
    }

    public void init() {
        primaryStage.setScene(scene);
        primaryStage.setTitle("Image Viewer with Undo/Redo");
        primaryStage.show();
    }

    private Scene createScene() {
        // Create menu bar
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem loadImage = new MenuItem("Load Image");
        MenuItem saveState = new MenuItem("Save State");
        MenuItem loadState = new MenuItem("Load State");
        MenuItem removeImage = new MenuItem("Remove Image");
        fileMenu.getItems().addAll(loadImage, saveState, loadState, removeImage);

        Menu editMenu = new Menu("Edit");
        MenuItem undo = new MenuItem("Undo");
        MenuItem redo = new MenuItem("Redo");
        editMenu.getItems().addAll(undo, redo);

        menuBar.getMenus().addAll(fileMenu, editMenu);

        // Create perspective controller and view
        perspectiveController = new PerspectiveController(perspective);

        VBox perspectivePanel = new VBox(perspectiveController.getView());
        VBox.setVgrow(perspectivePanel, Priority.ALWAYS);
        perspectivePanel.setStyle("-fx-border-width: 2; -fx-border-color: gray;");

        // Create thumbnail controllers
        thumbnailController1 = createThumbnailController(true);
        thumbnailController2 = createThumbnailController(false);

        VBox thumbnail1Container = thumbnailController1.getThumbnailContainer(true);
        VBox thumbnail2Container = thumbnailController2.getThumbnailContainer(false);

        VBox thumbnailContainer = new VBox(thumbnail1Container, new Separator(), thumbnail2Container);
        VBox.setVgrow(thumbnail1Container, Priority.ALWAYS);
        VBox.setVgrow(thumbnail2Container, Priority.ALWAYS);
        thumbnailContainer.setStyle("-fx-border-width: 2; -fx-border-color: gray;");

        SplitPane mainPane = new SplitPane();
        mainPane.setOrientation(Orientation.HORIZONTAL);
        mainPane.getItems().addAll(perspectivePanel, thumbnailContainer);
        mainPane.setDividerPositions(0.2);

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(mainPane);

        // Set up menu actions
        setupMenuActions(loadImage, saveState, loadState, removeImage, undo, redo);

        return new Scene(root, 1000, 600);
    }

    private ThumbnailController createThumbnailController(boolean isFirst) {
        ThumbnailController thumbnailController = new ThumbnailController(
            imageModel,
            clipboardImage,
            clipboardScaleX,
            clipboardScaleY,
            clipboardTranslateX,
            clipboardTranslateY
        );

        // Update active thumbnail on interaction
        thumbnailController.setOnActiveCallback(() -> {
            activeThumbnailController = thumbnailController;
            System.out.println("Active thumbnail set to: " + (isFirst ? "Thumbnail 1" : "Thumbnail 2"));
        });

        return thumbnailController;
    }

    private void setupMenuActions(MenuItem loadImage, MenuItem saveState, MenuItem loadState, MenuItem removeImage,
                                   MenuItem undo, MenuItem redo) {
        loadImage.setOnAction(e -> loadImage());
        saveState.setOnAction(e -> saveState());
        loadState.setOnAction(e -> loadState());
        removeImage.setOnAction(e -> removeImage());

        // Undo/Redo actions for the active thumbnail
        undo.setOnAction(e -> {
            if (activeThumbnailController != null) {
                System.out.println("Undo triggered for active thumbnail.");
                activeThumbnailController.undo();
            } else {
                System.out.println("No active thumbnail for undo.");
            }
        });

        redo.setOnAction(e -> {
            if (activeThumbnailController != null) {
                System.out.println("Redo triggered for active thumbnail.");
                activeThumbnailController.redo();
            } else {
                System.out.println("No active thumbnail for redo.");
            }
        });
    }

    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            imageModel.loadImageFromFile(file);
            perspectiveController.updateImage(imageModel.getImage());
            thumbnailController1.updateImage(imageModel.getImage());
            thumbnailController2.updateImage(imageModel.getImage());
            System.out.println("Image loaded.");
        } else {
            System.out.println("No image selected.");
        }
    }

    private void removeImage() {
        imageModel.setImage(null);
        perspectiveController.updateImage(null);
        thumbnailController1.updateImage(null);
        thumbnailController2.updateImage(null);
        System.out.println("Image removed.");
    }

    private void saveState() {
        System.out.println("Save state called.");
    }

    private void loadState() {
        System.out.println("Load state called.");
    }
}
