package com.etslabs.Views;

import com.etslabs.Models.Perspective;
import com.etslabs.Interfaces.Observer;
import com.etslabs.Commands.CommandManager;
import com.etslabs.Commands.TranslateCommand;
import com.etslabs.Commands.ZoomCommand;
import com.etslabs.Converter.ImageConverter;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public class PerspectiveView extends JPanel implements Observer {
    private final Perspective perspective;
    private Point lastMousePosition;

    public PerspectiveView(Perspective perspective) {
        this.perspective = perspective;
        this.perspective.addObserver(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePosition = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point currentPosition = e.getPoint();
                double offsetX = currentPosition.x - lastMousePosition.x;
                double offsetY = currentPosition.y - lastMousePosition.y;
        
                CommandManager.getInstance().executeCommand(new TranslateCommand(perspective, offsetX, offsetY));
                
                lastMousePosition = currentPosition; 
            }
        });        

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double scaleFactor = perspective.getScaleFactor();
                double delta = 0.1 * e.getPreciseWheelRotation();
                double newScaleFactor = scaleFactor - delta;
                newScaleFactor = Math.max(0.1, Math.min(newScaleFactor, 10.0)); 
                CommandManager.getInstance().executeCommand(new ZoomCommand(perspective, newScaleFactor));
            }
        });
    }

    @Override
    public void update() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        javafx.scene.image.Image fxImage = perspective.getTransformedImage(); 
        if (fxImage != null) {
            BufferedImage bufferedImage = ImageConverter.writableImageToBufferedImage(fxImage); 
            g.drawImage(bufferedImage, 0, 0, getWidth(), getHeight(), null); 
        }
    }
}
