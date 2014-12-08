package com.dozingcatsoftware.vectorpinball.model;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asFloat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.physics.box2d.World;
import com.dozingcatsoftware.vectorpinball.elements.FieldElement;
import com.dozingcatsoftware.vectorpinball.elements.FieldElementCollection;
import com.dozingcatsoftware.vectorpinball.elements.FlipperElement;

public class FieldLayout {

	Random RAND = new Random();

	private FieldLayout() {}

	public static FieldLayout layoutForLevel(Map<String, Object> levelMap, World world) {
		FieldLayout layout = new FieldLayout();
		layout.initFromLevel(levelMap, world);
		return layout;
	}

	FieldElementCollection fieldElements;
	float width;
	float height;
	Color ballColor;
	Color secondaryBallColor;
	float targetTimeRatio;
	Map allParameters;

	static final Color DEFAULT_BALL_COLOR = Color.fromRGB(255, 0, 0);
	static final Color DEFAULT_SECONDARY_BALL_COLOR = Color.fromRGB(176, 176, 176);

	static List<?> listForKey(Map<?, ?> map, Object key) {
		if (map.containsKey(key)) return (List<?>) map.get(key);
		return Collections.EMPTY_LIST;
	}

	private FieldElementCollection createFieldElements(Map<String, Object> layoutMap, World world) {
	    FieldElementCollection elements = new FieldElementCollection();

	    Map<String, Object> variables = (Map<String, Object>) layoutMap.get("variables");
	    if (variables != null) {
	        for (String varname : variables.keySet()) {
	            elements.setVariable(varname, variables.get(varname));
	        }
	    }

	    Set<Map<String, Object>> unresolvedElements = new HashSet<Map<String, Object>>();
	    // Initial pass
	    for (Object obj : listForKey(layoutMap, "elements")) {
	        if (!(obj instanceof Map)) continue;
            Map<String, Object> params = (Map<String, Object>) obj;
	        try {
	            elements.addElement(FieldElement.createFromParameters(params, elements, world));
	        }
	        catch (FieldElement.DependencyNotAvailableException ex) {
	            unresolvedElements.add(params);
	        }
	    }

	    return elements;
	}

	void initFromLevel(Map<String, Object> layoutMap, World world) {
		this.width = asFloat(layoutMap.get("width"), 20.0f);
		this.height = asFloat(layoutMap.get("height"), 30.0f);
		this.targetTimeRatio = asFloat(layoutMap.get("targetTimeRatio"));
		this.ballColor = (layoutMap.containsKey("ballcolor"))
		        ? Color.fromList((List<Number>)layoutMap.get("ballcolor"))
		        : DEFAULT_BALL_COLOR;
		this.secondaryBallColor = (layoutMap.containsKey("secondaryBallColor"))
		        ? Color.fromList((List<Number>)layoutMap.get("secondaryBallColor"))
		        : DEFAULT_SECONDARY_BALL_COLOR;
		this.allParameters = layoutMap;
		this.fieldElements = createFieldElements(layoutMap, world);
	}

	public List<FieldElement> getFieldElements() {
		return fieldElements.getAllElements();
	}

	public List<FlipperElement> getFlipperElements() {
		return fieldElements.getFlipperElements();
	}
	public List<FlipperElement> getLeftFlipperElements() {
		return fieldElements.getLeftFlipperElements();
	}
	public List<FlipperElement> getRightFlipperElements() {
	    return fieldElements.getRightFlipperElements();
	}

	public float getBallRadius() {
		return asFloat(allParameters.get("ballradius"), 0.5f);
	}

	public Color getBallColor() {
		return ballColor;
	}

	public Color getSecondaryBallColor() {
	    return secondaryBallColor;
	}

	public int getNumberOfBalls() {
		return (allParameters.containsKey("numballs")) ? ((Number)allParameters.get("numballs")).intValue() : 3;
	}

	public List<Number> getLaunchPosition() {
		Map launchMap = (Map)allParameters.get("launch");
		return (List<Number>)launchMap.get("position");
	}

	public List<Number> getLaunchDeadZone() {
		Map launchMap = (Map)allParameters.get("launch");
		return (List<Number>)launchMap.get("deadzone");
	}

	// can apply random velocity increment if specified by "random_velocity" key
	public List<Float> getLaunchVelocity() {
		Map launchMap = (Map)allParameters.get("launch");
		List<Number> velocity = (List<Number>)launchMap.get("velocity");
		float vx = velocity.get(0).floatValue();
		float vy = velocity.get(1).floatValue();

		if (launchMap.containsKey("random_velocity")) {
			List<Number> delta = (List<Number>)launchMap.get("random_velocity");
			if (delta.get(0).floatValue()>0) vx += delta.get(0).floatValue() * RAND.nextFloat();
			if (delta.get(1).floatValue()>0) vy += delta.get(1).floatValue() * RAND.nextFloat();
		}
		return Arrays.asList(vx, vy);
	}

	public float getWidth() {
		return width;
	}
	public float getHeight() {
		return height;
	}

	/** Returns the desired ratio between real world time and simulation time. The application should adjust the frame rate and/or
	 * time interval passed to Field.tick() to keep the ratio as close to this value as possible.
	 */
	public float getTargetTimeRatio() {
		return targetTimeRatio;
	}

	/** Returns the magnitude of the gravity vector. */
	public float getGravity() {
		return asFloat(allParameters.get("gravity"), 4.0f);
	}

	public String getDelegateClassName() {
		return (String)allParameters.get("delegate");
	}

	/** Returns a value from the "values" map, used to store information independent of the FieldElements.
	 */
	public Object getValueWithKey(String key) {
	    return fieldElements.getVariable(key);
	}
}
