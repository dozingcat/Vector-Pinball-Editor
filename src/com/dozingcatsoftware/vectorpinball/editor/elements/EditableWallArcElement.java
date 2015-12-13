package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;
import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asInt;
import static com.dozingcatsoftware.vectorpinball.util.MathUtils.toRadians;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

public class EditableWallArcElement extends EditableFieldElement {

    public static final String CENTER_PROPERTY = "center";
    public static final String RADIUS_PROPERTY = "radius";
    public static final String X_RADIUS_PROPERTY = "xradius";
    public static final String Y_RADIUS_PROPERTY = "yradius";
    public static final String NUM_SEGMENTS_PROPERTY = "segments";
    public static final String MIN_ANGLE_PROPERTY = "minangle";
    public static final String MAX_ANGLE_PROPERTY = "maxangle";
    public static final String IGNORE_BALL_PROPERTY = "ignoreBall";

    double[][] lineSegments;

    @Override protected void refreshInternalValues() {
        List<?> centerPos = (List<?>)getProperty(CENTER_PROPERTY);
        double centerX = asDouble(centerPos.get(0));
        double centerY = asDouble(centerPos.get(1));
        double minAngle = toRadians(asDouble(getProperty(MIN_ANGLE_PROPERTY)));
        double maxAngle = toRadians(asDouble(getProperty(MAX_ANGLE_PROPERTY)));
        int numSegments = asInt(getProperty(NUM_SEGMENTS_PROPERTY), 5);

        double xRadius, yRadius;
        if (hasProperty(RADIUS_PROPERTY)) {
            xRadius = yRadius = asDouble(getProperty(RADIUS_PROPERTY));
        }
        else {
            xRadius = asDouble(getProperty(X_RADIUS_PROPERTY));
            yRadius = asDouble(getProperty(Y_RADIUS_PROPERTY));
        }

        double diff = maxAngle - minAngle;
        // Create line segments to approximate circular arc.
        lineSegments = new double[numSegments][];
        for(int i=0; i<numSegments; i++) {
            double angle1 = minAngle + i * diff / numSegments;
            double angle2 = minAngle + (i+1) * diff / numSegments;
            double x1 = centerX + xRadius * (float)Math.cos(angle1);
            double y1 = centerY + yRadius * (float)Math.sin(angle1);
            double x2 = centerX + xRadius * (float)Math.cos(angle2);
            double y2 = centerY + yRadius * (float)Math.sin(angle2);
            lineSegments[i] = (new double[] {x1, y1, x2, y2});
        }
    }

    @Override protected void addPropertiesForNewElement(Map<String, Object> props, EditableField field) {
        props.put(RADIUS_PROPERTY, "0.5");
        props.put(CENTER_PROPERTY, Arrays.asList("-0.5", "0"));
        props.put(MIN_ANGLE_PROPERTY, "0");
        props.put(MAX_ANGLE_PROPERTY, "135");
    }

    @Override public void drawForEditor(IFieldRenderer renderer, boolean isSelected) {
        refreshIfDirty();
        Color color = currentColor(DEFAULT_WALL_COLOR);
        for (double[] segment : this.lineSegments) {
            renderer.drawLine(segment[0], segment[1], segment[2], segment[3], color);
        }
        if (isSelected) {
            double endpointRadius = 0.25 / renderer.getRelativeScale();
            renderer.fillCircle(lineSegments[0][0], lineSegments[0][1], endpointRadius, color);
            double[] last = lineSegments[lineSegments.length-1];
            renderer.fillCircle(last[2], last[3], endpointRadius, color);

            Color colorWithAlpha = Color.fromRGB(color.red, color.green, color.blue, color.alpha/2);
            List<?> centerPos = (List<?>)getProperty(CENTER_PROPERTY);
            double cx = asDouble(centerPos.get(0));
            double cy = asDouble(centerPos.get(1));
            renderer.fillCircle(cx, cy, endpointRadius, colorWithAlpha);
            renderer.drawLine(cx, cy, lineSegments[0][0], lineSegments[0][1], colorWithAlpha);
            renderer.drawLine(cx, cy, last[2], last[3], colorWithAlpha);
        }
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        refreshIfDirty();
        for (double[] segment : this.lineSegments) {
            if (point.distanceToLineSegment(segment[0], segment[1], segment[2], segment[3]) <= distance) {
                return true;
            }
        }
        return false;
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
        List<?> cpos = (List<?>)getProperty(CENTER_PROPERTY);
        setProperty(CENTER_PROPERTY, Arrays.asList(
                asDouble(cpos.get(0)) + deltaFromPrevious.x,
                asDouble(cpos.get(1)) + deltaFromPrevious.y));
    }

}
