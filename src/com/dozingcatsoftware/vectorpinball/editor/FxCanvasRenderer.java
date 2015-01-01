package com.dozingcatsoftware.vectorpinball.editor;

import java.util.HashSet;
import java.util.Set;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableField;
import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;
import com.dozingcatsoftware.vectorpinball.elements.FieldElement;
import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

public class FxCanvasRenderer implements IFieldRenderer {

    private Canvas canvas;
    private GraphicsContext context;
    private Field field;
    private EditableField editableField;

    private double scale = 30;
    private double xOffset = -2;
    private double yOffset = -2;

    Set<EditableFieldElement> selectedElements = new HashSet<>();
    Point dragStartPoint;
    Point lastDragPoint;

    public void setCanvas(Canvas c) {
        canvas = c;
        context = c.getGraphicsContext2D();
    }

    public void setField(Field f) {
        field = f;
        editableField = null;
    }

    public void setEditableField(EditableField f) {
        editableField = f;
        field = null;
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

    double pixelToWorldX(double x) {
        return x/scale + xOffset;
    }

    double pixelToWorldY(double y) {
        return (canvas.getHeight()-y)/scale + yOffset;
    }

    double worldToPixelDistance(double dist) {
        return scale * dist;
    }

    @Override public void drawLine(double x1, double y1, double x2, double y2, Color color) {
        context.setStroke(toFxPaint(color));
        context.beginPath();
        context.moveTo(worldToPixelX(x1), worldToPixelY(y1));
        context.lineTo(worldToPixelX(x2), worldToPixelY(y2));
        context.stroke();
    }
    @Override public void drawLine(float x1, float y1, float x2, float y2, Color color) {
        drawLine((double)x1, y1, x2, y2, color);
    }

    @Override public void fillCircle(double cx, double cy, double radius, Color color) {
        context.setFill(toFxPaint(color));
        context.fillArc(worldToPixelX(cx - radius), worldToPixelY(cy + radius),
                worldToPixelDistance(radius*2), worldToPixelDistance(radius*2), 0, 360, ArcType.OPEN);
    }
    @Override public void fillCircle(float cx, float cy, float radius, Color color) {
        fillCircle(cx, cy, radius, color);
    }

    @Override public void frameCircle(double cx, double cy, double radius, Color color) {
        context.setStroke(toFxPaint(color));
        context.strokeArc(worldToPixelX(cx - radius), worldToPixelY(cy + radius),
                worldToPixelDistance(radius*2), worldToPixelDistance(radius*2), 0, 360, ArcType.OPEN);
    }
    @Override public void frameCircle(float cx, float cy, float radius, Color color) {
        frameCircle(cx, cy, radius, color);
    }

    @Override public void doDraw() {
        if (Platform.isFxApplicationThread()) {
            draw();
        }
        else {
            Platform.runLater(this::draw);
        }
    }

    void draw() {
        context.setFill(javafx.scene.paint.Color.BLACK);
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (editableField != null) {
            for (EditableFieldElement elem : editableField.getElements()) {
                elem.drawForEditor(this, isElementSelected(elem));
            }
        }
        else if (field != null) {
            synchronized(field) {
                for (FieldElement elem : field.getFieldElements()) {
                    elem.draw(this);
                }
                field.drawBalls(this);
            }
        }
    }

    boolean isElementSelected(EditableFieldElement element) {
        return selectedElements.contains(element);
    }

    @Override public int getWidth() {
        return (int)canvas.getWidth();
    }

    @Override public int getHeight() {
        return (int)canvas.getHeight();
    }

    @Override public boolean canDraw() {
        return true;
    }

    @Override public void setDebugMessage(String debugInfo) {

    }

    Point worldPointFromEvent(MouseEvent event) {
        return Point.fromXY(pixelToWorldX(event.getX()), pixelToWorldY(event.getY()));
    }

    void handleEditorMouseDown(MouseEvent event) {
        System.out.println("mouseDown: " + pixelToWorldX(event.getX()) + "," + pixelToWorldY(event.getY()));
        Point worldPoint = worldPointFromEvent(event);
        selectedElements.clear();
        if (editableField == null) return;
        for (EditableFieldElement elem : editableField.getElements()) {
            if (elem.isPointWithinDistance(worldPoint, 10.0/scale)) {
                selectedElements.add(elem);
                elem.startDrag(worldPoint);
                break;
            }
        }
        dragStartPoint = (selectedElements.isEmpty()) ? null : worldPoint;
        lastDragPoint = null;
        draw();
    }

    void handleEditorMouseDrag(MouseEvent event) {
        if (editableField != null && !selectedElements.isEmpty() && dragStartPoint!=null) {
            Point worldPoint = worldPointFromEvent(event);
            Point totalDragOffset = worldPoint.subtract(dragStartPoint);
            Point previousDragOffset = worldPoint.subtract((lastDragPoint!=null) ? lastDragPoint : dragStartPoint);
            for (EditableFieldElement elem : selectedElements) {
                elem.handleDrag(dragStartPoint, totalDragOffset, previousDragOffset);
            }
            lastDragPoint = worldPoint;
            draw();
        }
    }

    void handleEditorMouseUp(MouseEvent event) {
        System.out.println("mouseUp: " + pixelToWorldX(event.getX()) + "," + pixelToWorldY(event.getY()));
    }
}
