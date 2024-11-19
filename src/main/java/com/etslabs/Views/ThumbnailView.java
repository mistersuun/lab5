package com.etslabs.Views;

import com.etslabs.Interfaces.Observer;
import com.etslabs.Models.ImageModel;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class ThumbnailView extends Pane implements Observer {
    private final ImageModel imageModel;
    private final Canvas canvas;

    public ThumbnailView(ImageModel imageModel) {
        this.imageModel = imageModel;
        this.imageModel.addObserver(this);

        canvas = new Canvas(200, 150); 
        getChildren().add(canvas);
    }

    @Override
    public void update() {
        repaint();
    }

    private void repaint() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); 

        Image fxImage = imageModel.getImage();
        if (fxImage != null) {
            double imageAspectRatio = fxImage.getWidth() / fxImage.getHeight();
            double canvasAspectRatio = canvas.getWidth() / canvas.getHeight();

            double drawWidth = canvas.getWidth();
            double drawHeight = canvas.getHeight();

            if (imageAspectRatio > canvasAspectRatio) {
                drawHeight = canvas.getWidth() / imageAspectRatio;
            } else {
                drawWidth = canvas.getHeight() * imageAspectRatio;
            }

            double xOffset = (canvas.getWidth() - drawWidth) / 2;
            double yOffset = (canvas.getHeight() - drawHeight) / 2;

            gc.drawImage(fxImage, xOffset, yOffset, drawWidth, drawHeight);
        }
    }
}
