package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

public class EditableBumperElement extends EditableFieldElement {
    public static final String POSITION_PROPERTY = "position";
    public static final String RADIUS_PROPERTY = "radius";
    public static final String KICK_PROPERTY = "kick";

    static final Color DEFAULT_COLOR = Color.fromRGB(0, 0, 255);

    double cx, cy;
    double radius;
    Color color;

    @Override protected void refreshInternalValues() {
        List<Object> pos = (List<Object>)getProperty(POSITION_PROPERTY);
        this.cx = asDouble(pos.get(0));
        this.cy = asDouble(pos.get(1));
        this.radius = asDouble(getProperty(RADIUS_PROPERTY));
        this.color = currentColor(DEFAULT_COLOR);
    }

    @Override protected void addPropertiesForNewElement(Map<String, Object> props, EditableField field) {
        props.put(POSITION_PROPERTY, Arrays.asList("-0.5", "-0.5"));
        props.put(RADIUS_PROPERTY, "0.5");
        props.put(KICK_PROPERTY, "1.0");
    }

    @Override public void drawForEditor(IFieldRenderer renderer, boolean isSelected) {
        refreshIfDirty();

        renderer.fillCircle(cx, cy, radius, currentColor(DEFAULT_COLOR));
        if (isSelected) {
            renderer.drawLine(cx - radius, cy - radius, cx + radius, cy - radius, color);
            renderer.drawLine(cx + radius, cy - radius, cx + radius, cy + radius, color);
            renderer.drawLine(cx + radius, cy + radius, cx - radius, cy + radius, color);
            renderer.drawLine(cx - radius, cy + radius, cx - radius, cy - radius, color);
        }
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        refreshIfDirty();
        // Ignore distance, just require clicking on circle;
        return point.distanceTo(cx, cy) <= this.radius;
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
         // TODO: handle resizing as well as moving.
        List<Object> pos = (List<Object>)getProperty(POSITION_PROPERTY);
        setProperty(POSITION_PROPERTY, Arrays.asList(
                asDouble(pos.get(0)) + deltaFromPrevious.x,
                asDouble(pos.get(1)) + deltaFromPrevious.y));
    }

}
