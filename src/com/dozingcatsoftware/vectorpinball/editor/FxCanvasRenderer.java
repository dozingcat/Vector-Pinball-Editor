package com.dozingcatsoftware.vectorpinball.editor;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;

import com.dozingcatsoftware.vectorpinball.elements.FieldElement;
import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;

public class FxCanvasRenderer implements IFieldRenderer {

    private Canvas canvas;
    private GraphicsContext context;
    private Field field;

    private double scale = 20;
    private double xOffset = -2;
    private double yOffset = -2;

    public void setCanvas(Canvas c) {
        canvas = c;
        context = c.getGraphicsContext2D();
    }

    public void setField(Field f) {
        field = f;
    }

    static Paint toFxPaint(Color color) {
        return javafx.scene.paint.Color.rgb(color.red, color.green, color.blue);
    }

    double worldToPixelX(double x) {
        return scale * (x-xOffset);
    }

    double worldToPixelY(double y) {
        return canvas.getHeight() - (scale * (y-yOffset));
    }

    double worldToPixelDistance(double dist) {
        return scale * dist;
    }

    @Override public void drawLine(float x1, float y1, float x2, float y2, Color color) {
        context.setStroke(toFxPaint(color));
        context.beginPath();
        context.moveTo(worldToPixelX(x1), worldToPixelY(y1));
        context.lineTo(worldToPixelX(x2), worldToPixelY(y2));
        context.stroke();
    }

    @Override public void fillCircle(float cx, float cy, float radius, Color color) {
        context.setFill(toFxPaint(color));
        context.fillArc(worldToPixelX(cx - radius), worldToPixelY(cy + radius),
                worldToPixelDistance(radius*2), worldToPixelDistance(radius*2), 0, 360, ArcType.OPEN);
    }

    @Override public void frameCircle(float cx, float cy, float radius, Color color) {
        context.setStroke(toFxPaint(color));
        context.strokeArc(worldToPixelX(cx - radius), worldToPixelY(cy + radius),
                worldToPixelDistance(radius*2), worldToPixelDistance(radius*2), 0, 360, ArcType.OPEN);
    }

    @Override public void doDraw() {
        for (FieldElement elem : field.getFieldElements()) {
            elem.draw(this);
        }
    }

    @Override public int getWidth() {
        return (int)canvas.getWidth();
    }

    @Override public int getHeight() {
        return (int)canvas.getHeight();
    }

}
