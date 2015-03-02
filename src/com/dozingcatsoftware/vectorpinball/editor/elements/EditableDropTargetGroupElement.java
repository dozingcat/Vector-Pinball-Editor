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
        List<List<Object>> positionList = (List) getProperty(POSITIONS_PROPERTY);
        usesDirectPositions = positionList!=null && !positionList.isEmpty();
        if (usesDirectPositions) {
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
        if (isSelected) {
            // Draw a translucent rectangle around each target.
            Color colorWithAlpha = Color.fromRGB(color.red, color.green, color.blue, color.alpha/2);
            double dist = 0.1 * renderer.getRelativeScale();
            double[] xPoints = new double[4];
            double[] yPoints = new double[4];
            for(double[] pos : positions) {
                double angle = Math.atan2(pos[3]-pos[1], pos[2]-pos[0]);
                double perpAngle = angle + TAU/4;
                // Extend past each endpoint of target, then go perpendicular to get polygon vertices.
                xPoints[0] = pos[0] - dist*Math.cos(angle) + dist*Math.cos(perpAngle);
                yPoints[0] = pos[1] - dist*Math.sin(angle) + dist*Math.sin(perpAngle);
                xPoints[1] = pos[2] + dist*Math.cos(angle) + dist*Math.cos(perpAngle);
                yPoints[1] = pos[3] + dist*Math.sin(angle) + dist*Math.sin(perpAngle);
                xPoints[2] = pos[2] + dist*Math.cos(angle) - dist*Math.cos(perpAngle);
                yPoints[2] = pos[3] + dist*Math.sin(angle) - dist*Math.sin(perpAngle);
                xPoints[3] = pos[0] - dist*Math.cos(angle) - dist*Math.cos(perpAngle);
                yPoints[3] = pos[1] - dist*Math.sin(angle) - dist*Math.sin(perpAngle);

                renderer.fillPolygon(xPoints, yPoints, colorWithAlpha);
            }
        }
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        refreshIfDirty();
        for (double[] pos : positions) {
            double actualDist = point.distanceToLineSegment(pos[0], pos[1], pos[2], pos[3]);
            if (actualDist <= distance) {
                return true;
            }
        }
        return false;
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
            // We only need to update the gapFromWall and startDistanceAlongWall properties,
            // and to do that we only need to look at the start of the first target.
            double newX = positions[0][0] + deltaFromPrevious.x;
            double newY = positions[0][1] + deltaFromPrevious.y;
            double distToWall = Point.distanceFromPointToLine(
                    newX, newY, wallStart[0], wallStart[1], wallEnd[0], wallEnd[1]);
            // Which side of the wall are we on? Go +90 degrees from wall vector
            // and see if dot product with vector from wall to target start is positive.
            double wallAngle = Math.atan2(wallEnd[1]-wallStart[1], wallEnd[0]-wallStart[0]);
            double wallDiffX = newX - wallStart[0];
            double wallDiffY = newY - wallStart[1];
            if (wallDiffX*Math.cos(wallAngle+TAU/4) + wallDiffY*Math.sin(wallAngle+TAU/4) < 0) {
                distToWall = -distToWall;
            }
            // Now we need the distance "along" the wall. Move to the line that the targets are on,
            // compute the distance, and take the dot product with wallAngle to get the sign.
            double targetLineX = wallStart[0] + distToWall*Math.cos(wallAngle + TAU/4);
            double targetLineY = wallStart[1] + distToWall*Math.sin(wallAngle + TAU/4);
            double distAlongWall = Point.distanceBetween(newX, newY, targetLineX, targetLineY);
            double lineDiffX = newX - targetLineX;
            double lineDiffY = newY - targetLineY;
            if (lineDiffX*Math.cos(wallAngle) + lineDiffY*Math.sin(wallAngle) < 0) {
                distAlongWall = -distAlongWall;
            }

            setProperty(GAP_FROM_WALL_PROPERTY, distToWall);
            setProperty(START_DISTANCE_ALONG_WALL_PROPERTY, distAlongWall);
        }
    }

}
