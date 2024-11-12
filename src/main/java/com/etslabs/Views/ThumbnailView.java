package com.etslabs.Views;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.etslabs.Converter.ImageConverter;
import com.etslabs.Interfaces.Observer;
import com.etslabs.Models.ImageModel;

public class ThumbnailView extends JPanel implements Observer {
    private final ImageModel imageModel;

    public ThumbnailView(ImageModel imageModel) {
        this.imageModel = imageModel;
        this.imageModel.addObserver(this);
    }

    @Override
    public void update() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        javafx.scene.image.Image fxImage = imageModel.getImage(); 
        if (fxImage != null) {
            BufferedImage bufferedImage = ImageConverter.writableImageToBufferedImage(fxImage); 
            g.drawImage(bufferedImage, 0, 0, getWidth(), getHeight(), null); 
        }
    }
}
