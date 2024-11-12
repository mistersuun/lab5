package com.etslabs;

import java.io.Serializable;

public class AppState implements Serializable {
    private static final long serialVersionUID = 1L;
    public double scaleFactor;
    public String imageUrl;

    public AppState(double scaleFactor, String imageUrl) {
        this.scaleFactor = scaleFactor;
        this.imageUrl = imageUrl;
    }
}
