package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableRolloverGroupElement;

public class RolloverGroupElementInspector extends ElementInspector<EditableRolloverGroupElement> {

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(new Label("TODO"));
        pane.getChildren().add(box);
    }

}
