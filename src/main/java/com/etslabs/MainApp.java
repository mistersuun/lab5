package com.etslabs;

import com.etslabs.Models.ImageModel;
import com.etslabs.Models.Perspective;
import com.etslabs.Commands.CommandManager;
import com.etslabs.Commands.ZoomCommand;
import com.etslabs.Commands.TranslateCommand;
import com.etslabs.Converter.ImageConverter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Stack;

public class MainApp extends Application {
    private final ImageModel imageModel = new ImageModel();
    private final Perspective perspective = new Perspective(imageModel);
    private final CommandManager commandManager = CommandManager.getInstance();
    private final Stack<ZoomCommand> undoStack = new Stack<>();
    private final Stack<ZoomCommand> redoStack = new Stack<>();
    private final Stack<TranslateCommand> translateUndoStack = new Stack<>();
    private final Stack<TranslateCommand> translateRedoStack = new Stack<>();
    private final ImageView imageView = new ImageView();
    private double scaleFactor = 1.0; 
    private double translateX = 0; 
    private double translateY = 0;
    private double dragStartX, dragStartY; 

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Viewer with Zoom, Pan, Undo, and Redo");

        VBox buttonPanel = new VBox(10);
        Button zoomInButton = new Button("Zoom In");
        Button zoomOutButton = new Button("Zoom Out");
        Button undoButton = new Button("Undo");
        Button redoButton = new Button("Redo");
        Button loadImageButton = new Button("Load Image");
        Button removeImageButton = new Button("Remove Image");

        buttonPanel.getChildren().addAll(zoomInButton, zoomOutButton, undoButton, redoButton, loadImageButton, removeImageButton);

        imageView.setPreserveRatio(true);
        imageView.setFitWidth(400); 
        imageView.setFitHeight(400);

        BorderPane imagePane = new BorderPane();
        imagePane.setCenter(imageView);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(buttonPanel, imagePane);
        splitPane.setDividerPositions(0.3); 

        Scene scene = new Scene(splitPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        zoomInButton.setOnAction(e -> zoomIn());
        zoomOutButton.setOnAction(e -> zoomOut());
        undoButton.setOnAction(e -> undo());
        redoButton.setOnAction(e -> redo());
        loadImageButton.setOnAction(e -> loadImage(primaryStage));
        removeImageButton.setOnAction(e -> removeImage());

        imageView.setOnMousePressed(this::startDrag);
        imageView.setOnMouseDragged(this::dragImage);
    }

    private void zoomIn() {
        scaleFactor *= 1.1; 
        executeZoomCommand(new ZoomCommand(perspective, scaleFactor));
    }

    private void zoomOut() {
        scaleFactor /= 1.1; 
        executeZoomCommand(new ZoomCommand(perspective, scaleFactor));
    }

    private void executeZoomCommand(ZoomCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); 
        applyZoom();
    }

    private void applyZoom() {
        imageView.setScaleX(scaleFactor);
        imageView.setScaleY(scaleFactor);
    }

    private void startDrag(MouseEvent event) {
        dragStartX = event.getSceneX();
        dragStartY = event.getSceneY();
    }

    private void dragImage(MouseEvent event) {
        double offsetX = event.getSceneX() - dragStartX;
        double offsetY = event.getSceneY() - dragStartY;

        dragStartX = event.getSceneX();
        dragStartY = event.getSceneY();

        translateX += offsetX;
        translateY += offsetY;

        applyTranslation();

        TranslateCommand command = new TranslateCommand(perspective, offsetX, offsetY);
        command.execute();
        translateUndoStack.push(command);
        translateRedoStack.clear(); 
    }

    private void applyTranslation() {
        imageView.setTranslateX(translateX);
        imageView.setTranslateY(translateY);
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            ZoomCommand zoomCommand = undoStack.pop();
            zoomCommand.undo();
            redoStack.push(zoomCommand);
            scaleFactor = zoomCommand.getOldScaleFactor(); 
            applyZoom();
        } else if (!translateUndoStack.isEmpty()) {
            TranslateCommand translateCommand = translateUndoStack.pop();
            translateCommand.undo();
            translateRedoStack.push(translateCommand);
            translateX -= translateCommand.getOffsetX();
            translateY -= translateCommand.getOffsetY();
            applyTranslation();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            ZoomCommand zoomCommand = redoStack.pop();
            zoomCommand.execute();
            undoStack.push(zoomCommand);
            scaleFactor = zoomCommand.getNewScaleFactor();
            applyZoom();
        } else if (!translateRedoStack.isEmpty()) {
            TranslateCommand translateCommand = translateRedoStack.pop();
            translateCommand.execute();
            translateUndoStack.push(translateCommand);
            translateX += translateCommand.getOffsetX();
            translateY += translateCommand.getOffsetY();
            applyTranslation();
        }
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
                imageView.setImage(fxImage); 
                scaleFactor = 1.0; 
                translateX = 0;
                translateY = 0; 
                applyZoom();
                applyTranslation();
            } catch (Exception e) {
                System.out.println("Failed to load image: " + e.getMessage());
            }
        }
    }

    private void removeImage() {
        imageModel.setImage(null); 
        imageView.setImage(null);   
        scaleFactor = 1.0; 
        translateX = 0;
        translateY = 0; 
        applyZoom();
        applyTranslation();
    }

    public static void main(String[] args) {
        launch(args); 
    }
}
