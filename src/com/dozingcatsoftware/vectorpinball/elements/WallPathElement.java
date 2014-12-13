package com.dozingcatsoftware.vectorpinball.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asFloat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
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

	List wallBodies = new ArrayList();
	float[][] lineSegments;

	@Override public void finishCreateElement(Map params, FieldElementCollection collection) {
		List positions = (List)params.get("positions");
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

    @Override List<Point> getSamplePoints() {
        float[] first = this.lineSegments[0];
        float[] last = this.lineSegments[this.lineSegments.length-1];
        return Arrays.asList(Point.fromXY(first[0], first[1]), Point.fromXY(last[2], last[3]));
    }

    @Override boolean isPointWithinDistance(Point point, double distance) {
        for (float[] segment : this.lineSegments) {
            Point start = Point.fromXY(segment[0], segment[1]);
            Point end = Point.fromXY(segment[2], segment[3]);
            if (point.distanceToLineSegment(start, end) <= distance) {
                return true;
            }
        }
        return false;
    }
}
