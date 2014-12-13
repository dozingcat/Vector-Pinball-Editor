package com.dozingcatsoftware.vectorpinball.model;

import java.util.List;

/**
 * An immutable 2D point.
 */
public class Point {
  public final double x, y;

  private Point(double x, double y) {
      this.x = x;
      this.y = y;
  }

  public static Point fromXY(double x, double y) {
      return new Point(x, y);
  }

  public static Point fromList(List<Number> xyList) {
      return fromXY(xyList.get(0).doubleValue(), xyList.get(1).doubleValue());
  }

  public double distanceTo(Point other) {
      double xdiff = this.x - other.x;
      double ydiff = this.y - other.y;
      return Math.sqrt(xdiff*xdiff + ydiff*ydiff);
  }

  public double squaredDistanceTo(Point other) {
      double xdiff = this.x - other.x;
      double ydiff = this.y - other.y;
      return xdiff*xdiff + ydiff*ydiff;
  }

  public double distanceToLine(Point p1, Point p2) {
      // http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
      double segmentLength = p1.distanceTo(p2);
      if (segmentLength==0) return this.distanceTo(p1);
      return Math.abs((p2.y-p1.y)*x - (p2.x-p1.x)*y + p2.x*p1.y - p2.y*p1.x) / segmentLength;
  }

  public double distanceToLineSegment(Point p1, Point p2) {
      // If dot product of (p2-p1) and (this-p1) is negative, we extend past p1.
      if ((p2.x-p1.x) * (x-p1.x) + (p2.y-p1.y)*(y-p1.y) < 0) {
          return distanceTo(p1);
      }
      // If dot product of (p1-p2) and (this-p2) is negative, we extend past p2.
      if ((p1.x-p2.x) * (x-p2.x) + (p1.y-p2.y)*(y-p2.y) < 0) {
          return distanceTo(p2);
      }
      return distanceToLine(p1, p2);
  }
}
