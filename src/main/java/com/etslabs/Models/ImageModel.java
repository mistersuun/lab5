package com.etslabs.Models;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.etslabs.Converter.ImageConverter;
import com.etslabs.Interfaces.Observer;

import javafx.scene.image.Image;

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

    public void loadImageFromFile(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage argbImage = new BufferedImage(
                        bufferedImage.getWidth(),
                        bufferedImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );
                argbImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
                bufferedImage = argbImage;
            }
            Image fxImage = ImageConverter.bufferedImageToWritableImage(bufferedImage);
            setImage(fxImage);
            System.out.println("Image loaded successfully: " + fxImage);
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
            e.printStackTrace();
        }
    }    
}
