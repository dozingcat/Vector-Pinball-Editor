package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableWallArcElement;

public class WallArcElementInspector extends ElementInspector {

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(createColorSelectorWithLabel("Color", EditableWallArcElement.COLOR_PROPERTY));
        box.getChildren().add(createPositionStringFieldsWithLabel("Center", EditableWallArcElement.CENTER_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Radius", EditableWallArcElement.RADIUS_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("X Radius", EditableWallArcElement.X_RADIUS_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Y Radius", EditableWallArcElement.Y_RADIUS_PROPERTY));
        box.getChildren().add(createIntegerFieldWithLabel("# Segments", EditableWallArcElement.NUM_SEGMENTS_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Min angle", EditableWallArcElement.MIN_ANGLE_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Max angle", EditableWallArcElement.MAX_ANGLE_PROPERTY));
        pane.getChildren().add(box);
    }

}
