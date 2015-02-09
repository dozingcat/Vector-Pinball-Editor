package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableWallArcElement;

public class WallArcElementInspector extends ElementInspector {

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(createStringFieldWithLabel(EditableWallArcElement.ID_PROPERTY, "ID"));
        box.getChildren().add(createColorSelectorWithLabel(EditableWallArcElement.COLOR_PROPERTY, "Color"));
        box.getChildren().add(createPositionStringFieldsWithLabel(EditableWallArcElement.CENTER_PROPERTY, "Center"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableWallArcElement.RADIUS_PROPERTY, "Radius"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableWallArcElement.X_RADIUS_PROPERTY, "X Radius"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableWallArcElement.Y_RADIUS_PROPERTY, "Y Radius"));
        box.getChildren().add(createIntegerFieldWithLabel(EditableWallArcElement.NUM_SEGMENTS_PROPERTY, "# Segments"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableWallArcElement.MIN_ANGLE_PROPERTY, "Min angle"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableWallArcElement.MAX_ANGLE_PROPERTY, "Max angle"));
        pane.getChildren().add(box);
    }

}
