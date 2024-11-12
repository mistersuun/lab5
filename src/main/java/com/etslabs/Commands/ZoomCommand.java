package com.etslabs.Commands;

import com.etslabs.Interfaces.Command;
import com.etslabs.Models.Perspective;

public class ZoomCommand implements Command {
    private final Perspective perspective;
    private final double oldScaleFactor;
    private final double newScaleFactor;

    public ZoomCommand(Perspective perspective, double newScaleFactor) {
        this.perspective = perspective;
        this.oldScaleFactor = perspective.getScaleFactor(); 
        this.newScaleFactor = newScaleFactor;
    }

    @Override
    public void execute() {
        perspective.setScaleFactor(newScaleFactor); 
    }

    @Override
    public void undo() {
        perspective.setScaleFactor(oldScaleFactor); 
    }

    public double getOldScaleFactor() {
        return oldScaleFactor;
    }

    public double getNewScaleFactor() {
        return newScaleFactor;
    }
}
