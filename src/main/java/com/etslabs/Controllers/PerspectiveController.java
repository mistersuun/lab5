package com.etslabs.Controllers;

import com.etslabs.Commands.CommandManager;
import com.etslabs.Commands.TranslateCommand;
import com.etslabs.Commands.ZoomCommand;
import com.etslabs.Models.Perspective;

import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

public class PerspectiveController {
    private final Perspective perspective;
    private final StackPane view;
    private final ImageView perspectiveView;
    private double scale = 1.0;
    private double translateX = 0;
    private double translateY = 0;
    private double dragStartX, dragStartY;
    private final CommandManager commandManager = CommandManager.getInstance();

    public PerspectiveController(Perspective perspective) {
        this.perspective = perspective;
        this.view = new StackPane();
        this.perspectiveView = new ImageView();
        this.perspectiveView.setPreserveRatio(true); // Maintain image aspect ratio
        this.view.getChildren().add(perspectiveView);
        initialize();
    }

    public StackPane getView() {
        return view;
    }

    private void initialize() {
        // Event handlers for panning
        perspectiveView.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
        });

        perspectiveView.setOnMouseDragged(event -> {
            double offsetX = event.getSceneX() - dragStartX;
            double offsetY = event.getSceneY() - dragStartY;

            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();

            // Execute TranslateCommand to track the translation
            TranslateCommand translateCommand = new TranslateCommand(perspective, offsetX, offsetY);
            commandManager.executeCommand(translateCommand);

            translateX += offsetX;
            translateY += offsetY;
            applyTranslation();
        });

        // Event handler for zooming
        perspectiveView.setOnScroll(this::onScroll);

        // Resize listener to adjust the clip and centering
        view.widthProperty().addListener((obs, oldVal, newVal) -> applyTranslation());
        view.heightProperty().addListener((obs, oldVal, newVal) -> applyTranslation());
    }

    private void onScroll(ScrollEvent e) {
        double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;

        // Execute ZoomCommand to track the zoom action
        double newScale = Math.max(0.1, Math.min(scale * zoomFactor, 5.0));
        ZoomCommand zoomCommand = new ZoomCommand(perspective, newScale);
        commandManager.executeCommand(zoomCommand);

        scale = newScale;
        perspectiveView.setScaleX(scale);
        perspectiveView.setScaleY(scale);
    }

    private void applyTranslation() {
        perspectiveView.setTranslateX(translateX);
        perspectiveView.setTranslateY(translateY);
    }

    public void updateImage(javafx.scene.image.Image image) {
        if (image != null) {
            perspectiveView.setImage(image);
            resetView();
        }
    }

    private void resetView() {
        // Reset scale and translation
        scale = 1.0;
        translateX = 0;
        translateY = 0;
        perspectiveView.setScaleX(scale);
        perspectiveView.setScaleY(scale);
        applyTranslation();
    }
}
