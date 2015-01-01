package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;
import static com.dozingcatsoftware.vectorpinball.util.MathUtils.toRadians;

import java.util.Arrays;
import java.util.List;

import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

public class EditableFlipperElement extends EditableFieldElement {

    public static final String POSITION_PROPERTY = "position";
    public static final String LENGTH_PROPERTY = "length";
    public static final String MIN_ANGLE_PROPERTY = "minangle";
    public static final String MAX_ANGLE_PROPERTY = "maxangle";
    public static final String UP_SPEED_PROPERTY = "upspeed";
    public static final String DOWN_SPEED_PROPERTY = "downspeed";

    double flipperLength; // negative if flipper rotates around its right end
    double minangle, maxangle;
    double cx, cy;

    @Override protected void refreshInternalValues() {
        List pos = (List)getProperty(POSITION_PROPERTY);

        this.cx = asDouble(pos.get(0));
        this.cy = asDouble(pos.get(1));
        this.flipperLength = asDouble(getProperty(LENGTH_PROPERTY));
        this.minangle = toRadians(asDouble(getProperty(MIN_ANGLE_PROPERTY)));
        this.maxangle = toRadians(asDouble(getProperty(MAX_ANGLE_PROPERTY)));
    }

    @Override public void drawForEditor(IFieldRenderer renderer, boolean isSelected) {
        refreshIfDirty();
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        refreshIfDirty();
        Point endPoint = Point.fromXY(
                cx + flipperLength*Math.cos(minangle),
                cy + flipperLength*Math.sin(minangle));
        return point.distanceToLineSegment(Point.fromXY(cx, cy), endPoint) <= distance;
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
        List<Object> pos = (List<Object>)getProperty(POSITION_PROPERTY);
        setProperty(POSITION_PROPERTY, Arrays.asList(
                asDouble(pos.get(0)) + deltaFromPrevious.x,
                asDouble(pos.get(1)) + deltaFromPrevious.y));
    }

}
