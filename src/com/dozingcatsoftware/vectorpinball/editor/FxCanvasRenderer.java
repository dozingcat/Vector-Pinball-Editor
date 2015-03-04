package com.dozingcatsoftware.vectorpinball.editor;

import java.util.ArrayList;
import java.util.List;

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

    static final double DEFAULT_SCALE = 25;
    // Zoom levels greater than 2 result in poor performance using the simple
    // approach of creating a larger canvas.
    static final double[] SCALE_RATIOS = {1.0/2, 3.0/4, 1.0, 3.0/2, 2.0};
    static final int DEFAULT_SCALE_RATIO_INDEX = 2;

    private Canvas canvas;
    private GraphicsContext context;
    private Field field;
    private EditableField editableField;
    private UndoStack undoStack;

    private double scale = DEFAULT_SCALE;
    private int scaleRatioIndex = DEFAULT_SCALE_RATIO_INDEX;
    private double xOffset = -1.5;
    private double yOffset = -1.5;

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

    public void setUndoStack(UndoStack stack) {
        undoStack = stack;
    }

    static Paint toFxPaint(Color color) {
        return javafx.scene.paint.Color.rgb(color.red, color.green, color.blue, color.alpha/255.0);
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
        fillCircle((double)cx, (double)cy, radius, color);
    }

    @Override public void frameCircle(double cx, double cy, double radius, Color color) {
        context.setStroke(toFxPaint(color));
        context.strokeArc(worldToPixelX(cx - radius), worldToPixelY(cy + radius),
                worldToPixelDistance(radius*2), worldToPixelDistance(radius*2), 0, 360, ArcType.OPEN);
    }
    @Override public void frameCircle(float cx, float cy, float radius, Color color) {
        frameCircle((double)cx, (double)cy, radius, color);
    }

    @Override public void fillPolygon(double[] xPoints, double[] yPoints, Color color) {
        double[] pixelX = new double[xPoints.length];
        double[] pixelY = new double[yPoints.length];
        for (int i=0; i<xPoints.length; i++) {
            pixelX[i] = worldToPixelX(xPoints[i]);
            pixelY[i] = worldToPixelY(yPoints[i]);
        }
        context.setFill(toFxPaint(color));
        context.fillPolygon(pixelX, pixelY, pixelX.length);
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
                elem.drawForEditor(this, editableField.isElementSelected(elem));
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

    @Override public double getRelativeScale() {
        return SCALE_RATIOS[scaleRatioIndex];
    }

    public void zoomIn() {
        if (scaleRatioIndex < SCALE_RATIOS.length-1) {
            scaleRatioIndex++;
            scale = SCALE_RATIOS[scaleRatioIndex] * DEFAULT_SCALE;
        }
    }

    public void zoomOut() {
        if (scaleRatioIndex > 0) {
            scaleRatioIndex--;
            scale = SCALE_RATIOS[scaleRatioIndex] * DEFAULT_SCALE;
        }
    }

    public void zoomDefault() {
        scaleRatioIndex = DEFAULT_SCALE_RATIO_INDEX;
        scale = SCALE_RATIOS[scaleRatioIndex] * DEFAULT_SCALE;
    }

    Point worldPointFromEvent(MouseEvent event) {
        return Point.fromXY(pixelToWorldX(event.getX()), pixelToWorldY(event.getY()));
    }

    EditableFieldElement findClickTarget(Point worldPoint) {
        // Give priority to already-selected elements.
        if (editableField.hasSelection()) {
            for (EditableFieldElement elem : editableField.getSelectedElements()) {
                if (elem.isPointWithinDistance(worldPoint, 10.0/scale)) {
                    return elem;
                }
            }
        }
        for (EditableFieldElement elem : editableField.getElements()) {
            if (elem.isPointWithinDistance(worldPoint, 10.0/scale)) {
                return elem;
            }
        }
        return null;
    }

    void handleEditorMouseDown(MouseEvent event) {
        Point worldPoint = worldPointFromEvent(event);
        List<EditableFieldElement> selected = new ArrayList<>();
        if (editableField == null) return;
        EditableFieldElement clickedElement = findClickTarget(worldPoint);
        if (clickedElement != null) {
            selected.add(clickedElement);
            clickedElement.startDrag(worldPoint);
        }
        editableField.setSelectedElements(selected);
        dragStartPoint = (selected.isEmpty()) ? null : worldPoint;
        lastDragPoint = null;
        draw();
    }

    void handleEditorMouseDrag(MouseEvent event) {
        if (editableField != null && editableField.hasSelection() && dragStartPoint!=null) {
            Point worldPoint = worldPointFromEvent(event);
            Point totalDragOffset = worldPoint.subtract(dragStartPoint);
            Point previousDragOffset = worldPoint.subtract((lastDragPoint!=null) ? lastDragPoint : dragStartPoint);
            for (EditableFieldElement elem : editableField.getSelectedElements()) {
                elem.handleDrag(dragStartPoint, totalDragOffset, previousDragOffset);
            }
            lastDragPoint = worldPoint;
            draw();
        }
    }

    void handleEditorMouseUp(MouseEvent event) {
        if (editableField != null && editableField.hasSelection() && dragStartPoint!=null) {
            undoStack.pushSnapshot();
        }
    }
}
