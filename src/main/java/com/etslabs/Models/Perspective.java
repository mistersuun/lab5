package com.etslabs.Models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.etslabs.Interfaces.Observer;

import javafx.scene.image.Image;

public class Perspective {
    private double scaleFactor = 1.0;
    private Point translation = new Point(0, 0);
    private final ImageModel imageModel;
    private final List<Observer> observers = new ArrayList<>();

    public Perspective(ImageModel imageModel) {
        this.imageModel = imageModel;
        // Register as an observer to ImageModel to observe image changes
        this.imageModel.addObserver(() -> {
            System.out.println("Perspective: ImageModel changed, notifying observers.");
            notifyObservers();
        });
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        notifyObservers();
    }

    public void setTranslation(Point translation) {
        this.translation = translation;
        notifyObservers();
    }

    public Point getTranslation() {
        return translation;
    }

    public Image getTransformedImage() {
        return imageModel.getImage(); 
    }

    public ImageModel getImageModel() {
        return imageModel;
    }

    // Add this method to set the image
    public void setImage(Image image) {
        imageModel.setImage(image);
        // No need to call notifyObservers here because ImageModel already notifies, which triggers Perspective to notify
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
