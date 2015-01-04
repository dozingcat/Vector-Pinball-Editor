package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableBumperElement;

public class BumperElementInspector extends ElementInspector<EditableBumperElement> {

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(new Label("Bumper"));
        box.getChildren().add(createDecimalTextFieldWithLabel("Radius", EditableBumperElement.RADIUS_PROPERTY));

        pane.getChildren().add(box);
    }

}
