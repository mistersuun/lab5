package com.etslabs.Commands;

import java.awt.Point;

import com.etslabs.Interfaces.Command;
import com.etslabs.Models.ImageModel;
import com.etslabs.Models.Perspective;

import javafx.scene.image.Image;

/**
 * Command to handle pasting an image and its state to the Perspective.
 */
public class PasteCommand implements Command {
    private final Perspective perspective;
    private final Image newImage;
    private final double newScaleFactor;
    private final Point newTranslation;

    private Image oldImage;
    private double oldScaleFactor;
    private Point oldTranslation;

    /**
     * Constructor for PasteCommand.
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

    /**
     * Execute the paste command by setting the new image and state.
     */
    @Override
    public void execute() {
        // Save old state
        ImageModel imageModel = perspective.getImageModel();
        oldImage = imageModel.getImage();
        oldScaleFactor = perspective.getScaleFactor();
        oldTranslation = new Point(perspective.getTranslation());

        // Apply new state
        perspective.setImage(newImage);
        perspective.setScaleFactor(newScaleFactor);
        perspective.setTranslation(newTranslation);
    }

    /**
     * Undo the paste command by restoring the old image and state.
     */
    @Override
    public void undo() {
        perspective.setImage(oldImage);
        perspective.setScaleFactor(oldScaleFactor);
        perspective.setTranslation(oldTranslation);
    }
}
