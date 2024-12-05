package com.etslabs.Controllers;

import com.etslabs.Interfaces.Observer;
import com.etslabs.Models.Perspective;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class PerspectiveController implements Observer {
    private final StackPane view;
    private final ImageView perspectiveView;

    public PerspectiveController(Perspective perspective) {
        this.view = new StackPane();
        this.perspectiveView = new ImageView();
        this.perspectiveView.setPreserveRatio(true);
        perspectiveView.fitWidthProperty().bind(view.widthProperty());
        perspectiveView.fitHeightProperty().bind(view.heightProperty());
        this.view.getChildren().add(perspectiveView);
    }

    public StackPane getView() {
        return view;
    }

    @Override
    public void update() {
    }

    public void updateImage(javafx.scene.image.Image image) {
        perspectiveView.setImage(image);
    }
}
