package com.dozingcatsoftware.vectorpinball.editor.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

public class EditableWallPathElement extends EditableFieldElement {

    public static final String POSITIONS_PROPERTY = "positions";

    double getSegmentX(int index) {
        List<List<Number>> positions = (List<List<Number>>)getProperty(POSITIONS_PROPERTY);
        return positions.get(index).get(0).doubleValue();
    }

    double getSegmentY(int index) {
        List<List<Number>> positions = (List<List<Number>>)getProperty(POSITIONS_PROPERTY);
        return positions.get(index).get(1).doubleValue();
    }

    int numSegments() {
        return ((List<?>)getProperty(POSITIONS_PROPERTY)).size();
    }

    @Override public void drawForEditor(IFieldRenderer renderer, boolean isSelected) {
        Color color = currentColor(DEFAULT_WALL_COLOR);
        int size = numSegments();
        for (int i=1; i<size; i++) {
            renderer.drawLine(getSegmentX(i-1), getSegmentY(i-1),
                              getSegmentX(i), getSegmentY(i), color);
        }
        if (isSelected) {
            renderer.fillCircle(getSegmentX(0), getSegmentY(0), 0.25, color);
            renderer.fillCircle(getSegmentX(size-1), getSegmentY(size-1), 0.25, color);
        }
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        int size = numSegments();
        for (int i=1; i<size; i++) {
            if (point.distanceToLineSegment(getSegmentX(i-1), getSegmentY(i-1), getSegmentX(i), getSegmentY(i)) <= distance) {
                return true;
            }
        }
        return false;
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
        int size = numSegments();
        List<List<Double>> newPositions = new ArrayList<>();
        for (int i=0; i<size; i++) {
            newPositions.add(Arrays.asList(
                    getSegmentX(i) + deltaFromPrevious.x,
                    getSegmentY(i) + deltaFromPrevious.y));
        }
        setProperty(POSITIONS_PROPERTY, newPositions);
    }

}
