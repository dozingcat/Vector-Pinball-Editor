package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.editor.IEditableFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.editor.Point;

public class EditableSensorElement extends EditableFieldElement {

    public static final String RECT_PROPERTY = "rect";
    public static final String BALL_LAYER_TO_PROPERTY = "ballLayer";
    public static final String BALL_LAYER_FROM_PROPERTY = "ballLayerFrom";
    public static final String RECORD_BALL_TIMES_PROPERTY = "recordBallTimes";

    static final int EDITOR_OUTLINE_COLOR = Color.fromRGB(128, 128, 128);
    static final int EDITOR_FILL_COLOR = Color.fromRGBA(128, 128, 128, 128);

    @Override protected void addPropertiesForNewElement(Map<String, Object> props, EditableField field) {
        props.put(RECT_PROPERTY, Arrays.asList("-0.5", "-0.5", "0", "0"));
    }

    /** Returns the normalized bounds as {xmin, ymin, xmax, ymax}. */
    private double[] bounds() {
        double xa = getListDoubleProperty(RECT_PROPERTY, 0);
        double ya = getListDoubleProperty(RECT_PROPERTY, 1);
        double xb = getListDoubleProperty(RECT_PROPERTY, 2);
        double yb = getListDoubleProperty(RECT_PROPERTY, 3);
        return new double[] {
                Math.min(xa, xb), Math.min(ya, yb), Math.max(xa, xb), Math.max(ya, yb)};
    }

    @Override public void drawForEditor(IEditableFieldRenderer renderer, boolean isSelected) {
        double[] b = bounds();
        double xmin = b[0], ymin = b[1], xmax = b[2], ymax = b[3];
        renderer.drawLine(xmin, ymin, xmax, ymin, EDITOR_OUTLINE_COLOR);
        renderer.drawLine(xmax, ymin, xmax, ymax, EDITOR_OUTLINE_COLOR);
        renderer.drawLine(xmax, ymax, xmin, ymax, EDITOR_OUTLINE_COLOR);
        renderer.drawLine(xmin, ymax, xmin, ymin, EDITOR_OUTLINE_COLOR);

        if (isSelected) {
            renderer.fillPolygon(
                    new double[] {xmin, xmin, xmax, xmax},
                    new double[] {ymin, ymax, ymax, ymin},
                    EDITOR_FILL_COLOR);
        }
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        // Always treat as rectangle, require click inside and ignore distance.
        double[] b = bounds();
        return (point.x>=b[0] && point.x<=b[2] && point.y>=b[1] && point.y<=b[3]);
    }

    @Override public void translate(Point offset) {
        // Ideally this would support resizing by corners, but for now just drag.
        List<Object> rect = (List<Object>)getProperty(RECT_PROPERTY);
        setProperty(RECT_PROPERTY, Arrays.asList(
                asDouble(rect.get(0)) + offset.x,
                asDouble(rect.get(1)) + offset.y,
                asDouble(rect.get(2)) + offset.x,
                asDouble(rect.get(3)) + offset.y));
    }

}
