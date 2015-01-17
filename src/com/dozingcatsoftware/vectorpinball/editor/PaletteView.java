package com.dozingcatsoftware.vectorpinball.editor;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableBumperElement;
import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;

public class PaletteView extends VBox {

    public static interface CreateElementCallback {
        public void createElement(Class<? extends EditableFieldElement> elementClass);
    }

    CreateElementCallback createCallback;

    public PaletteView(CreateElementCallback createCallback) {
        super();
        this.createCallback = createCallback;
        Button bumperButton = new Button("Add Bumper");
        this.getChildren().add(bumperButton);
        bumperButton.setOnAction((event) -> createCallback.createElement(EditableBumperElement.class));
    }

}
