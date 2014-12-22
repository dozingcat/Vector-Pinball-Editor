package com.dozingcatsoftware.vectorpinball.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asFloat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

/**
 * FieldElement subclass that represents a straight wall. Its position is specified by the "position" parameter
 * with 4 values, which are [start x, start y, end x, end y]. There are several optional parameters to customize
 * the wall's behavior:
 * "kick": impulse to apply when a ball hits the wall, used for kickers and ball savers.
 * "kill": if true, the ball is lost when it hits the wall. Used for invisible wall below the flippers.
 * "retractWhenHit": if true, the wall is removed when hit by a ball. Used for ball savers.
 * "disabled": if true, the wall starts out retracted, and will only be shown when setRetracted(field, true) is called.
 *
 * Walls can be removed from the field by calling setRetracted(field, true), and restored with setRetracted(field, false).
 *
 * @author brian
 *
 */

public class WallElement extends FieldElement {

    public static final String POSITION_PROPERTY = "position";
    public static final String RESTITUTION_PROPERTY = "restitution";
    public static final String KICK_PROPERTY = "kick";
    public static final String KILL_PROPERTY = "kill";
    public static final String RETRACT_WHEN_HIT_PROPERTY = "retractWhenHit";
    public static final String DISABLED_PROPERTY = "disabled";

	Body wallBody;
	List<Body> bodySet;
	float x1, y1, x2, y2;
	float kick;

	boolean killBall;
	boolean retractWhenHit;
	float restitution;
	boolean disabled;

	@Override public void finishCreateElement(Map params, FieldElementCollection collection) {
		List pos = (List)params.get(POSITION_PROPERTY);
		this.x1 = asFloat(pos.get(0));
		this.y1 = asFloat(pos.get(1));
		this.x2 = asFloat(pos.get(2));
		this.y2 = asFloat(pos.get(3));
		this.restitution = asFloat(params.get(RESTITUTION_PROPERTY));

		this.kick = asFloat(params.get(KICK_PROPERTY));
		this.killBall = (Boolean.TRUE.equals(params.get(KILL_PROPERTY)));
		this.retractWhenHit = (Boolean.TRUE.equals(params.get(RETRACT_WHEN_HIT_PROPERTY)));
		this.disabled = Boolean.TRUE.equals(params.get(DISABLED_PROPERTY));
	}

	@Override public void createBodies(World world) {
        wallBody = Box2DFactory.createThinWall(world, x1, y1, x2, y2, restitution);
        bodySet = Collections.singletonList(wallBody);
        if (disabled) {
            setRetracted(true);
        }
	}

	public boolean isRetracted() {
		return !wallBody.isActive();
	}

	public void setRetracted(boolean retracted) {
		if (retracted!=this.isRetracted()) {
			wallBody.setActive(!retracted);
		}
	}

	@Override public List<Body> getBodies() {
		return bodySet;
	}

	@Override public boolean shouldCallTick() {
		// tick() only needs to be called if this wall provides a kick which makes it flash
		return (this.kick > 0.01f);
	}

	Vector2 impulseForBall(Body ball) {
		if (this.kick <= 0.01f) return null;
		// rotate wall direction 90 degrees for normal, choose direction toward ball
		float ix = this.y2 - this.y1;
		float iy = this.x1 - this.x2;
		float mag = (float)Math.sqrt(ix*ix + iy*iy);
		float scale = this.kick / mag;
		ix *= scale;
		iy *= scale;

		// dot product of (ball center - wall center) and impulse direction should be positive, if not flip impulse
		Vector2 balldiff = ball.getWorldCenter().cpy().sub(this.x1, this.y1);
		float dotprod = balldiff.x * ix + balldiff.y * iy;
		if (dotprod<0) {
			ix = -ix;
			iy = -iy;
		}

		return new Vector2(ix, iy);
	}


	@Override public void handleCollision(Body ball, Body bodyHit, Field field) {
		if (retractWhenHit) {
			this.setRetracted(true);
		}

		if (killBall) {
			field.removeBall(ball);
		}
		else {
			Vector2 impulse = this.impulseForBall(ball);
			if (impulse!=null) {
				ball.applyLinearImpulse(impulse, ball.getWorldCenter(), true);
				flashForFrames(3);
			}
		}
	}

	@Override public void draw(IFieldRenderer renderer) {
		if (isRetracted()) return;
		renderer.drawLine(x1, y1, x2, y2, currentColor(DEFAULT_WALL_COLOR));
	}

	// Editor support.
	enum DragType {
	    START, END, ALL,
	}
	DragType dragType;

    @Override public boolean isPointWithinDistance(Point point, double distance) {
        return point.distanceToLineSegment(Point.fromXY(x1, y1), Point.fromXY(x2, y2)) <= distance;
    }

    @Override public void startDrag(Point point) {
        double toStart = point.distanceTo(x1, y1);
        double toEnd = point.distanceTo(x2, y2);
        if (5*toStart < toEnd) {
            dragType = DragType.START;
        }
        else if (5*toEnd < toStart) {
            dragType = DragType.END;
        }
        else {
            dragType = DragType.ALL;
        }
    }

    @Override public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious) {
        switch (dragType) {
            case START:
                x1 += deltaFromPrevious.x;
                y1 += deltaFromPrevious.y;
                break;
            case END:
                x2 += deltaFromPrevious.x;
                y2 += deltaFromPrevious.y;
                break;
            case ALL:
                x1 += deltaFromPrevious.x;
                y1 += deltaFromPrevious.y;
                x2 += deltaFromPrevious.x;
                y2 += deltaFromPrevious.y;
                break;
            default:
                throw new AssertionError("Unknown drag type: " + dragType);
        }
    }

    @Override public void drawForEditor(IFieldRenderer renderer, boolean isSelected) {
        draw(renderer);
        Color color = currentColor(DEFAULT_WALL_COLOR);
        renderer.fillCircle(x1, y1, 0.25f, color);
        renderer.fillCircle(x2, y2, 0.25f, color);
    }

    @Override public Map<String, Object> getPropertyMap() {
        Map<String, Object> properties = mapWithDefaultProperties();
        properties.put(POSITION_PROPERTY, Arrays.asList(x1, y1, x2, y2));
        properties.put(RESTITUTION_PROPERTY, restitution);
        properties.put(KICK_PROPERTY, kick);
        properties.put(KILL_PROPERTY, killBall);
        properties.put(RETRACT_WHEN_HIT_PROPERTY, retractWhenHit);
        properties.put(DISABLED_PROPERTY, disabled);
        return properties;
    }
}
