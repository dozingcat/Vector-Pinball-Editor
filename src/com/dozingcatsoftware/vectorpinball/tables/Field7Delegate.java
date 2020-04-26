package com.dozingcatsoftware.vectorpinball.tables;

import com.dozingcatsoftware.vectorpinball.elements.SensorElement;
import com.dozingcatsoftware.vectorpinball.elements.WallElement;
import com.dozingcatsoftware.vectorpinball.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Field7Delegate extends BaseFieldDelegate {

    static final double TAU = 2 * Math.PI;

    StarCatalog catalog = makeCatalog();
    Star2DProjection projection = new Star2DProjection();

    double rightAscension = 0;

    @Override public boolean isFieldActive(Field field) {
        return true;
    }

    @Override public void ballInSensorRange(final Field field, SensorElement sensor, Ball ball) {
        String sensorID = sensor.getElementId();
        // enable launch barrier
        if ("LaunchBarrierSensor".equals(sensorID)) {
            setLaunchBarrierEnabled(field, true);
        } else if ("LaunchBarrierRetract".equals(sensorID)) {
            setLaunchBarrierEnabled(field, false);
        }
    }

    static void setLaunchBarrierEnabled(Field field, boolean enabled) {
        WallElement barrier = (WallElement)field.getFieldElementById("LaunchBarrier");
        barrier.setRetracted(!enabled);
    }

    double betel_ra = 88.8*TAU/360;
    double betel_dec = 7.41*TAU/360;
    double rigel_ra = 78.6*TAU/360;
    double rigel_dec = -8.2*TAU/360;

    @Override public void tick(Field field, long nanos) {
        rightAscension += 0.01;
        if (rightAscension > TAU) {
            rightAscension -= TAU;
        }
        projectVisibleStars(catalog, rightAscension, betel_dec, 0.7, projection);
        field.setShapes(shapesFromProjection(projection, 9.4, 13, 4.5/.7, 0.05));
    }

    static List<Shape> shapesFromProjection(
            Star2DProjection proj, double centerX, double centerY, double distScale, double radius) {
        List<Shape> stars = new ArrayList<Shape>();
        for (int i = 0; i < proj.size(); i++) {
            double cx = centerX + proj.x.get(i) * distScale;
            double cy = centerY + proj.y.get(i) * distScale;
            int color = Color.fromRGB(255, 255, 0);
            stars.add(Shape.Circle.create(cx, cy, radius, Shape.FillType.SOLID, 0, color, null));
        }
        return stars;
    }

    static class StarCatalog {
        double[] x;
        double[] y;
        double[] z;
        double[] magnitude;

        int size() {
            return this.x.length;
        }
    }

    private static StarCatalog makeCatalog() {
        StarCatalog cat = new StarCatalog();
        assert STAR_DATA.length % 3 == 0;
        int numStars = STAR_DATA.length / 3;
        cat.x = new double[numStars];
        cat.y = new double[numStars];
        cat.z = new double[numStars];
        cat.magnitude = new double[numStars];
        for (int i = 0; i < numStars; i++) {
            int offset = 3 * i;
            double rho = Math.toRadians(STAR_DATA[offset]);
            double theta = Math.toRadians(STAR_DATA[offset + 1]);
            cat.x[i] = Math.cos(rho) * Math.cos(theta);
            cat.y[i] = -Math.cos(rho) * Math.sin(theta);
            cat.z[i] = Math.sin(rho);
            cat.magnitude[i] = STAR_DATA[offset + 2];
        }
        return cat;
    }

    static class Star2DProjection {
        ArrayList<Double> x = new ArrayList<Double>();
        ArrayList<Double> y = new ArrayList<Double>();
        ArrayList<Double> magnitude = new ArrayList<Double>();
        ArrayList<Integer> indices = new ArrayList<Integer>();

        int size() {
            return this.x.size();
        }

        void clear() {
            this.x.clear();
            this.y.clear();
            this.magnitude.clear();
            this.indices.clear();
        }

        void add(double xx, double yy, double mag, int index) {
            this.x.add(xx);
            this.y.add(yy);
            this.magnitude.add(mag);
            this.indices.add(index);
        }
    }

    static void projectVisibleStars(
            StarCatalog catalog, double rightAscension, double declination, double viewRadius,
            Star2DProjection projection) {
        projection.clear();
        double rad2 = viewRadius * viewRadius;
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
            double x1 = x * Math.cos(rightAscension) - y * Math.sin(rightAscension);
            double y1 = x * Math.sin(rightAscension) + y * Math.cos(rightAscension);
            double z1 = z;
            // Around Y axis:
            // [cos(theta), 0, sin(theta)]
            // [0, 1, 0]
            // [-sin(theta), 0, cos(theta)]
            double x2 = x1 * Math.cos(declination) + z1 * Math.sin(declination);
            double y2 = y1;
            double z2 = -x1 * Math.sin(declination) + z1 * Math.cos(declination);
            // We started with a unit vector so we could normalize [x2, y2, z2], but it shouldn't be too far off.
            // The star is "visible" if it's close enough to the X axis on the positive side.
            double yzOffsetSq = y2 * y2 + z2 * z2;
            if (x2 > 0 && yzOffsetSq < rad2) {
                projection.add(y2, z2, catalog.magnitude[i], i);
            }
        }
    }

    // From https://github.com/sky-map-team/stardroid/blob/master/tools/data/stardata_names.txt
    // Concatenated triples of declination (degrees), right ascension (degrees), magnitude.
    static final double[] STAR_DATA = {
        -16.71, 101.25, -1.43,
        -52.70,  96.00, -0.62,
         19.19, 213.90, -0.10,
        -60.84, 219.90,  0.01,
         38.78, 279.30,  0.03,
         -8.20,  78.60,  0.18,
          5.23, 114.90,  0.38,
          7.41,  88.80,  0.45,
        -57.24,  24.45,  0.45,
        -60.37, 210.90,  0.61,
         46.00,  79.20,  0.71,
          8.87, 297.75,  0.77,
        -63.10, 186.60,  0.77,
         16.51,  69.00,  0.85,
         46.01,  79.20,  0.96,
        -11.16, 201.30,  0.98,
        -26.43, 247.35,  1.06,
         28.03, 116.40,  1.14,
        -29.62, 344.40,  1.16,
         45.28, 310.35,  1.25,
        -59.69, 192.00,  1.25,
        -60.84, 219.90,  1.34,
         11.97, 152.10,  1.36,
        -28.97, 104.70,  1.50,
        -57.11, 187.80,  1.59,
        -37.10, 263.40,  1.62,
          6.35,  81.30,  1.64,
         28.61,  81.60,  1.65,
        -69.72, 138.30,  1.67,
         -1.20,  84.00,  1.69,
        -46.96, 332.10,  1.74,
         -1.94,  85.20,  1.74,
        -47.34, 122.40,  1.75,
         55.96, 193.50,  1.76,
         49.86,  51.15,  1.79,
        -34.38, 276.00,  1.79,
         61.75, 165.90,  1.81,
        -26.39, 107.10,  1.83,
         49.31, 206.85,  1.85,
        -59.51, 125.70,  1.86,
        -43.00, 264.30,  1.86,
         44.95,  89.85,  1.90,
        -69.03, 252.15,  1.91,
         16.40,  99.45,  1.93,
        -56.73, 306.45,  1.94,
         31.89, 113.70,  1.94,
         89.26,  37.95,  1.97,
        -17.96,  95.70,  1.98,
         -8.66, 141.90,  1.99,
         23.46,  31.80,  2.00,
         19.84, 154.95,  2.01,
        -54.71, 131.25,  2.02,
        -17.99,  10.95,  2.04,
        -26.30, 283.80,  2.05,
         35.62,  17.40,  2.05,
        -36.37, 211.65,  2.06,
         29.09,   2.10,  2.07,
        -46.88, 340.65,  2.07,
         -9.67,  87.00,  2.07,
         74.16, 222.75,  2.07,
         12.56, 263.70,  2.08,
         40.96,  47.10,  2.09,
         42.33,  30.90,  2.10,
         14.57, 177.30,  2.14,
         60.72,  14.25,  2.15,
        -48.96, 190.35,  2.20,
        -59.28, 139.20,  2.21,
        -40.00, 120.90,  2.21,
         26.71, 233.70,  2.22,
         40.26, 305.55,  2.23,
        -43.43, 136.95,  2.23,
         51.49, 269.10,  2.24,
         56.54,  10.20,  2.24,
         54.93, 201.00,  2.25,
         -0.30,  82.95,  2.25,
         59.15,   2.25,  2.27,
        -53.47, 204.90,  2.29,
        -34.29, 252.60,  2.29,
        -22.62, 240.15,  2.29,
        -47.39, 220.50,  2.30,
        -42.16, 218.85,  2.33,
         27.07, 221.25,  2.35,
         56.38, 165.45,  2.37,
          9.88, 326.10,  2.38,
        -39.03, 265.65,  2.39,
        -42.31,   6.60,  2.40,
         53.69, 178.50,  2.41,
         62.59, 319.65,  2.44,
         28.08, 345.90,  2.44,
         33.97, 311.55,  2.45,
        -29.30, 111.00,  2.45,
        -55.01, 140.55,  2.47,
         15.21, 346.20,  2.49,
        -10.57, 249.30,  2.54,
          4.09,  45.60,  2.54,
        -47.29, 208.95,  2.55,
        -19.81, 241.35,  2.56,
         20.52, 168.60,  2.56,
        -17.82,  83.25,  2.58,
        -17.54, 183.90,  2.58,
    };
}
