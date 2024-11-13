package com.etslabs;

import com.etslabs.Models.ImageModel;
import com.etslabs.Models.Perspective;
import com.etslabs.Commands.CommandManager;
import com.etslabs.Commands.ZoomCommand;
import com.etslabs.Commands.TranslateCommand;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Stack;

import com.etslabs.Converter.ImageConverter;

public class MainApp extends Application {
    private final ImageModel imageModel = new ImageModel();
    private final Perspective perspective = new Perspective(imageModel);
    private final CommandManager commandManager = CommandManager.getInstance();
    private final Stack<ZoomCommand> undoStack = new Stack<>();
    private final Stack<ZoomCommand> redoStack = new Stack<>();
    private final ImageView perspectiveView = new ImageView();
    private final ImageView thumbnailView1 = new ImageView();
    private final ImageView thumbnailView2 = new ImageView();
    private double thumbnail1Scale = 1.0;
    private double thumbnail2Scale = 1.0;
    private double thumbnail1TranslateX = 0, thumbnail1TranslateY = 0;
    private double thumbnail2TranslateX = 0, thumbnail2TranslateY = 0;

    private double dragStartX, dragStartY;

    private double copiedScale = 1.0;
    private double copiedTranslateX = 0;
    private double copiedTranslateY = 0;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Viewer with Enhanced Layout");

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
        MenuItem copyThumbnail = new MenuItem("Copy Thumbnail");
        MenuItem pasteThumbnail = new MenuItem("Paste Thumbnail");
        editMenu.getItems().addAll(undo, redo, copyThumbnail, pasteThumbnail);

        menuBar.getMenus().addAll(fileMenu, editMenu);

        perspectiveView.setPreserveRatio(true);
        VBox perspectivePanel = new VBox(perspectiveView);
        VBox.setVgrow(perspectivePanel, Priority.ALWAYS);
        perspectivePanel.setStyle("-fx-border-width: 2; -fx-border-color: gray;");

        VBox thumbnail1Container = createThumbnailContainer(thumbnailView1, true);
        VBox thumbnail2Container = createThumbnailContainer(thumbnailView2, false);

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

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        loadImage.setOnAction(e -> loadImage(primaryStage));
        saveState.setOnAction(e -> saveState(primaryStage));
        loadState.setOnAction(e -> loadState(primaryStage));
        removeImage.setOnAction(e -> removeImage());
        undo.setOnAction(e -> undo());
        redo.setOnAction(e -> redo());
        copyThumbnail.setOnAction(e -> copyThumbnail());
        pasteThumbnail.setOnAction(e -> pasteThumbnail());

        setDragHandlers(thumbnailView1, true);
        setDragHandlers(thumbnailView2, false);

        setScrollHandlers(thumbnailView1, true);
        setScrollHandlers(thumbnailView2, false);
    }

    private final ObjectProperty<Image> clipboardImage = new SimpleObjectProperty<>();
    private final DoubleProperty clipboardScaleX = new SimpleDoubleProperty(1.0);
    private final DoubleProperty clipboardScaleY = new SimpleDoubleProperty(1.0);
    private final DoubleProperty clipboardTranslateX = new SimpleDoubleProperty(0.0);
    private final DoubleProperty clipboardTranslateY = new SimpleDoubleProperty(0.0);

    private void handleCopy(ImageView thumbnailView) {
        if (thumbnailView.getImage() != null) {
            clipboardImage.set(thumbnailView.getImage());
            clipboardScaleX.set(thumbnailView.getScaleX());
            clipboardScaleY.set(thumbnailView.getScaleY());
            clipboardTranslateX.set(thumbnailView.getTranslateX());
            clipboardTranslateY.set(thumbnailView.getTranslateY());
            System.out.println("Image and state copied to clipboard.");
        } else {
            System.out.println("No image to copy.");
        }
    }

    private void handlePaste(ImageView thumbnailView) {
        if (clipboardImage.get() != null) {
            thumbnailView.setImage(clipboardImage.get());
            thumbnailView.setScaleX(clipboardScaleX.get());
            thumbnailView.setScaleY(clipboardScaleY.get());
            thumbnailView.setTranslateX(clipboardTranslateX.get());
            thumbnailView.setTranslateY(clipboardTranslateY.get());
            System.out.println("Image and state pasted from clipboard.");
        } else {
            System.out.println("Clipboard is empty.");
        }
    }

    private VBox createThumbnailContainer(ImageView thumbnailView, boolean isFirstThumbnail) {
        thumbnailView.setPreserveRatio(true);
        thumbnailView.setFitWidth(400);
        thumbnailView.setFitHeight(200);
    
        StackPane thumbnailPane = new StackPane(thumbnailView);
        thumbnailPane.setPrefSize(400, 200);
    
        Rectangle clip = new Rectangle(400, 200);
        thumbnailPane.setClip(clip);
        thumbnailPane.setStyle("-fx-border-width: 1; -fx-border-color: black;");
    
        Button zoomInButton = new Button("Zoom In");
        Button zoomOutButton = new Button("Zoom Out");
    
        zoomInButton.setOnAction(e -> {
            thumbnailView.setScaleX(thumbnailView.getScaleX() * 1.1);
            thumbnailView.setScaleY(thumbnailView.getScaleY() * 1.1);
        });
    
        zoomOutButton.setOnAction(e -> {
            thumbnailView.setScaleX(thumbnailView.getScaleX() * 0.9);
            thumbnailView.setScaleY(thumbnailView.getScaleY() * 0.9);
        });
    
        HBox buttonContainer = new HBox(10, zoomInButton, zoomOutButton);
        buttonContainer.setStyle("-fx-alignment: center; -fx-padding: 5;");
    
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");
    
        copyItem.setOnAction(e -> handleCopy(thumbnailView));
        pasteItem.setOnAction(e -> handlePaste(thumbnailView));
    
        contextMenu.getItems().addAll(copyItem, pasteItem);
    
        thumbnailView.setOnContextMenuRequested(event -> {
            contextMenu.show(thumbnailView, event.getScreenX(), event.getScreenY());
        });
    
        thumbnailView.setOnScroll(e -> {
            double zoomFactor = (e.getDeltaY() > 0) ? 1.1 : 0.9;
            thumbnailView.setScaleX(thumbnailView.getScaleX() * zoomFactor);
            thumbnailView.setScaleY(thumbnailView.getScaleY() * zoomFactor);
        });
    
        VBox container = new VBox(10, thumbnailPane, buttonContainer);
        if (isFirstThumbnail) {
            container.setStyle("-fx-background-color: lightblue; -fx-padding: 10;");
        } else {
            container.setStyle("-fx-background-color: lightgreen; -fx-padding: 10;");
        }
    
        return container;
    }    

    private void zoomThumbnail(ImageView thumbnail, double zoomFactor, boolean isFirstThumbnail) {
        double newScale = (isFirstThumbnail ? thumbnail1Scale : thumbnail2Scale) * zoomFactor;

        if (newScale > 0.1 && newScale < 5) {
            if (isFirstThumbnail) {
                thumbnail1Scale = newScale;
            } else {
                thumbnail2Scale = newScale;
            }
            thumbnail.setScaleX(newScale);
            thumbnail.setScaleY(newScale);
        }
    }

    private void setDragHandlers(ImageView thumbnail, boolean isFirstThumbnail) {
        thumbnail.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
        });

        thumbnail.setOnMouseDragged(event -> {
            double offsetX = event.getSceneX() - dragStartX;
            double offsetY = event.getSceneY() - dragStartY;

            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();

            if (isFirstThumbnail) {
                thumbnail1TranslateX += offsetX;
                thumbnail1TranslateY += offsetY;
                applyTranslation(thumbnail, thumbnail1TranslateX, thumbnail1TranslateY);
            } else {
                thumbnail2TranslateX += offsetX;
                thumbnail2TranslateY += offsetY;
                applyTranslation(thumbnail, thumbnail2TranslateX, thumbnail2TranslateY);
            }
        });
    }

    private void setScrollHandlers(ImageView thumbnail, boolean isFirstThumbnail) {
        thumbnail.setOnScroll((ScrollEvent e) -> {
            double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            zoomThumbnail(thumbnail, zoomFactor, isFirstThumbnail);
        });
    }

    private void applyTranslation(ImageView thumbnail, double translateX, double translateY) {
        thumbnail.setTranslateX(translateX);
        thumbnail.setTranslateY(translateY);
    }

    private void copyThumbnail() {
        copiedScale = thumbnail1Scale;
        copiedTranslateX = thumbnail1TranslateX;
        copiedTranslateY = thumbnail1TranslateY;
        System.out.println("Thumbnail copied.");
    }

    private void pasteThumbnail() {
        thumbnail2Scale = copiedScale;
        thumbnail2TranslateX = copiedTranslateX;
        thumbnail2TranslateY = copiedTranslateY;

        thumbnailView2.setScaleX(thumbnail2Scale);
        thumbnailView2.setScaleY(thumbnail2Scale);
        applyTranslation(thumbnailView2, thumbnail2TranslateX, thumbnail2TranslateY);

        System.out.println("Thumbnail pasted.");
    }

    private void loadImage(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                if (bufferedImage.getType() != BufferedImage.TYPE_INT_ARGB) {
                    BufferedImage argbImage = new BufferedImage(
                            bufferedImage.getWidth(),
                            bufferedImage.getHeight(),
                            BufferedImage.TYPE_INT_ARGB
                    );
                    argbImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
                    bufferedImage = argbImage;
                }
                Image fxImage = ImageConverter.bufferedImageToWritableImage(bufferedImage);
                imageModel.setImage(fxImage);
                perspectiveView.setImage(fxImage);
                thumbnailView1.setImage(fxImage);
                thumbnailView2.setImage(fxImage);
            } catch (Exception e) {
                System.out.println("Failed to load image: " + e.getMessage());
            }
        }
    }

    private void removeImage() {
        imageModel.setImage(null);
        perspectiveView.setImage(null);
        thumbnailView1.setImage(null);
        thumbnailView2.setImage(null);
    }

    private void saveState(Stage primaryStage) {
        System.out.println("Save state.");
    }

    private void loadState(Stage primaryStage) {
        System.out.println("Load state.");
    }

    private void undo() {
        System.out.println("Undo action.");
    }

    private void redo() {
        System.out.println("Redo action.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
