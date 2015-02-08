package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class ColorSelector extends PropertyEditor<List<Number>> {
    Pane container;
    TextField textField;
    Pane colorBox;

    // Allows lengths of 3 to 8, but only 3, 4, 6, and 8 are valid.
    static final Pattern COLOR_REGEX = Pattern.compile("[0-9a-fA-F]{3,8}");

    public ColorSelector() {
        HBox box = new HBox();
        this.textField = new TextField();
        this.textField.setPrefWidth(75);
        this.textField.setOnAction((event) -> runChangeHandler());
        this.textField.focusedProperty().addListener((target, wasFocused, isFocused) -> {
            if (!isFocused) runChangeHandler();
        });
        box.getChildren().add(textField);
        setContainer(box);
    }

    @Override public List<Number> getValue() {
        String text = textField.getText();
        if (text == null || !COLOR_REGEX.matcher(text).matches()) {
            return null;
        }

        switch (text.length()) {
            case 3:
                return Arrays.asList(
                        17 * Integer.parseInt(text.substring(0, 1), 16),
                        17 * Integer.parseInt(text.substring(1, 2), 16),
                        17 * Integer.parseInt(text.substring(2, 3), 16));
            case 4:
                return Arrays.asList(
                        17 * Integer.parseInt(text.substring(0, 1), 16),
                        17 * Integer.parseInt(text.substring(1, 2), 16),
                        17 * Integer.parseInt(text.substring(2, 3), 16),
                        17 * Integer.parseInt(text.substring(3, 4), 16));
            case 6:
                return Arrays.asList(
                        Integer.parseInt(text.substring(0, 2), 16),
                        Integer.parseInt(text.substring(2, 4), 16),
                        Integer.parseInt(text.substring(4, 6), 16));
            case 8:
                return Arrays.asList(
                        Integer.parseInt(text.substring(0, 2), 16),
                        Integer.parseInt(text.substring(2, 4), 16),
                        Integer.parseInt(text.substring(4, 6), 16),
                        Integer.parseInt(text.substring(6, 8), 16));
            default:
                return null;
        }
    }

    @Override public void updateFromValue(List<Number> value) {
        if (value==null) {
            textField.setText("");
        }
        else if (value.size() == 3) {
            textField.setText(String.format("%02x%02x%02x",
                    value.get(0), value.get(1), value.get(2)));
        }
        else if (value.size() == 4) {
            textField.setText(String.format("%02x%02x%02x%02x",
                    value.get(0), value.get(1), value.get(2), value.get(3)));
        }
        else {
            textField.setText("");
        }
    }
}
