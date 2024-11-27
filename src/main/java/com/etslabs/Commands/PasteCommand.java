package com.etslabs.Commands;

import java.awt.Point;

import com.etslabs.Interfaces.Command;
import com.etslabs.Models.ImageModel;
import com.etslabs.Models.Perspective;

import javafx.scene.image.Image;

public class PasteCommand implements Command {
    private final Perspective perspective;
    private final Image newImage;
    private final double newScaleFactor;
    private final Point newTranslation;

    private Image oldImage;
    private double oldScaleFactor;
    private Point oldTranslation;

    /**
     * @param perspective the Perspective to apply the paste to
     * @param newImage the new Image to paste
     * @param newScaleFactor the new scale factor
     * @param newTranslation the new translation
     */
    public PasteCommand(Perspective perspective, Image newImage, double newScaleFactor, Point newTranslation) {
        this.perspective = perspective;
        this.newImage = newImage;
        this.newScaleFactor = newScaleFactor;
        this.newTranslation = newTranslation;
    }

    @Override
    public void execute() {
        ImageModel imageModel = perspective.getImageModel();
        oldImage = imageModel.getImage();
        oldScaleFactor = perspective.getScaleFactor();
        oldTranslation = new Point(perspective.getTranslation());

        perspective.setImage(newImage);
        perspective.setScaleFactor(newScaleFactor);
        perspective.setTranslation(newTranslation);
    }

    @Override
    public void undo() {
        perspective.setImage(oldImage);
        perspective.setScaleFactor(oldScaleFactor);
        perspective.setTranslation(oldTranslation);
    }
}
