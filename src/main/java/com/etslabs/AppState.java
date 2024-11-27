package com.etslabs;

import java.awt.Point;
import java.io.Serializable;
public class AppState implements Serializable {
    private static final long serialVersionUID = 1L;
    private double scaleFactor;
    private double translateX;
    private double translateY;
    private Point translation;

    public AppState(double scaleFactor, double translateX, double translateY, Point translation) {
        this.scaleFactor = scaleFactor;
        this.translateX = translateX;
        this.translateY = translateY;
        this.translation = translation;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public double getTranslateX() {
        return translateX;
    }

    public void setTranslateX(double translateX) {
        this.translateX = translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        this.translateY = translateY;
    }

    public Point getTranslation() {
        return translation;
    }

    public void setTranslation(Point translation) {
        this.translation = translation;
    }
}
