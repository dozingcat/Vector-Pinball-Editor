package com.dozingcatsoftware.vectorpinball.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asFloat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

/**
 * FieldElement subclass which represents a series of wall segments. The segments are defined in the "positions"
 * parameter as a list of [x,y] values, for example:
 * {
 * 		"class": "WallPathElement",
 * 		"positions": [[5,5], [5,10], [8,10], [5, 15]]
 * }
 *
 * @author brian
 */

public class WallPathElement extends FieldElement {

    public static final String POSITIONS_PROPERTY = "positions";

	List<Body> wallBodies = new ArrayList<Body>();
	float[][] lineSegments;

	@Override public void finishCreateElement(Map params, FieldElementCollection collection) {
		List positions = (List)params.get(POSITIONS_PROPERTY);
		// N positions produce N-1 line segments
		lineSegments = new float[positions.size()-1][];
		for(int i=0; i<lineSegments.length; i++) {
			List startpos = (List)positions.get(i);
			List endpos = (List)positions.get(i+1);

			float[] segment = {asFloat(startpos.get(0)), asFloat(startpos.get(1)),
					asFloat(endpos.get(0)), asFloat(endpos.get(1))};
			lineSegments[i] = segment;
		}
	}

	@Override public void createBodies(World world) {
        for (float[] segment : this.lineSegments) {
            Body wall = Box2DFactory.createThinWall(world, segment[0], segment[1], segment[2], segment[3], 0f);
            this.wallBodies.add(wall);
        }
	}

	@Override public List<Body> getBodies() {
		return wallBodies;
	}

	@Override public void draw(IFieldRenderer renderer) {
		for (float[] segment : this.lineSegments) {
			renderer.drawLine(segment[0], segment[1], segment[2], segment[3], currentColor(DEFAULT_WALL_COLOR));
		}
	}

	// Editor support.
    @Override public void drawForEditor(IFieldRenderer renderer, boolean isSelected) {
        draw(renderer);
        if (isSelected) {
            Color color = currentColor(DEFAULT_WALL_COLOR);
            float[] lastSegment = this.lineSegments[this.lineSegments.length-1];
            renderer.fillCircle(lineSegments[0][0], lineSegments[0][1], 0.25f, color);
            renderer.fillCircle(lastSegment[2], lastSegment[3], 0.25f, color);
        }
    }

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        for (float[] segment : this.lineSegments) {
            Point start = Point.fromXY(segment[0], segment[1]);
            Point end = Point.fromXY(segment[2], segment[3]);
            if (point.distanceToLineSegment(start, end) <= distance) {
                return true;
            }
        }
        return false;
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
        for (float[] segment : lineSegments) {
            segment[0] += deltaFromPrevious.x;
            segment[1] += deltaFromPrevious.y;
            segment[2] += deltaFromPrevious.x;
            segment[3] += deltaFromPrevious.y;
        }
    }

    @Override public Map<String, Object> getPropertyMap() {
        Map<String, Object> properties = mapWithDefaultProperties();
        List<List<Number>> positions = new ArrayList<List<Number>>();
        // Take the start point of the first segment, and then every end point.
        positions.add(Arrays.asList(lineSegments[0][0], lineSegments[0][1]));
        for (float[] segment : lineSegments) {
            positions.add(Arrays.asList(segment[2], segment[3]));
        }
        properties.put(POSITIONS_PROPERTY, positions);
        return properties;
    }
}
