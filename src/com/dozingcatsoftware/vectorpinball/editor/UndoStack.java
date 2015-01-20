package com.dozingcatsoftware.vectorpinball.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableField;

public class UndoStack {

    static class Entry {
        final Map<String, Object> fieldMap;
        final Set<Integer> selectedIndexes;

        Entry(Map<String, Object> fmap, Set<Integer> selection) {
            fieldMap = fmap;
            selectedIndexes = selection;
        }
    }

    EditableField editableField;
    List<Entry> entries = new ArrayList<>();
    int currentEntryIndex = -1;

    public void setEditableField(EditableField field) {
        editableField = field;
    }

    public void pushSnapshot() {
        // Remove anything after currentEntryIndex, and append to end.
    }

    public boolean canUndo() {
        return currentEntryIndex>0;
    }

    public boolean canRedo() {
        return entries.size()>0 && currentEntryIndex<entries.size()-1;
    }

    public void undo() {
        if (!canUndo()) {
            throw new IllegalStateException("Can't undo");
        }
        currentEntryIndex -= 1;
        // restore EditableField
    }

    public void redo() {
        if (!canRedo()) {
            throw new IllegalStateException("Can't undo");
        }
        currentEntryIndex += 1;
        // restore EditableField
    }
}
