package com.etslabs.Models;

import javafx.scene.image.Image;
import com.etslabs.Interfaces.Observer;
import java.util.ArrayList;
import java.util.List;

public class ImageModel {
    private Image image;
    private final List<Observer> observers = new ArrayList<>();

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        notifyObservers();
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
