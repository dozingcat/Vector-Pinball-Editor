package com.dozingcatsoftware.vectorpinball.model;

/** This interface defines methods that draw graphical elements such as lines as circles to display the field. An implementation of
 * this interface is passed to FieldElement objects so they can draw themselves without depending directly on Android UI classes.
 */

public interface IFieldRenderer {

    // Some UI libraries (e.g. JavaFX) use doubles for coordinates, and some (e.g. Android canvas)
    // use floats. As an inelegant compromise support both in this interface; one set will call
    // through to the other.
    public void drawLine(float x1, float y1, float x2, float y2, Color color);
    public void drawLine(double x1, double y1, double x2, double y2, Color color);

    public void fillCircle(float cx, float cy, float radius, Color color);
    public void fillCircle(double cx, double cy, double radius, Color color);

    public void frameCircle(float cx, float cy, float radius, Color color);
    public void frameCircle(double cx, double cy, double radius, Color color);

    public void fillPolygon(double[] xPoints, double[] yPoints, Color color);

	public void doDraw();

	public int getWidth();

	public int getHeight();

	// 1=normal view, >1 zoomed in, <1 zoomed out.
	public double getRelativeScale();

    public void setDebugMessage(String debugMessage);

    public boolean canDraw();
}
