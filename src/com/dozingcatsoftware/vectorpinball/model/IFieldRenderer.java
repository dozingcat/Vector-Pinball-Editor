package com.dozingcatsoftware.vectorpinball.model;

/** This interface defines methods that draw graphical elements such as lines as circles to display the field. An implementation of
 * this interface is passed to FieldElement objects so they can draw themselves without depending directly on Android UI classes.
 */

public interface IFieldRenderer {

	public void drawLine(float x1, float y1, float x2, float y2, Color color);

	public void fillCircle(float cx, float cy, float radius, Color color);

	public void frameCircle(float cx, float cy, float radius, Color color);

	public void doDraw();

	public int getWidth();

	public int getHeight();

    public void setDebugMessage(String fpsDebugInfo);

    public boolean canDraw();
}
