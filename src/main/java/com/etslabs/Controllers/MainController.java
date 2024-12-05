package com.etslabs.Controllers;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.etslabs.AppState;
import com.etslabs.Models.ImageModel;
import com.etslabs.Models.Perspective;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
    private PerspectiveController perspectiveController;
    private ThumbnailController thumbnailController1;
    private ThumbnailController thumbnailController2;
    private ThumbnailController activeThumbnailController;
    private File currentImageFile;

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

        perspectiveController = new PerspectiveController(perspective);

        VBox perspectivePanel = new VBox(perspectiveController.getView());
        perspectivePanel.setMinWidth(0);
        VBox.setVgrow(perspectivePanel, Priority.ALWAYS);

        thumbnailController1 = createThumbnailController(true);
        thumbnailController2 = createThumbnailController(false);

        VBox thumbnail1Container = thumbnailController1.getThumbnailContainer(true);
        VBox thumbnail2Container = thumbnailController2.getThumbnailContainer(false);

        thumbnail1Container.setMinWidth(0);
        thumbnail2Container.setMinWidth(0);
        VBox.setVgrow(thumbnail1Container, Priority.ALWAYS);
        VBox.setVgrow(thumbnail2Container, Priority.ALWAYS);

        HBox mainContainer = new HBox(perspectivePanel, thumbnail1Container, thumbnail2Container);
        mainContainer.setMinWidth(0);
        HBox.setHgrow(perspectivePanel, Priority.ALWAYS);
        HBox.setHgrow(thumbnail1Container, Priority.ALWAYS);
        HBox.setHgrow(thumbnail2Container, Priority.ALWAYS);

        // Ensure each container takes exactly one-third of the width
        perspectivePanel.prefWidthProperty().bind(mainContainer.widthProperty().divide(3));
        thumbnail1Container.prefWidthProperty().bind(mainContainer.widthProperty().divide(3));
        thumbnail2Container.prefWidthProperty().bind(mainContainer.widthProperty().divide(3));

        // Ensure each container fills the height
        perspectivePanel.prefHeightProperty().bind(mainContainer.heightProperty());
        thumbnail1Container.prefHeightProperty().bind(mainContainer.heightProperty());
        thumbnail2Container.prefHeightProperty().bind(mainContainer.heightProperty());

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(mainContainer);

        setupMenuActions(loadImage, saveState, loadState, removeImage, undo, redo);

        return new Scene(root, 900, 600);
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
        thumbnailController.setOnActiveCallback(() -> {
            activeThumbnailController = thumbnailController;
        });
        return thumbnailController;
    }

    private void setupMenuActions(MenuItem loadImage, MenuItem saveState, MenuItem loadState, MenuItem removeImage,
                                  MenuItem undo, MenuItem redo) {
        loadImage.setOnAction(e -> loadImage());
        saveState.setOnAction(e -> saveState());
        loadState.setOnAction(e -> loadState());
        removeImage.setOnAction(e -> removeImage());

        undo.setOnAction(e -> {
            if (activeThumbnailController != null) {
                activeThumbnailController.undo();
            }
        });

        redo.setOnAction(e -> {
            if (activeThumbnailController != null) {
                activeThumbnailController.redo();
            }
        });
    }

    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            currentImageFile = file;
            imageModel.loadImageFromFile(file);
            perspectiveController.updateImage(imageModel.getImage());
            thumbnailController1.updateImage(imageModel.getImage());
            thumbnailController2.updateImage(imageModel.getImage());
        }
    }

    private void removeImage() {
        imageModel.setImage(null);
        perspectiveController.updateImage(null);
        thumbnailController1.updateImage(null);
        thumbnailController2.updateImage(null);
        currentImageFile = null;
    }

    private void saveState() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save State");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("State Files", "*.perspective"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                String imagePath = null;
                if (currentImageFile != null) {
                    imagePath = currentImageFile.getAbsolutePath();
                }
                oos.writeObject(imagePath);

                ImageView thumbnailView1 = thumbnailController1.getThumbnailView();
                AppState thumbnail1State = new AppState(
                    thumbnailView1.getScaleX(),
                    thumbnailView1.getTranslateX(),
                    thumbnailView1.getTranslateY(),
                    new Point((int) thumbnailView1.getTranslateX(), (int) thumbnailView1.getTranslateY())
                );
                oos.writeObject(thumbnail1State);

                ImageView thumbnailView2 = thumbnailController2.getThumbnailView();
                AppState thumbnail2State = new AppState(
                    thumbnailView2.getScaleX(),
                    thumbnailView2.getTranslateX(),
                    thumbnailView2.getTranslateY(),
                    new Point((int) thumbnailView2.getTranslateX(), (int) thumbnailView2.getTranslateY())
                );
                oos.writeObject(thumbnail2State);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadState() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load State");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("State Files", "*.perspective"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                String imagePath = (String) ois.readObject();
                AppState thumbnail1State = (AppState) ois.readObject();
                AppState thumbnail2State = (AppState) ois.readObject();
                if (imagePath != null) {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        currentImageFile = imageFile;
                        imageModel.loadImageFromFile(imageFile);
                        perspectiveController.updateImage(imageModel.getImage());
                        thumbnailController1.updateImage(imageModel.getImage());
                        thumbnailController2.updateImage(imageModel.getImage());
                    }
                }

                ImageView thumbnailView1 = thumbnailController1.getThumbnailView();
                thumbnailView1.setScaleX(thumbnail1State.getScaleFactor());
                thumbnailView1.setScaleY(thumbnail1State.getScaleFactor());
                thumbnailView1.setTranslateX(thumbnail1State.getTranslateX());
                thumbnailView1.setTranslateY(thumbnail1State.getTranslateY());

                ImageView thumbnailView2 = thumbnailController2.getThumbnailView();
                thumbnailView2.setScaleX(thumbnail2State.getScaleFactor());
                thumbnailView2.setScaleY(thumbnail2State.getScaleFactor());
                thumbnailView2.setTranslateX(thumbnail2State.getTranslateX());
                thumbnailView2.setTranslateY(thumbnail2State.getTranslateY());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
