package com.etslabs.Controllers;

import com.etslabs.Commands.CommandManager;
import com.etslabs.Commands.TranslateCommand;
import com.etslabs.Commands.ZoomCommand;
import com.etslabs.Interfaces.Command;
import com.etslabs.Interfaces.Observer;
import com.etslabs.Models.Perspective;

import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
public class PerspectiveController implements Observer {
    private final Perspective perspective;
    private final StackPane view;
    private final ImageView perspectiveView;
    private double scale = 1.0;
    private double translateX = 0;
    private double translateY = 0;
    private double dragStartX, dragStartY;
    private final CommandManager commandManager = CommandManager.getInstance();

    /**
     * @param perspective the Perspective to control
     */
    public PerspectiveController(Perspective perspective) {
        this.perspective = perspective;
        this.view = new StackPane();
        this.perspectiveView = new ImageView();
        this.perspectiveView.setPreserveRatio(true); 
        this.view.getChildren().add(perspectiveView);
        this.perspective.addObserver(this); 
        initialize();
    }

    /**
     * @return the StackPane containing the Perspective view
     */
    public StackPane getView() {
        return view;
    }

    private void initialize() {
        perspectiveView.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
        });

        perspectiveView.setOnMouseDragged(event -> {
            double offsetX = event.getSceneX() - dragStartX;
            double offsetY = event.getSceneY() - dragStartY;

            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();

            Command translateCommand = new TranslateCommand(perspective, offsetX, offsetY);
            translateCommand.execute();
            commandManager.addCommand(translateCommand);

            translateX += offsetX;
            translateY += offsetY;
            applyTranslation();
        });

        perspectiveView.setOnScroll(this::onScroll);

        view.widthProperty().addListener((obs, oldVal, newVal) -> applyTranslation());
        view.heightProperty().addListener((obs, oldVal, newVal) -> applyTranslation());
    }

    private void onScroll(ScrollEvent e) {
        double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;

        double newScale = Math.max(0.1, Math.min(scale * zoomFactor, 5.0));
        Command zoomCommand = new ZoomCommand(perspective, newScale);
        zoomCommand.execute();
        commandManager.addCommand(zoomCommand);

        scale = newScale;
        perspectiveView.setScaleX(scale);
        perspectiveView.setScaleY(scale);
    }

    private void applyTranslation() {
        perspectiveView.setTranslateX(translateX);
        perspectiveView.setTranslateY(translateY);
    }

    @Override
    public void update() {
        scale = perspective.getScaleFactor();
        translateX = perspective.getTranslation().getX();
        translateY = perspective.getTranslation().getY();

        perspectiveView.setScaleX(scale);
        perspectiveView.setScaleY(scale);
        perspectiveView.setTranslateX(translateX);
        perspectiveView.setTranslateY(translateY);
        perspectiveView.setImage(perspective.getTransformedImage());
    }

    public void updateImage(javafx.scene.image.Image image) {
        if (image != null) {
            perspectiveView.setImage(image);
            resetView();
        }
    }

    private void resetView() {
        scale = 1.0;
        translateX = 0;
        translateY = 0;
        perspectiveView.setScaleX(scale);
        perspectiveView.setScaleY(scale);
        applyTranslation();
    }

    public void undo() {
        commandManager.undo();
    }

    public void redo() {
        commandManager.redo();
    }
}
