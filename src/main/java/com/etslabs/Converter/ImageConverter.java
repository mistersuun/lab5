package com.etslabs.Converter;

import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelFormat;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javafx.scene.image.PixelReader;

public class ImageConverter {

    public static WritableImage bufferedImageToWritableImage(BufferedImage bufferedImage) {
        WritableImage writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        int[] rgbData = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();

        pixelWriter.setPixels(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(),
                PixelFormat.getIntArgbInstance(), rgbData, 0, bufferedImage.getWidth());
        return writableImage;
    }

    public static BufferedImage writableImageToBufferedImage(javafx.scene.image.Image fxImage) {
        int width = (int) fxImage.getWidth();
        int height = (int) fxImage.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        PixelReader pixelReader = fxImage.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, pixelReader.getArgb(x, y));
            }
        }
        return bufferedImage;
    }
}
