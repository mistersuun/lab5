package com.etslabs.Controllers;

import java.awt.Point;

import com.etslabs.Commands.CommandManager;
import com.etslabs.Commands.PasteCommand;
import com.etslabs.Commands.TranslateCommand;
import com.etslabs.Commands.ZoomCommand;
import com.etslabs.Models.ImageModel;
import com.etslabs.Models.Perspective;

import javafx.animation.PauseTransition; // Ajouté
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration; // Ajouté

public class ThumbnailController {
    private final ImageView thumbnailView = new ImageView();
    private final Perspective perspective; 
    private final CommandManager commandManager = CommandManager.getInstance();
    private double dragStartX, dragStartY;

    private final ObjectProperty<Image> clipboardImage;
    private final DoubleProperty clipboardScaleX;
    private final DoubleProperty clipboardScaleY;
    private final DoubleProperty clipboardTranslateX;
    private final DoubleProperty clipboardTranslateY;

    // Variables pour regrouper les actions de zoom et drag
    private PauseTransition pauseTransition; // Ajouté
    private double cumulativeZoomFactor = 1.0; // Ajouté pour accumuler le zoom
    private double cumulativeOffsetX = 0.0;    // Ajouté pour accumuler le déplacement en X
    private double cumulativeOffsetY = 0.0;    // Ajouté pour accumuler le déplacement en Y

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
        thumbnailView.setFitHeight(500);

        StackPane thumbnailPane = new StackPane(thumbnailView);
        thumbnailPane.setPrefSize(400, 500);

        Rectangle clip = new Rectangle(400, 500);
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

        // Create the container
        VBox container = new VBox(10, thumbnailPane, buttonContainer);
        if (isFirstThumbnail) {
            container.setStyle("-fx-background-color: lightblue; -fx-padding: 10;");
        } else {
            container.setStyle("-fx-background-color: lightgreen; -fx-padding: 10;");
        }

        // Allow the thumbnail pane to grow vertically to fill available space
        VBox.setVgrow(thumbnailPane, Priority.ALWAYS);

        // Attach scroll handler to the entire container instead of just the image
        container.setOnScroll(this::onScroll);

        return container;
    }

    private void initialize() {
        // Initialisation du PauseTransition pour regrouper les actions
        pauseTransition = new PauseTransition(Duration.millis(300)); // Ajouté
        pauseTransition.setOnFinished(event -> commitCommands()); // Ajouté

        thumbnailView.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
            notifyActive(); 
        });

        // On accumule le déplacement au lieu de créer une commande immédiate
        thumbnailView.setOnMouseDragged(event -> {
            double offsetX = event.getSceneX() - dragStartX;
            double offsetY = event.getSceneY() - dragStartY;

            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();

            // Accumulation du déplacement
            cumulativeOffsetX += offsetX;
            cumulativeOffsetY += offsetY;

            // Mise à jour visuelle temporaire du déplacement
            thumbnailView.setTranslateX(thumbnailView.getTranslateX() + offsetX);
            thumbnailView.setTranslateY(thumbnailView.getTranslateY() + offsetY);

            // Redémarrage du PauseTransition
            pauseTransition.stop();
            pauseTransition.playFromStart();

            notifyActive();
        });

        thumbnailView.setOnMouseReleased(event -> {
            System.out.println("Drag completed. Final translation applied: X=" 
                               + thumbnailView.getTranslateX() 
                               + ", Y=" + thumbnailView.getTranslateY());

            // Lâcher la souris signifie potentiellement la fin du drag
            pauseTransition.stop();
            pauseTransition.playFromStart();
        });
    }

    private void onScroll(ScrollEvent e) {
        double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;

        // Accumuler le facteur de zoom au lieu d'exécuter immédiatement une commande
        cumulativeZoomFactor *= zoomFactor;
        System.out.println("Scrolled with zoomFactor: " + zoomFactor + ", cumulativeZoomFactor: " + cumulativeZoomFactor);

        // Mise à jour visuelle temporaire du zoom
        thumbnailView.setScaleX(perspective.getScaleFactor() * cumulativeZoomFactor);
        thumbnailView.setScaleY(perspective.getScaleFactor() * cumulativeZoomFactor);

        // Redémarrer le PauseTransition à chaque scroll
        pauseTransition.stop();
        pauseTransition.playFromStart();

        notifyActive(); 
    }

    // Méthode modifiée pour intégrer la logique de regroupement
    private void commitCommands() { // Ajouté
        // Créer une commande Zoom si on a zoomé
        if (cumulativeZoomFactor != 1.0) {
            double newScaleFactor = Math.max(0.1, Math.min(perspective.getScaleFactor() * cumulativeZoomFactor, 5.0));
            System.out.println("Committing Zoom Command with factor: " + newScaleFactor);
            ZoomCommand zoomCommand = new ZoomCommand(perspective, newScaleFactor);
            commandManager.executeCommand(zoomCommand);
            cumulativeZoomFactor = 1.0;
            applyPerspectiveToThumbnail();
        }

        // Créer une commande Translate si on a déplacé
        if (cumulativeOffsetX != 0.0 || cumulativeOffsetY != 0.0) {
            System.out.println("Committing Translate Command with offsetX: " + cumulativeOffsetX + ", offsetY: " + cumulativeOffsetY);
            TranslateCommand translateCommand = new TranslateCommand(perspective, cumulativeOffsetX, cumulativeOffsetY);
            commandManager.executeCommand(translateCommand);
            cumulativeOffsetX = 0.0;
            cumulativeOffsetY = 0.0;
            applyPerspectiveToThumbnail();
        }
    }

    private void executeZoomCommand(double zoomFactor) {
        double newScaleFactor = Math.max(0.1, Math.min(perspective.getScaleFactor() * zoomFactor, 5.0));
        ZoomCommand zoomCommand = new ZoomCommand(perspective, newScaleFactor);
        commandManager.executeCommand(zoomCommand); 
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
            double pastedScaleFactor = clipboardScaleX.get(); 
            Point pastedTranslation = new Point(
                (int) clipboardTranslateX.get(),
                (int) clipboardTranslateY.get()
            );

            PasteCommand pasteCommand = new PasteCommand(perspective, pastedImage, pastedScaleFactor, pastedTranslation);
            commandManager.executeCommand(pasteCommand); 
            applyPerspectiveToThumbnail();

            System.out.println("Image and state pasted to thumbnail.");
        } else {
            System.out.println("Clipboard is empty.");
        }
    }

    public void undo() {
        System.out.println("Undo called.");
        commandManager.undo();
        applyPerspectiveToThumbnail();
    }

    public void redo() {
        System.out.println("Redo called.");
        commandManager.redo();
        applyPerspectiveToThumbnail(); 
    }

    public void updateImage(Image image) {
        thumbnailView.setImage(image);
        perspective.setScaleFactor(1.0);
        perspective.setTranslation(new Point(0, 0));
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
