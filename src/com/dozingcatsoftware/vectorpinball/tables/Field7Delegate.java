package com.dozingcatsoftware.vectorpinball.tables;

import com.badlogic.gdx.math.Vector2;
import com.dozingcatsoftware.vectorpinball.elements.DropTargetGroupElement;
import com.dozingcatsoftware.vectorpinball.elements.RolloverGroupElement;
import com.dozingcatsoftware.vectorpinball.elements.SensorElement;
import com.dozingcatsoftware.vectorpinball.elements.WallElement;
import com.dozingcatsoftware.vectorpinball.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dozingcatsoftware.vectorpinball.tables.Stars.Constellation;
import static com.dozingcatsoftware.vectorpinball.tables.Stars.StarCatalog;

public class Field7Delegate extends BaseFieldDelegate {

    static final double TAU = 2 * Math.PI;

    static final List<Constellation> CONSTELLATIONS = Stars.CONSTELLATIONS;
    static final StarCatalog CATALOG = Stars.CATALOG;

    static long billions(int n) {
        return 1_000_000_000L * n;
    }

    static double interp(double start, double end, double fraction) {
        return start + fraction * (end - start);
    }

    enum Mode {
        WANDERING,
        CONSTELLATION_IN_PROGRESS,
        CONSTELLATION_FINISHED,
    }

    static class ProjectionTarget {
        double rightAscension;
        double declination;
        double angularRadius;
    }

    static class Star2DProjection {
        ArrayList<Double> x = new ArrayList<Double>();
        ArrayList<Double> y = new ArrayList<Double>();
        ArrayList<Double> magnitude = new ArrayList<Double>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        HashMap<Integer, Integer> starIndexToProjIndex = new HashMap<Integer, Integer>();

        int size() {
            return this.x.size();
        }

        void clear() {
            this.x.clear();
            this.y.clear();
            this.magnitude.clear();
            this.indices.clear();
            this.starIndexToProjIndex.clear();
        }

        void add(double xx, double yy, double mag, int index) {
            this.x.add(xx);
            this.y.add(yy);
            this.magnitude.add(mag);
            this.indices.add(index);
            this.starIndexToProjIndex.put(index, this.x.size() - 1);
        }
    }

    static class State {
        Set<Integer> activatedStars = new HashSet<Integer>();
        List<Constellation> lockedConstellations = new ArrayList<Constellation>();
        Constellation currentConstellation = CONSTELLATIONS.get(0);
        ProjectionTarget currentTarget = new ProjectionTarget();
        Star2DProjection projection = new Star2DProjection();

        ProjectionTarget animateFromTarget = new ProjectionTarget();
        ProjectionTarget animateToTarget = new ProjectionTarget();
        long animationDurationNanos = billions(10);
        long animationElapsedNanos = -1;

        void tick(long nanos) {
            if (animationElapsedNanos >= 0) {
                animationElapsedNanos += nanos;
                if (animationElapsedNanos >= animationDurationNanos) {
                    animationElapsedNanos = -1;
                }
            }
            if (animationElapsedNanos < 0) {
                Constellation dst = this.currentConstellation;
                currentTarget.rightAscension = dst.centerRaRadians;
                currentTarget.declination = dst.centerDecRadians;
                currentTarget.angularRadius = 1.2 * dst.angularRadius;
            }
            else {
                ProjectionTarget src = this.animateFromTarget;
                ProjectionTarget dst = this.animateToTarget;
                double frac = Math.min(1.0, 1.0 * animationElapsedNanos / animationDurationNanos);
                currentTarget.rightAscension = interp(src.rightAscension, dst.rightAscension, frac);
                currentTarget.declination = interp(src.declination, dst.declination, frac);
                currentTarget.angularRadius =
                        1.2 * interp(src.angularRadius, dst.angularRadius, frac);
            }
            projectVisibleStars(CATALOG, this.currentTarget, this.projection);
        }

        void activateStarsInActiveConstellationNearPoint(double x, double y) {
            if (x * x + y * y < 1) {
                for (int starIndex : this.currentConstellation.starIndices) {
                    Integer pi = this.projection.starIndexToProjIndex.get(starIndex);
                    if (pi != null) {
                        double px = projection.x.get(pi) / this.currentTarget.angularRadius;
                        double py = projection.y.get(pi) / this.currentTarget.angularRadius;
                        double dist2 = (x - px) * (x - px) + (y - py) * (y - py);
                        if (dist2 < 0.1 * 0.1) {
                            this.activatedStars.add(starIndex);
                        }
                    }
                }
            }
        }

        boolean allStarsInCurrentConstellationActive() {
            return this.activatedStars.containsAll(this.currentConstellation.starIndices);
        }

        void animateToConstellation(Constellation c) {
            this.currentConstellation = c;
            this.animateFromTarget.rightAscension = this.currentTarget.rightAscension;
            this.animateFromTarget.declination = this.currentTarget.declination;
            this.animateFromTarget.angularRadius = this.currentTarget.angularRadius;
            this.animateToTarget.rightAscension = c.centerRaRadians;
            this.animateToTarget.declination = c.centerDecRadians;
            this.animateToTarget.angularRadius = c.angularRadius;
            this.animationElapsedNanos = 0;
        }

        void switchConstellationIfAllowed() {
            if (CONSTELLATIONS.size() == this.lockedConstellations.size()) {
                return;
            }
            int ci = (this.currentConstellation != null) ?
                    CONSTELLATIONS.indexOf(this.currentConstellation) : -1;
            while (true) {
                ci = (ci + 1) % CONSTELLATIONS.size();
                if (!this.lockedConstellations.contains(CONSTELLATIONS.get(ci))) {
                    animateToConstellation(CONSTELLATIONS.get(ci));
                    break;
                }
            }
        }

        static void projectVisibleStars(StarCatalog catalog, ProjectionTarget target, Star2DProjection projection) {
            projection.clear();
            double rad2 = target.angularRadius * target.angularRadius;
            int catSize = catalog.size();
            // Rotate each star around the Z axis for right ascension, then the Y axis for declination.
            // The point we're looking at will now be at (1, 0, 0), and when we project to 2D, Y becomes X
            // and Z becomes Y.
            for (int i = 0; i < catSize; i++) {
                double x = catalog.x[i];
                double y = catalog.y[i];
                double z = catalog.z[i];
                // Matrix rotations from https://en.wikipedia.org/wiki/Rotation_matrix#In_three_dimensions
                // Around Z axis:
                // [cos(theta), -sin(theta), 0]
                // [sin(theta), cos(theta), 0]
                // [0, 0, 1]
                // We can treat this as a 2d rotation in the XY plane; z remains constant.
                double x1 = x * Math.cos(target.rightAscension) - y * Math.sin(target.rightAscension);
                double y1 = x * Math.sin(target.rightAscension) + y * Math.cos(target.rightAscension);
                double z1 = z;
                // Around Y axis:
                // [cos(theta), 0, sin(theta)]
                // [0, 1, 0]
                // [-sin(theta), 0, cos(theta)]
                double x2 = x1 * Math.cos(target.declination) + z1 * Math.sin(target.declination);
                double y2 = y1;
                double z2 = -x1 * Math.sin(target.declination) + z1 * Math.cos(target.declination);
                // We started with a unit vector so we could normalize [x2, y2, z2], but it shouldn't be too far off.
                // The star is "visible" if it's close enough to the X axis on the positive side.
                double yzOffsetSq = y2 * y2 + z2 * z2;
                if (x2 > 0 && yzOffsetSq < rad2) {
                    projection.add(y2, z2, catalog.magnitude[i], i);
                }
            }
        }
    }

    State state = new State();

    Vector2 starViewCenter;
    double starViewRadius;

    @Override public boolean isFieldActive(Field field) {
        return true;
    }

    @Override public void gameStarted(Field field) {
        state = new State();
    }

    @Override public void ballInSensorRange(final Field field, SensorElement sensor, Ball ball) {
        String id = sensor.getElementId();
        // Enable launch barrier.
        if ("LaunchBarrierSensor".equals(id)) {
            setLaunchBarrierEnabled(field, true);
        }
        else if ("LaunchBarrierRetract".equals(id)) {
            setLaunchBarrierEnabled(field, false);
        }
        else if ("LeftFlipperDropSensor".equals(id) || "RightFlipperDropSensor".equals(id)) {
            ball.getBody().setLinearVelocity(0, 0);
        }
        else if ("MiniTableOrBallLockSensor".equals(id)) {

        }
        else if ("LeftLoopDetector".equals(id) && !id.equals(ball.getPreviousSensorId())) {
            handleLoop(field);
        }
        else if ("RightLoopDetector".equals(id) && !id.equals(ball.getPreviousSensorId())) {
            handleLoop(field);
        }
    }

    void handleLoop(Field field) {
        field.addScore(5000);
        state.switchConstellationIfAllowed();
    }

    static void setLaunchBarrierEnabled(Field field, boolean enabled) {
        WallElement barrier = (WallElement)field.getFieldElementById("LaunchBarrier");
        barrier.setRetracted(!enabled);
    }

    void updateActivatedStars(Field field) {
        List<Ball> balls = field.getBalls();
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            if (ball.getLayer() != 0) {
                continue;
            }
            double bx = (ball.getPosition().x - starViewCenter.x) / starViewRadius;
            double by = (ball.getPosition().y - starViewCenter.y) / starViewRadius;
            state.activateStarsInActiveConstellationNearPoint(bx, by);
        }
    }

    @Override public void tick(Field field, long nanos) {
        if (starViewCenter == null) {
            RolloverGroupElement boundary =
                    (RolloverGroupElement) field.getFieldElementById("StarViewBoundary");
            starViewCenter = boundary.getRolloverCenterAtIndex(0);
            starViewRadius = boundary.getRolloverRadiusAtIndex(0);
        }
        state.tick(nanos);
        updateActivatedStars(field);
        double distScale = starViewRadius / state.currentTarget.angularRadius;
        field.setShapes(shapesFromProjection());
    }

    @Override public void allRolloversInGroupActivated(Field field, RolloverGroupElement rolloverGroup, Ball ball) {
        String id = rolloverGroup.getElementId();
        if ("FlipperRollovers".equals(id) || "TopRollovers".equals(id)) {
            // rollover groups increment field multiplier when all rollovers are activated, also reset to inactive
            rolloverGroup.setAllRolloversActivated(false);
            field.getGameState().incrementScoreMultiplier();
            field.showGameMessage(((int)field.getGameState().getScoreMultiplier()) + "x Multiplier", 1500);
        }
    }

    @Override public void allDropTargetsInGroupHit(Field field, DropTargetGroupElement targetGroup, Ball ball) {
        String id = targetGroup.getElementId();
        if ("DropTargetLeftSave".equals(id)) {
            ((WallElement)field.getFieldElementById("BallSaver-left")).setRetracted(false);
            field.showGameMessage("Left Save Enabled", 1500);
        }
        else if ("DropTargetRightSave".equals(id)) {
            ((WallElement)field.getFieldElementById("BallSaver-right")).setRetracted(false);
            field.showGameMessage("Right Save Enabled", 1500);
        }
    }

    static int ACTIVE_STAR_ACTIVE_CONSTELLATION_COLOR = Color.fromRGB(255, 255, 0);
    static int INACTIVE_STAR_ACTIVE_CONSTELLATION_COLOR = Color.fromRGB(0, 255, 0);
    static int ACTIVE_STAR_INACTIVE_CONSTELLATION_COLOR = Color.fromRGB(255, 0, 0);
    static int INACTIVE_STAR_INACTIVE_CONSTELLATION_COLOR = Color.fromRGB(128, 0, 0);
    static int CONSTELLATION_LINE_COLOR = Color.fromRGBA(240, 240, 240, 192);

    int starColorForIndex(int starIndex) {
        boolean isActive = state.activatedStars.contains(starIndex);
        boolean isInActiveConstellation = state.currentConstellation.starIndices.contains(starIndex);
        if (isInActiveConstellation) {
            return isActive ?
                    ACTIVE_STAR_ACTIVE_CONSTELLATION_COLOR :
                    INACTIVE_STAR_ACTIVE_CONSTELLATION_COLOR;
        }
        else {
            return isActive ?
                    ACTIVE_STAR_INACTIVE_CONSTELLATION_COLOR :
                    INACTIVE_STAR_INACTIVE_CONSTELLATION_COLOR;
        }
    }

    List<Shape> shapesFromProjection() {
        Star2DProjection proj = state.projection;
        double centerX = this.starViewCenter.x;
        double centerY = this.starViewCenter.y;
        double distScale = this.starViewRadius / state.currentTarget.angularRadius;
        double baseRadius = this.starViewRadius * 0.015;
        List<Shape> shapes = new ArrayList<Shape>();
        int consColor = Color.fromRGB(255, 255, 0);
        int noConsColor = Color.fromRGB(160, 0, 0);
        for (int i = 0; i < proj.size(); i++) {
            double cx = centerX + proj.x.get(i) * distScale;
            double cy = centerY + proj.y.get(i) * distScale;
            double mag = proj.magnitude.get(i);
            int alpha = (mag <= 0) ? 255 : Math.max(0, (int) (255 - 30 * mag));
            int baseColor = starColorForIndex(proj.indices.get(i));
            int color = Color.withAlpha(baseColor, alpha);
            double rmul = (mag <= 0) ? 1.5 : (mag >= 4) ? 0.75 : 1.0;
            shapes.add(Shape.Circle.create(cx, cy, rmul * baseRadius, Shape.FillType.SOLID, 0, color, null));
        }
        // Draw brighter stars (with lower magnitudes) last.
        Collections.reverse(shapes);
        // Lines for active constellation.
        for (int starIndex : state.currentConstellation.starIndices) {
            if (state.activatedStars.contains((starIndex))) {
                Set<Integer> endpoints = state.currentConstellation.segmentsByIndex.get(starIndex);
                if (endpoints != null) {
                    for (int endIndex : endpoints) {
                        if (state.activatedStars.contains(endIndex)) {
                            Integer pi1 = proj.starIndexToProjIndex.get(starIndex);
                            Integer pi2 = proj.starIndexToProjIndex.get(endIndex);
                            if (pi1 == null || pi2 == null) {
                                continue;
                            }
                            double x1 = centerX + proj.x.get(pi1) * distScale;
                            double y1 = centerY + proj.y.get(pi1) * distScale;
                            double x2 = centerX + proj.x.get(pi2) * distScale;
                            double y2 = centerY + proj.y.get(pi2) * distScale;
                            shapes.add(Shape.Line.create(x1, y1, x2, y2, 0, CONSTELLATION_LINE_COLOR, null));
                        }
                    }
                }
            }
        }
        return shapes;
    }
}
