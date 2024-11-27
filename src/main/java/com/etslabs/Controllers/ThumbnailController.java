package com.etslabs.Controllers;

import java.awt.Point;

import com.etslabs.Commands.CommandManager;
import com.etslabs.Commands.PasteCommand;
import com.etslabs.Commands.TranslateCommand;
import com.etslabs.Commands.ZoomCommand;
import com.etslabs.Models.ImageModel;
import com.etslabs.Models.Perspective;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Controller for handling the Thumbnail view.
 */
public class ThumbnailController {
    private final ImageView thumbnailView = new ImageView();
    private final Perspective perspective; // Dedicated perspective for this thumbnail
    private final CommandManager commandManager = CommandManager.getInstance();
    private double dragStartX, dragStartY;

    private final ObjectProperty<Image> clipboardImage;
    private final DoubleProperty clipboardScaleX;
    private final DoubleProperty clipboardScaleY;
    private final DoubleProperty clipboardTranslateX;
    private final DoubleProperty clipboardTranslateY;

    public ThumbnailController(ImageModel imageModel,
                                ObjectProperty<Image> clipboardImage,
                                DoubleProperty clipboardScaleX, DoubleProperty clipboardScaleY,
                                DoubleProperty clipboardTranslateX, DoubleProperty clipboardTranslateY) {
        this.clipboardImage = clipboardImage;
        this.clipboardScaleX = clipboardScaleX;
        this.clipboardScaleY = clipboardScaleY;
        this.clipboardTranslateX = clipboardTranslateX;
        this.clipboardTranslateY = clipboardTranslateY;

        this.perspective = new Perspective(imageModel);
        initialize();
    }

    public VBox getThumbnailContainer(boolean isFirstThumbnail) {
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
            executeZoomCommand(1.1);
            notifyActive();
        });

        zoomOutButton.setOnAction(e -> {
            executeZoomCommand(0.9);
            notifyActive();
        });

        HBox buttonContainer = new HBox(10, zoomInButton, zoomOutButton);
        buttonContainer.setStyle("-fx-alignment: center; -fx-padding: 5;");

        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");

        copyItem.setOnAction(e -> handleCopy());
        pasteItem.setOnAction(e -> handlePaste());

        contextMenu.getItems().addAll(copyItem, pasteItem);

        thumbnailView.setOnContextMenuRequested(event -> {
            contextMenu.show(thumbnailView, event.getScreenX(), event.getScreenY());
        });

        thumbnailView.setOnScroll(this::onScroll);

        VBox container = new VBox(10, thumbnailPane, buttonContainer);
        if (isFirstThumbnail) {
            container.setStyle("-fx-background-color: lightblue; -fx-padding: 10;");
        } else {
            container.setStyle("-fx-background-color: lightgreen; -fx-padding: 10;");
        }

        return container;
    }

    private void initialize() {
        thumbnailView.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
            notifyActive(); // Set as active on mouse press
        });

        thumbnailView.setOnMouseDragged(event -> {
            // Update visual feedback while dragging
            double offsetX = event.getSceneX() - dragStartX;
            double offsetY = event.getSceneY() - dragStartY;

            thumbnailView.setTranslateX(thumbnailView.getTranslateX() + offsetX);
            thumbnailView.setTranslateY(thumbnailView.getTranslateY() + offsetY);

            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
        });

        thumbnailView.setOnMouseReleased(event -> {
            // Commit the final position on drag release
            double offsetX = thumbnailView.getTranslateX();
            double offsetY = thumbnailView.getTranslateY();

            TranslateCommand translateCommand = new TranslateCommand(perspective, offsetX, offsetY);
            translateCommand.execute();
            commandManager.executeCommand(translateCommand); // Updated to use executeCommand

            applyPerspectiveToThumbnail(); // Update thumbnail to reflect the final state
            System.out.println("Drag completed. Final translation applied: X=" + offsetX + ", Y=" + offsetY);
        });
    }

    private void onScroll(ScrollEvent e) {
        double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
        executeZoomCommand(zoomFactor);
        notifyActive(); // Set as active on scroll
    }

    private void executeZoomCommand(double zoomFactor) {
        double newScaleFactor = Math.max(0.1, Math.min(perspective.getScaleFactor() * zoomFactor, 5.0));
        ZoomCommand zoomCommand = new ZoomCommand(perspective, newScaleFactor);
        commandManager.executeCommand(zoomCommand); // Updated to use executeCommand
        applyPerspectiveToThumbnail();
    }

    private void applyPerspectiveToThumbnail() {
        thumbnailView.setScaleX(perspective.getScaleFactor());
        thumbnailView.setScaleY(perspective.getScaleFactor());
        thumbnailView.setTranslateX(perspective.getTranslation().getX());
        thumbnailView.setTranslateY(perspective.getTranslation().getY());
    }

    private void handleCopy() {
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

    private void handlePaste() {
        if (clipboardImage.get() != null) {
            Image pastedImage = clipboardImage.get();
            double pastedScaleFactor = clipboardScaleX.get(); // Assuming uniform scaling
            Point pastedTranslation = new Point(
                (int) clipboardTranslateX.get(),
                (int) clipboardTranslateY.get()
            );

            PasteCommand pasteCommand = new PasteCommand(perspective, pastedImage, pastedScaleFactor, pastedTranslation);
            commandManager.executeCommand(pasteCommand); // Updated to use executeCommand
            applyPerspectiveToThumbnail();

            System.out.println("Image and state pasted to thumbnail.");
        } else {
            System.out.println("Clipboard is empty.");
        }
    }

    public void undo() {
        commandManager.undo();
        applyPerspectiveToThumbnail(); // Ensure the thumbnail reflects the undone state
    }

    public void redo() {
        commandManager.redo();
        applyPerspectiveToThumbnail(); // Ensure the thumbnail reflects the redone state
    }

    public void updateImage(Image image) {
        thumbnailView.setImage(image);
        perspective.setScaleFactor(1.0); // Reset zoom
        perspective.setTranslation(new Point(0, 0)); // Reset translation
        applyPerspectiveToThumbnail();
    }

    public ImageView getThumbnailView() {
        return thumbnailView;
    }

    private Runnable onActiveCallback;

    public void setOnActiveCallback(Runnable onActiveCallback) {
        this.onActiveCallback = onActiveCallback;
    }

    private void notifyActive() {
        if (onActiveCallback != null) {
            onActiveCallback.run();
            System.out.println("Thumbnail set as active.");
        }
    }
}
