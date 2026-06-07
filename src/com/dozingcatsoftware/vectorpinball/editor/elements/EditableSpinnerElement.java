package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;

import com.dozingcatsoftware.vectorpinball.editor.IEditableFieldRenderer;
import com.dozingcatsoftware.vectorpinball.editor.Point;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EditableSpinnerElement extends EditableFieldElement {
    public static final String CENTER_PROPERTY = "position";
    public static final String RADIUS_PROPERTY = "radius";
    public static final String MIN_ACTIVATION_SPEED_PROPERTY = "minActivationSpeed";
    public static final String BASE_CYCLES_PER_SECOND_PROPERTY = "baseCyclesPerSecond";
    public static final String MIN_CYCLES_PER_SECOND_PROPERTY = "minCyclesPerSecond";
    public static final String SPEED_DECAY_RATE_PROPERTY = "speedDecayRate";

    @Override
    protected void addPropertiesForNewElement(Map<String, Object> props, EditableField field) {
        props.put(CENTER_PROPERTY, Arrays.asList("-0.5", "-0.5"));
        props.put(RADIUS_PROPERTY, "0.5");
        props.put(COLOR_PROPERTY, Arrays.asList(255, 255, 255));
    }

    private double centerX() {
        return asDouble(((List<Object>)getProperty(CENTER_PROPERTY)).get(0));
    }

    private double centerY() {
        return asDouble(((List<Object>)getProperty(CENTER_PROPERTY)).get(1));
    }

    private double radius() {
        return asDouble(getProperty(RADIUS_PROPERTY));
    }

    @Override
    public void drawForEditor(IEditableFieldRenderer renderer, boolean isSelected) {
        renderer.frameCircle(centerX(), centerY(), radius(), currentColor(0));
    }

    @Override
    public boolean isPointWithinDistance(Point point, double distance) {
        double dist = point.distanceTo(centerX(), centerY());
        return dist <= radius();
    }

    @Override
    public void translate(Point offset) {
        setProperty(CENTER_PROPERTY, Arrays.asList(
                centerX() + offset.x,
                centerY() + offset.y));

    }
}
