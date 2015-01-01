package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

public class EditableRolloverGroupElement extends EditableFieldElement {

    public static final String TOGGLE_OFF_PROPERTY = "toggleOff";
    public static final String CYCLE_ON_FLIPPER_PROPERTY = "cycleOnFlipper";
    public static final String IGNORE_BALL_PROPERTY = "ignoreBall";
    public static final String RADIUS_PROPERTY = "radius";
    public static final String RESET_DELAY_PROPERTY = "reset";
    public static final String ROLLOVERS_PROPERTY = "rollovers";
    // For individual rollovers.
    public static final String POSITION_PROPERTY = "position";
    public static final String COLOR_PROPERTY = "color";
    public static final String SCORE_PROPERTY = "score";

    static final Color DEFAULT_COLOR = Color.fromRGB(0, 255, 0);

    // This element doesn't use dirty checking.

    @Override public void drawForEditor(IFieldRenderer renderer, boolean isSelected) {
        Color groupColor = currentColor(DEFAULT_COLOR);
        double groupRadius = asDouble(getProperty(RADIUS_PROPERTY));
        List<Map<String, Object>> rolloverMaps = (List<Map<String, Object>>)getProperty(ROLLOVERS_PROPERTY);
        for (Map<String, Object> rmap : rolloverMaps) {
            List<Number> pos = (List<Number>)rmap.get(POSITION_PROPERTY);
            Color color = rmap.containsKey(COLOR_PROPERTY) ?
                    Color.fromList((List<Number>)rmap.get(COLOR_PROPERTY)) : groupColor;
            double radius = rmap.containsKey(RADIUS_PROPERTY) ?
                    asDouble(rmap.get(RADIUS_PROPERTY)) : groupRadius;
            renderer.frameCircle(asDouble(pos.get(0)), asDouble(pos.get(1)), radius, color);
        }
        // TODO: indicate selection
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        double groupRadius = asDouble(getProperty(RADIUS_PROPERTY));
        List<Map<String, Object>> rolloverMaps = (List<Map<String, Object>>)getProperty(ROLLOVERS_PROPERTY);
        for (Map<String, Object> rmap : rolloverMaps) {
            List<Number> pos = (List<Number>)rmap.get(POSITION_PROPERTY);
            double rx = asDouble(pos.get(0));
            double ry = asDouble(pos.get(1));
            double radius = rmap.containsKey(RADIUS_PROPERTY) ?
                    asDouble(rmap.get(RADIUS_PROPERTY)) : groupRadius;
            if (point.distanceTo(rx, ry) <= radius) {
                return true;
            }
        }
        return false;
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
        List<Map<String, Object>> rolloverMaps = (List<Map<String, Object>>)getProperty(ROLLOVERS_PROPERTY);
        for (Map<String, Object> rmap : rolloverMaps) {
            List<Number> pos = (List<Number>)rmap.get(POSITION_PROPERTY);
            // This won't set the dirty flag, but this class doesn't use it.
            rmap.put(POSITION_PROPERTY, Arrays.asList(
                    asDouble(pos.get(0)) + deltaFromPrevious.x,
                    asDouble(pos.get(1)) + deltaFromPrevious.y));
        }
    }

}
