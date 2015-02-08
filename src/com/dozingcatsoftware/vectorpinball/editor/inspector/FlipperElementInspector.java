package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFlipperElement;

public class FlipperElementInspector extends ElementInspector {

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(createColorSelectorWithLabel("Color", EditableFlipperElement.COLOR_PROPERTY));
        box.getChildren().add(createPositionStringFieldsWithLabel("Center", EditableFlipperElement.POSITION_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Length", EditableFlipperElement.LENGTH_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Min angle", EditableFlipperElement.MIN_ANGLE_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Max angle", EditableFlipperElement.MAX_ANGLE_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Up speed", EditableFlipperElement.UP_SPEED_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Down speed", EditableFlipperElement.DOWN_SPEED_PROPERTY));
        pane.getChildren().add(box);
    }

}
