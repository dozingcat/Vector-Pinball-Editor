package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.TAU;
import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;
import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

public class EditableDropTargetGroupElement extends EditableFieldElement {

    public static final String POSITIONS_PROPERTY = "positions";
    public static final String WALL_START_PROPERTY = "wallStart";
    public static final String WALL_END_PROPERTY = "wallEnd";
    public static final String GAP_FROM_WALL_PROPERTY = "gapFromWall";
    public static final String START_DISTANCE_ALONG_WALL_PROPERTY = "startDistanceAlongWall";
    public static final String TARGET_WIDTH_PROPERTY = "targetWidth";
    public static final String GAP_BETWEEN_TARGETS_PROPERTY = "gapBetweenTargets";
    public static final String RESET_DELAY_PROPERTY = "reset";
    public static final String NUM_TARGETS_PROPERTY = "numTargets";

    static final Color DEFAULT_COLOR = Color.fromRGB(0, 255, 0);

    boolean usesDirectPositions;
    double[][] positions;
    // The following are used if positions are not given directly.
    double[] wallStart;
    double[] wallEnd;
    double gapFromWall;
    double startDistanceAlongWall;
    double targetWidth;
    double gapBetweenTargets;
    double resetDelay;
    int numTargets;

    @Override public void refreshInternalValues() {
        // Individual targets can be specified in "positions" list.
        usesDirectPositions = hasProperty(POSITIONS_PROPERTY);
        if (usesDirectPositions) {
            List<List<Object>> positionList = (List) getProperty(POSITIONS_PROPERTY);
            positions = new double[positionList.size()][];
            for (int i = 0; i < positionList.size(); i++) {
                List<Object> coords = positionList.get(i);
                positions[i] = new double[] {asDouble(coords.get(0)), asDouble(coords.get(1)),
                                             asDouble(coords.get(2)), asDouble(coords.get(3))};
            }
        }
        else {
            wallStart = getDoubleArrayProperty(WALL_START_PROPERTY);
            wallEnd = getDoubleArrayProperty(WALL_END_PROPERTY);
            gapFromWall = asDouble(getProperty(GAP_FROM_WALL_PROPERTY));
            startDistanceAlongWall = asDouble(getProperty(START_DISTANCE_ALONG_WALL_PROPERTY));
            targetWidth = asDouble(getProperty(TARGET_WIDTH_PROPERTY));
            gapBetweenTargets = asDouble(getProperty(GAP_BETWEEN_TARGETS_PROPERTY));
            resetDelay = asDouble(getProperty(RESET_DELAY_PROPERTY));
            numTargets = asInt(getProperty(NUM_TARGETS_PROPERTY));

            positions = new double[numTargets][];
            double wallAngle = Math.atan2(wallEnd[1] - wallStart[1], wallEnd[0] - wallStart[0]);
            double perpToWallAngle = wallAngle + TAU/4;
            for (int i = 0; i < numTargets; i++) {
                double alongWallStart = startDistanceAlongWall + i * (targetWidth + gapBetweenTargets);
                double alongWallEnd = alongWallStart + targetWidth;
                double x1 = (wallStart[0] + (alongWallStart * Math.cos(wallAngle)) +
                                            (gapFromWall * Math.cos(perpToWallAngle)));
                double y1 = (wallStart[1] + (alongWallStart * Math.sin(wallAngle)) +
                                            (gapFromWall * Math.sin(perpToWallAngle)));
                double x2 = (wallStart[0] + (alongWallEnd * Math.cos(wallAngle)) +
                                            (gapFromWall * Math.cos(perpToWallAngle)));
                double y2 = (wallStart[1] + (alongWallEnd * Math.sin(wallAngle)) +
                                            (gapFromWall * Math.sin(perpToWallAngle)));
                positions[i] = new double[] {x1, y1, x2, y2};
            }
        }
    }

    @Override protected void addPropertiesForNewElement(Map<String, Object> props, EditableField field) {
        props.put(POSITIONS_PROPERTY, Arrays.asList(
                Arrays.asList(-0.5, 0.0, -0.5, 0.8),
                Arrays.asList(-0.5, 1.0, -0.5, 1.8),
                Arrays.asList(-0.5, 2.0, -0.5, 2.8)
        ));
        props.put(RESET_DELAY_PROPERTY, "2");
    }

    @Override public void drawForEditor(IFieldRenderer renderer, boolean isSelected) {
        refreshIfDirty();
        // draw line for each target
        Color color = currentColor(DEFAULT_COLOR);
        for(double[] pos : positions) {
            renderer.drawLine(pos[0], pos[1], pos[2], pos[3], color);
        }

    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        refreshIfDirty();
        double[] firstSegment = positions[0];
        double[] lastSegment = positions[positions.length-1];
        double actualDist = point.distanceToLineSegment(
                firstSegment[0], firstSegment[1], lastSegment[2], lastSegment[3]);
        return actualDist <= distance;
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
        refreshIfDirty();
        if (usesDirectPositions) {
            List<List<Number>> newPositions = new ArrayList<>();
            for (double[] pos : positions) {
                newPositions.add(Arrays.asList(
                        pos[0] + deltaFromPrevious.x,
                        pos[1] + deltaFromPrevious.y,
                        pos[2] + deltaFromPrevious.x,
                        pos[3] + deltaFromPrevious.y));
            }
            setProperty(POSITIONS_PROPERTY, newPositions);
        }
        else {
            // TODO: adjust wall-relative values.
        }
    }

}
