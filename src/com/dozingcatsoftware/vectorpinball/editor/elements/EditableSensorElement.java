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

    double xmin, ymin, xmax, ymax;

    @Override protected void refreshInternalValues() {
        List<Object> rect = (List<Object>)getProperty(RECT_PROPERTY);
        xmin = Math.min(asDouble(rect.get(0)), asDouble(rect.get(2)));
        xmax = Math.max(asDouble(rect.get(0)), asDouble(rect.get(2)));
        ymin = Math.min(asDouble(rect.get(1)), asDouble(rect.get(3)));
        ymax = Math.max(asDouble(rect.get(1)), asDouble(rect.get(3)));
    }

    @Override protected void addPropertiesForNewElement(Map<String, Object> props, EditableField field) {
        props.put(RECT_PROPERTY, Arrays.asList("-0.5", "-0.5", "0", "0"));
    }

    @Override public void drawForEditor(IEditableFieldRenderer renderer, boolean isSelected) {
        refreshIfDirty();
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
        refreshIfDirty();
        // Always treat as rectangle, require click inside and ignore distance.
        return (point.x>=xmin && point.x<=xmax && point.y>=ymin && point.y<=ymax);
    }

    @Override public void translate(Point offset) {
        refreshIfDirty();
        // Ideally this would support resizing by corners, but for now just drag.
        List<Object> rect = (List<Object>)getProperty(RECT_PROPERTY);
        setProperty(RECT_PROPERTY, Arrays.asList(
                asDouble(rect.get(0)) + offset.x,
                asDouble(rect.get(1)) + offset.y,
                asDouble(rect.get(2)) + offset.x,
                asDouble(rect.get(3)) + offset.y));
    }

}
