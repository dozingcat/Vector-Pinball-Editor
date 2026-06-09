package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.TAU;

import java.util.Arrays;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.editor.IEditableFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.editor.Point;

public class EditableFlipperElement extends EditableFieldElement {

    public static final String POSITION_PROPERTY = "position";
    public static final String LENGTH_PROPERTY = "length";
    public static final String MIN_ANGLE_PROPERTY = "minangle";
    public static final String MAX_ANGLE_PROPERTY = "maxangle";
    public static final String UP_SPEED_PROPERTY = "upspeed";
    public static final String DOWN_SPEED_PROPERTY = "downspeed";

    static final int DEFAULT_COLOR = Color.fromRGB(0, 255, 0);

    @Override protected void addPropertiesForNewElement(Map<String, Object> props, EditableField field) {
        props.put(POSITION_PROPERTY, Arrays.asList(0, 0));
        props.put(LENGTH_PROPERTY, "2.5");
        props.put(MIN_ANGLE_PROPERTY, "-20");
        props.put(MAX_ANGLE_PROPERTY, "20");
        props.put(UP_SPEED_PROPERTY, "7");
        props.put(DOWN_SPEED_PROPERTY, "3");
    }

    private double cx() {
        return getListDoubleProperty(POSITION_PROPERTY, 0);
    }

    private double cy() {
        return getListDoubleProperty(POSITION_PROPERTY, 1);
    }

    // Negative if flipper rotates around its right end.
    private double flipperLength() {
        return getDoubleProperty(LENGTH_PROPERTY);
    }

    private double minAngle() {
        return Math.toRadians(getDoubleProperty(MIN_ANGLE_PROPERTY));
    }

    double endX() {
        double length = flipperLength();
        if (length > 0) {
            return cx() + length*Math.cos(minAngle());
        }
        else {
            return cx() - length*Math.cos(TAU/2 - minAngle());
        }
    }

    double endY() {
        double length = flipperLength();
        if (length > 0) {
            return cy() + length*Math.sin(minAngle());
        }
        else {
            return cy() - length*Math.sin(TAU/2 - minAngle());
        }
    }

    @Override public void drawForEditor(IEditableFieldRenderer renderer, boolean isSelected) {
        double cx = cx(), cy = cy();
        int color = currentColor(DEFAULT_COLOR);
        renderer.drawLine(cx, cy, endX(), endY(), color);
        if (isSelected) {
            int colorWithAlpha = Color.withAlpha(color, Color.getAlpha(color) / 2);
            renderer.fillCircle(cx, cy, 0.35*renderer.getRelativeScale(), colorWithAlpha);
            renderer.fillCircle(endX(), endY(), 0.15*renderer.getRelativeScale(), colorWithAlpha);
        }
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        return point.distanceToLineSegment(cx(), cy(), endX(), endY()) <= distance;
    }

    @Override public void translate(Point offset) {
        setProperty(POSITION_PROPERTY, Arrays.asList(cx() + offset.x, cy() + offset.y));
    }

}
