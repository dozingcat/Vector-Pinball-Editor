package com.dozingcatsoftware.vectorpinball.editor;

import javafx.scene.canvas.Canvas;

import com.dozingcatsoftware.vectorpinball.model.Color;

public class FieldCanvas extends Canvas {
    public FieldCanvas() {
        super(500, 500);
        widthProperty().addListener(event -> draw());
        heightProperty().addListener(event -> draw());
    }

    public void draw() {
        System.out.println("draw: " + getWidth() + " : " + getHeight());
        FxCanvasRenderer renderer = new FxCanvasRenderer();
        renderer.setCanvas(this);
        renderer.drawLine(10, 10, 100, 20, Color.fromRGB(255, 0, 0));
        renderer.fillCircle(50, 100, 20, Color.fromRGB(0, 255, 0));
        renderer.frameCircle(100, 100, 20, Color.fromRGB(0, 0, 255));
    }

    @Override public boolean isResizable() {
        return true;
    }

    @Override public double prefWidth(double width) {
        return getWidth();
    }

    @Override public double prefHeight(double height) {
        return getHeight();
    }
}
