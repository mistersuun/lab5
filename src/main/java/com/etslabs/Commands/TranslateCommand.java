package com.etslabs.Commands;

import java.awt.Point;
import com.etslabs.Interfaces.Command;
import com.etslabs.Models.Perspective;

public class TranslateCommand implements Command {
    private final Perspective perspective;
    private final Point oldTranslation;
    private final double offsetX;
    private final double offsetY;

    public TranslateCommand(Perspective perspective, double offsetX, double offsetY) {
        this.perspective = perspective;
        this.oldTranslation = perspective.getTranslation();
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void execute() {
        Point newTranslation = new Point(
                (int) (oldTranslation.getX() + offsetX),
                (int) (oldTranslation.getY() + offsetY)
        );
        perspective.setTranslation(newTranslation);
    }

    @Override
    public void undo() {
        perspective.setTranslation(oldTranslation);
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }
}
