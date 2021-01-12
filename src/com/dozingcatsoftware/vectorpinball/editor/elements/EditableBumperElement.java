package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.editor.IEditableFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.editor.Point;

public class EditableBumperElement extends EditableFieldElement {
    public static final String POSITION_PROPERTY = "position";
    public static final String RADIUS_PROPERTY = "radius";
    public static final String KICK_PROPERTY = "kick";
    public static final String OUTER_RADIUS_PROPERTY = "outerRadius";
    public static final String OUTER_COLOR_PROPERTY = "outerColor";
    public static final String INACTIVE_LAYER_OUTER_COLOR_PROPERTY = "inactiveLayerOuterColor";

    static final int DEFAULT_COLOR = Color.fromRGB(0, 0, 255);
    static final int DEFAULT_OUTER_COLOR = Color.fromRGBA(0, 0, 255, 128);

    double cx, cy;
    double radius, outerRadius;
    int color;
    Integer outerColor;

    @Override protected void refreshInternalValues() {
        List<Object> pos = (List<Object>)getProperty(POSITION_PROPERTY);
        this.cx = asDouble(pos.get(0));
        this.cy = asDouble(pos.get(1));
        this.radius = asDouble(getProperty(RADIUS_PROPERTY));
        this.outerRadius = asDouble(getProperty(OUTER_RADIUS_PROPERTY));
        this.color = currentColor(DEFAULT_COLOR);
        this.outerColor = colorForDisplay(getProperty(OUTER_COLOR_PROPERTY) != null ?
                Color.fromList((List<Number>)getProperty(OUTER_COLOR_PROPERTY)) :
                DEFAULT_OUTER_COLOR);

    }

    @Override protected void addPropertiesForNewElement(Map<String, Object> props, EditableField field) {
        props.put(POSITION_PROPERTY, Arrays.asList("-0.5", "-0.5"));
        props.put(RADIUS_PROPERTY, "0.5");
        props.put(KICK_PROPERTY, "1.0");
    }

    @Override public void drawForEditor(IEditableFieldRenderer renderer, boolean isSelected) {
        refreshIfDirty();

        double maxRad = Math.max(this.radius, this.outerRadius);
        if (this.outerRadius > 0) {
            renderer.fillCircle(cx, cy, outerRadius, this.outerColor);
        }
        renderer.fillCircle(cx, cy, radius, currentColor(DEFAULT_COLOR));
        if (isSelected) {
            renderer.drawLine(cx - maxRad, cy - maxRad, cx + maxRad, cy - maxRad, color);
            renderer.drawLine(cx + maxRad, cy - maxRad, cx + maxRad, cy + maxRad, color);
            renderer.drawLine(cx + maxRad, cy + maxRad, cx - maxRad, cy + maxRad, color);
            renderer.drawLine(cx - maxRad, cy + maxRad, cx - maxRad, cy - maxRad, color);
        }
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        refreshIfDirty();
        // Ignore distance, just require clicking on circle.
        double dist = point.distanceTo(cx, cy);
        return dist <= this.radius || dist <= this.outerRadius;
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
         // TODO: handle resizing as well as moving.
        List<Object> pos = (List<Object>)getProperty(POSITION_PROPERTY);
        setProperty(POSITION_PROPERTY, Arrays.asList(
                asDouble(pos.get(0)) + deltaFromPrevious.x,
                asDouble(pos.get(1)) + deltaFromPrevious.y));
    }

}
