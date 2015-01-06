package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.Arrays;
import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableWallElement;

public class WallElementInspector extends ElementInspector<EditableWallElement> {

    List<TextField> endpointTextFields;

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(new Label("Wall"));
        // Position property has 4 values, 2 each for start and end.
        endpointTextFields = Arrays.asList(
                createEndpointTextField(),
                createEndpointTextField(),
                createEndpointTextField(),
                createEndpointTextField());

        HBox startBox = new HBox();
        startBox.getChildren().add(new Label("Start"));
        startBox.getChildren().addAll(endpointTextFields.get(0), endpointTextFields.get(1));
        box.getChildren().add(startBox);

        HBox endBox = new HBox();
        endBox.getChildren().add(new Label("End"));
        endBox.getChildren().addAll(endpointTextFields.get(2), endpointTextFields.get(3));
        box.getChildren().add(endBox);

        pane.getChildren().add(box);
    }

    TextField createEndpointTextField() {
        DecimalTextField field = new DecimalTextField();
        field.textProperty().addListener((event) -> updateEndpoints());
        return field;
    }

    void updateEndpoints() {
        if (updatingFromExternalChange) return;
        List<Double> endpoints = Arrays.asList(
                Double.valueOf(endpointTextFields.get(0).getText()),
                Double.valueOf(endpointTextFields.get(1).getText()),
                Double.valueOf(endpointTextFields.get(2).getText()),
                Double.valueOf(endpointTextFields.get(3).getText()));
        getElement().setProperty(EditableWallElement.POSITION_PROPERTY, endpoints);
        notifyChanged();
    }

    @Override public void updateCustomControlValues() {
        List<Number> endpoints = (List<Number>)getElement().getProperty(EditableWallElement.POSITION_PROPERTY);
        for (int i=0; i<4; i++) {
            endpointTextFields.get(i).setText(endpoints.get(i).toString());
        }
    }
}
