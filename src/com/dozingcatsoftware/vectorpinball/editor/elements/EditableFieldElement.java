package com.dozingcatsoftware.vectorpinball.editor.elements;

import static com.dozingcatsoftware.vectorpinball.util.MathUtils.asDouble;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.model.Color;
import com.dozingcatsoftware.vectorpinball.model.IFieldRenderer;
import com.dozingcatsoftware.vectorpinball.model.Point;

public abstract class EditableFieldElement implements PropertyContainer {

    public static final String CLASS_PROPERTY = "class";
    public static final String ID_PROPERTY = "id";
    public static final String COLOR_PROPERTY = "color";
    public static final String SCORE_PROPERTY = "score";

    static final Color DEFAULT_WALL_COLOR = Color.fromRGB(64, 64, 160);
    static final String CLASS_PREFIX = "com.dozingcatsoftware.vectorpinball.editor.elements.Editable";

    private Map<String, Object> properties;
    private boolean propertiesDirty = false;
    private Runnable changeHandler;

    public void initFromPropertyMap(Map<String, Object> props) {
        properties = props;
        propertiesDirty = true;
    }

    public void initAsNewElement(EditableField field) {
        properties = new HashMap<>();
        properties.put(CLASS_PROPERTY, getClass().getName().replace(CLASS_PREFIX, ""));
        addPropertiesForNewElement(properties, field);
        propertiesDirty = true;
    }

    /**
     * Must be implemented by subclasses to fill a map of properties for the
     * element when it has been added to an existing field.
     */
    protected abstract void addPropertiesForNewElement(Map<String, Object> props, EditableField field);

    public void setChangeHandler(Runnable handler) {
        changeHandler = handler;
    }

    public Map<String, Object> getPropertyMap() {
        return properties;
    }

    @Override public void setProperty(String key, Object value) {
        properties.put(key, value);
        propertiesDirty = true;
        if (changeHandler != null) {
            changeHandler.run();
        }
    }

    @Override public void removeProperty(String key) {
        properties.remove(key);
        propertiesDirty = true;
        if (changeHandler != null) {
            changeHandler.run();
        }
    }

    @Override public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    @Override public Object getProperty(String key) {
        return properties.get(key);
    }

    public double[] getDoubleArrayProperty(String key) {
        List<?> list = (List<?>) getProperty(key);
        double[] result = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = asDouble(list.get(i));
        }
        return result;
    }

    public static EditableFieldElement createFromParameters(Map params) {
        if (!params.containsKey(CLASS_PROPERTY)) {
            throw new IllegalArgumentException("class not specified for element: " + params);
        }
        EditableFieldElement self = null;
        try {
            // if package not specified, use this package
            String className = (String)params.get(CLASS_PROPERTY);
            if (className.indexOf('.')==-1) {
                className = CLASS_PREFIX + className;
            }
            Class elementClass = Class.forName(className);
            self = (EditableFieldElement) elementClass.newInstance();
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        self.initFromPropertyMap(params);
        return self;
    }

    protected void refreshIfDirty() {
        if (propertiesDirty) {
            refreshInternalValues();
            propertiesDirty = false;
        }
    }

    protected void refreshInternalValues() {}

    /**
     * Gets the current color by using the defined color if set and the default color if not.
     */
    protected Color currentColor(Color defaultColor) {
        Color color = properties.containsKey(COLOR_PROPERTY) ?
                Color.fromList((List<Number>)properties.get(COLOR_PROPERTY)) : defaultColor;

        // If very dark, make brighter for display.
        int rgbSum = color.red + color.green + color.blue;
        if (rgbSum < 192 || color.alpha < 64) {
            int extra = Math.max(0, (192 - rgbSum) / 3);
            color = Color.fromRGB(
                    color.red + extra, color.green + extra, color.blue + extra, Math.max(color.alpha, 128));
        }
        return color;
    }

    /**
     * Draws the element in the context of a field editor. By default this just calls draw(), but some
     * elements may want to do something else, for example if they're normally invisible.
     */
    abstract public void drawForEditor(IFieldRenderer renderer, boolean isSelected);

    /**
     * Determines whether a point is sufficiently close to any part of this element.
     */
    abstract public boolean isPointWithinDistance(Point point, double distance);

    /**
     * Called when a drag operation begins. Will be followed by any number of handleDrag calls.
     * This method can be used to store data needed to update correctly for drags, for example
     * which part of the element the drag started at.
     */
    public void startDrag(Point point) {}

    /**
     * Called when a drag is in progress to update the properties map. drawForEditor() may be
     * called any number of times during a drag.
     */
    abstract public void handleDrag(Point point, Point deltaFromStart, Point deltaFromPrevious);

}
