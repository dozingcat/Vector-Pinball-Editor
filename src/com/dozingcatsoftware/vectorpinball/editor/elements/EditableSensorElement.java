package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

public class EditableSensorElement extends EditableFieldElement {

    public static final String CENTER_PROPERTY = "center";
    public static final String RADIUS_PROPERTY = "radius";
    public static final String RECT_PROPERTY = "rect";

    static final Color EDITOR_OUTLINE_COLOR = Color.fromRGB(128, 128, 128);

    // Use these even for circular sensors, we can derive center and radius from them.
    double xmin, ymin, xmax, ymax;

    boolean isCircular() {
        return hasProperty(CENTER_PROPERTY) && hasProperty(RADIUS_PROPERTY);
    }

    @Override protected void refreshInternalValues() {
        if (isCircular()) {
            List<Object> cpos = (List<Object>)getProperty(CENTER_PROPERTY);
            double cx = asDouble(cpos.get(0));
            double cy = asDouble(cpos.get(1));
            double radius = asDouble(getProperty(RADIUS_PROPERTY));
            xmin = cx - radius/2;
            xmax = cx + radius/2;
            ymin = cy - radius/2;
            ymax = cy + radius/2;
        }
        else {
            List<Object> rect = (List<Object>)getProperty(RECT_PROPERTY);
            xmin = asDouble(rect.get(0));
            ymin = asDouble(rect.get(1));
            xmax = asDouble(rect.get(2));
            ymax = asDouble(rect.get(3));
        }
    }

    @Override protected void addPropertiesForNewElement(Map<String, Object> props, EditableField field) {
        props.put(RECT_PROPERTY, Arrays.asList("-0.5", "-0.5", "0", "0"));
    }

    @Override public void drawForEditor(IFieldRenderer renderer, boolean isSelected) {
        refreshIfDirty();
        if (isCircular()) {
            double cx = (xmin+xmax) / 2;
            double cy = (ymin+ymax) / 2;
            double radius = xmax - cx;
            renderer.frameCircle(cx, cy, radius, EDITOR_OUTLINE_COLOR);
        }
        else {
            renderer.drawLine(xmin, ymin, xmax, ymin, EDITOR_OUTLINE_COLOR);
            renderer.drawLine(xmax, ymin, xmax, ymax, EDITOR_OUTLINE_COLOR);
            renderer.drawLine(xmax, ymax, xmin, ymax, EDITOR_OUTLINE_COLOR);
            renderer.drawLine(xmin, ymax, xmin, ymin, EDITOR_OUTLINE_COLOR);
        }
        // TODO: indicate selection
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        refreshIfDirty();
        // Always treat as rectangle, require click inside and ignore distance.
        return (point.x>=xmin && point.x<=xmax && point.y>=ymin && point.y<=ymax);
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
        refreshIfDirty();
        if (isCircular()) {
            List<Object> cpos = (List<Object>)getProperty(CENTER_PROPERTY);
            setProperty(CENTER_PROPERTY, Arrays.asList(
                    asDouble(cpos.get(0)) + deltaFromPrevious.x,
                    asDouble(cpos.get(1)) + deltaFromPrevious.y));
        }
        else {
            List<Object> rect = (List<Object>)getProperty(RECT_PROPERTY);
            setProperty(RECT_PROPERTY, Arrays.asList(
                    asDouble(rect.get(0)) + deltaFromPrevious.x,
                    asDouble(rect.get(1)) + deltaFromPrevious.y,
                    asDouble(rect.get(2)) + deltaFromPrevious.x,
                    asDouble(rect.get(3)) + deltaFromPrevious.y));
        }
    }

}
