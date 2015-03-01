package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.TAU;
import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;
import static com.dozingcatsoftware.vectorpinball.util.MathUtils.toRadians;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

public class EditableFlipperElement extends EditableFieldElement {

    public static final String POSITION_PROPERTY = "position";
    public static final String LENGTH_PROPERTY = "length";
    public static final String MIN_ANGLE_PROPERTY = "minangle";
    public static final String MAX_ANGLE_PROPERTY = "maxangle";
    public static final String UP_SPEED_PROPERTY = "upspeed";
    public static final String DOWN_SPEED_PROPERTY = "downspeed";

    static final Color DEFAULT_COLOR = Color.fromRGB(0, 255, 0);

    double flipperLength; // negative if flipper rotates around its right end
    double minangle, maxangle;
    double cx, cy;

    @Override protected void refreshInternalValues() {
        List<?> pos = (List<?>)getProperty(POSITION_PROPERTY);

        this.cx = asDouble(pos.get(0));
        this.cy = asDouble(pos.get(1));
        this.flipperLength = asDouble(getProperty(LENGTH_PROPERTY));
        this.minangle = toRadians(asDouble(getProperty(MIN_ANGLE_PROPERTY)));
        this.maxangle = toRadians(asDouble(getProperty(MAX_ANGLE_PROPERTY)));
    }

    @Override protected void addPropertiesForNewElement(Map<String, Object> props, EditableField field) {
        props.put(POSITION_PROPERTY, Arrays.asList(0, 0));
        props.put(LENGTH_PROPERTY, "2.5");
        props.put(MIN_ANGLE_PROPERTY, "-20");
        props.put(MAX_ANGLE_PROPERTY, "20");
        props.put(UP_SPEED_PROPERTY, "7");
        props.put(DOWN_SPEED_PROPERTY, "3");
    }

    double endX() {
        if (flipperLength > 0) {
            return cx + flipperLength*Math.cos(minangle);
        }
        else {
            return cx - flipperLength*Math.cos(TAU/2 - minangle);
        }
    }

    double endY() {
        if (flipperLength > 0) {
            return cy + flipperLength*Math.sin(minangle);
        }
        else {
            return cy - flipperLength*Math.sin(TAU/2 - minangle);
        }
    }

    @Override public void drawForEditor(IFieldRenderer renderer, boolean isSelected) {
        refreshIfDirty();
        Color color = currentColor(DEFAULT_COLOR);
        renderer.drawLine(cx, cy, endX(), endY(), color);
        if (isSelected) {
            renderer.fillCircle(cx, cy, 0.35*renderer.getRelativeScale(), color);
            renderer.fillCircle(endX(), endY(), 0.15*renderer.getRelativeScale(), color);
        }
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        refreshIfDirty();
        return point.distanceToLineSegment(cx, cy, endX(), endY()) <= distance;
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
        List<?> pos = (List<?>)getProperty(POSITION_PROPERTY);
        setProperty(POSITION_PROPERTY, Arrays.asList(
                asDouble(pos.get(0)) + deltaFromPrevious.x,
                asDouble(pos.get(1)) + deltaFromPrevious.y));
    }

}
