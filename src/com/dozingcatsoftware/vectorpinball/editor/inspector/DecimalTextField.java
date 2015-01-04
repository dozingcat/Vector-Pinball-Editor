package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.regex.Pattern;

import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;

public class DecimalTextField extends TextField {

    static Pattern DECIMAL_PATTERN = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)");

    Double minValue = null;
    Double maxValue = null;

    public void setMinValue(Double value) {
        minValue = value;
    }

    public void setMaxValue(Double value) {

    }

    boolean isValidDecimal(String text) {
        if (text==null || text.length()==0) return true;
        if (!DECIMAL_PATTERN.matcher(text).matches()) return false;
        try {
            double value = Double.valueOf(text);
            return ((minValue==null || value>=minValue) && (maxValue==null || value<=maxValue));
        }
        catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override public void replaceText(int start, int end, String text) {
        System.out.println("replaceText: " + start + " " + end + " " + text);
        String origText = this.getText();
        String newText = origText.substring(0, start) + text + origText.substring(end);
        System.out.println("replaceText orig: " + getText() + " new: " + newText);
        if (isValidDecimal(newText)) {
            super.replaceText(start, end, text);
        }
    }

    @Override public void replaceSelection(String text) {
        String origText = this.getText();
        IndexRange selection = this.getSelection();
        String newText = origText.substring(0, selection.getStart()) + text + origText.substring(selection.getEnd());
        System.out.println("replaceSelection orig: " + getText() + " new: " + newText);
        if (isValidDecimal(newText)) {
            super.replaceSelection(text);
        }
    }
}
