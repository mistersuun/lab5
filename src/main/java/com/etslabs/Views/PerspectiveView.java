package com.etslabs.Views;

import com.etslabs.Interfaces.Observer;
import com.etslabs.Models.Perspective;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 * View component for displaying the transformed perspective image.
 */
public class PerspectiveView extends Pane implements Observer {
    private final Perspective perspective;
    private final Canvas canvas;

    public PerspectiveView(Perspective perspective) {
        this.perspective = perspective;
        this.perspective.addObserver(this);

        canvas = new Canvas(800, 600); // Adjust size as needed
        getChildren().add(canvas);
    }

    /**
     * Update method called when the observed Perspective changes.
     */
    @Override
    public void update() {
        repaint();
    }

    /**
     * Repaint the perspective canvas based on the current image and transformations.
     */
    private void repaint() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); 

        Image fxImage = perspective.getTransformedImage();
        if (fxImage != null) {
            gc.save(); // Save the current state

            // Apply transformations
            gc.translate(perspective.getTranslation().getX(), perspective.getTranslation().getY());
            gc.scale(perspective.getScaleFactor(), perspective.getScaleFactor());

            gc.drawImage(fxImage, 0, 0);

            gc.restore(); // Restore the state
        }
    }
}
