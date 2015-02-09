package com.dozingcatsoftware.vectorpinball.editor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableField;
import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;
import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.FieldDriver;
import com.dozingcatsoftware.vectorpinball.util.CollectionUtils;
import com.dozingcatsoftware.vectorpinball.util.JSONUtils;

// Need to edit project as described in
// http://stackoverflow.com/questions/24467931/using-javafx-jdk-1-8-0-05-in-eclipse-luna-does-not-work

public class Main extends Application {
    public static void main(String[] args) {
        //test();
        launch(args);
    }

    static void test() {
        Map<String, Object> m = new HashMap<>();
        m.put("foo", Arrays.asList(1, 2, 3));
        System.out.println(m);
        Map<String, Object> m2 = CollectionUtils.mutableDeepCopyOfMap(m);
        ((List)m2.get("foo")).add(4);
        System.out.println(m);
        System.out.println(m2);
        System.exit(0);
    }

    enum EditorState {
        EDITING,
        SAMPLE_BALL,
        SAMPLE_GAME,
    }

    static String WINDOW_TITLE_PREFIX = "Vector Pinball: ";

    Stage mainStage;
    ScrollPane fieldScroller;
    Canvas fieldCanvas;
    PaletteView palette;
    ElementInspectorView inspector;

    Field field;
    EditableField editableField;
    FxCanvasRenderer renderer;
    UndoStack undoStack;

    FieldDriver fieldDriver;
    EditorState editorState = EditorState.EDITING;
    Map<String, Object> fieldMap = null;

    FileSystem fileSystem = FileSystems.getDefault();
    Path savedFilePath;

    @Override public void start(Stage primaryStage) {
        this.mainStage = primaryStage;

        editableField = new EditableField();
        editableField.setElementChangedCallback(this::handleElementChangeFromField);
        editableField.setSelectionChangedCallback(this::handleSelectionChange);

        undoStack = new UndoStack();
        undoStack.setEditableField(editableField);

        renderer = new FxCanvasRenderer();
        renderer.setEditableField(editableField);
        renderer.setUndoStack(undoStack);

        palette = new PaletteView(this::createElement);

        inspector = new ElementInspectorView();
        inspector.setChangeCallback(this::handleElementChangeFromInspector);
        inspector.setEditableField(editableField);
        inspector.setUndoStack(undoStack);

        int width = 1100;
        int height = 1100;

        GridPane root = new GridPane();

        ColumnConstraints col1 = new ColumnConstraints(350);
        ColumnConstraints col2 = new ColumnConstraints(0, 700, Double.MAX_VALUE);
        col2.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        root.getRowConstraints().addAll(row1, row2);

        palette.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
        GridPane.setConstraints(palette, 0, 0);

        ScrollPane inspectorScroller = new ScrollPane();
        inspectorScroller.setContent(inspector);
        inspectorScroller.setStyle("-fx-background: lightblue;");
        inspectorScroller.setPadding(new Insets(10, 10, 10, 10));
        inspector.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));
        GridPane.setConstraints(inspectorScroller, 0, 1);

        VBox fieldBox = new VBox();
        HBox fieldControls = new HBox(10);
        fieldControls.setPrefHeight(60);
        fieldControls.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));

        Button launchBallButton = new Button("Launch Ball");
        launchBallButton.setOnAction((event) -> launchSingleBall());
        fieldControls.getChildren().add(launchBallButton);

        Button endGameButton = new Button("Stop Game");
        endGameButton.setOnAction((event) -> stopGame());
        fieldControls.getChildren().add(endGameButton);

        /*
        Button undoButton = new Button("Undo");
        undoButton.setOnAction((event) -> undoEdit());
        fieldControls.getChildren().add(undoButton);

        Button redoButton = new Button("Redo");
        redoButton.setOnAction((event) -> redoEdit());
        fieldControls.getChildren().add(redoButton);
        */

        fieldScroller = new ScrollPane();
        fieldScroller.setStyle("-fx-background: black;");
        VBox.setVgrow(fieldScroller, Priority.ALWAYS);

        createCanvas(700, 1000);

        fieldBox.getChildren().addAll(fieldControls, fieldScroller);

        GridPane.setConstraints(fieldBox, 1, 0, 1, 2);

        MenuBar menuBar = buildMenuBar();
        root.getChildren().addAll(menuBar, palette, inspectorScroller, fieldBox);

        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.show();

        loadBuiltInLevel(1);
    }

    MenuItem createMenuItem(String label, String shortcutChar, Runnable onAction) {
        MenuItem item = new MenuItem(label);
        if (shortcutChar != null) {
            item.setAccelerator(new KeyCharacterCombination(shortcutChar, KeyCombination.SHORTCUT_DOWN));
        }
        if (onAction != null) {
            item.setOnAction((event) -> onAction.run());
        }
        return item;
    }
    MenuBar buildMenuBar() {
        Menu fileMenu = new Menu("File");

        Menu newFromTemplateMenu = new Menu("New From Template");
        newFromTemplateMenu.getItems().addAll(
                createMenuItem("Table 1", null, () -> loadBuiltInLevel(1)),
                createMenuItem("Table 2", null, () -> loadBuiltInLevel(2)),
                createMenuItem("Table 3", null, () -> loadBuiltInLevel(3))
        );

        MenuItem openItem = new MenuItem("Open");
        openItem.setAccelerator(new KeyCharacterCombination("O", KeyCombination.SHORTCUT_DOWN));
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCharacterCombination("S", KeyCombination.SHORTCUT_DOWN));
        fileMenu.getItems().addAll(
                createMenuItem("New Table", "N", null),
                newFromTemplateMenu,
                new SeparatorMenuItem(),
                createMenuItem("Open", "O", this::openFile),
                createMenuItem("Save", "S", this::saveFile)
        );

        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = createMenuItem("Undo", "Z", this::undoEdit);
        MenuItem redoItem = createMenuItem("Redo", null, this::redoEdit);
        redoItem.setAccelerator(new KeyCharacterCombination("Z", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        editMenu.getItems().addAll(undoItem, redoItem);

        MenuBar mbar = new MenuBar();
        mbar.getMenus().addAll(fileMenu, editMenu);
        mbar.setUseSystemMenuBar(true);
        System.out.println(mbar.isUseSystemMenuBar());
        return mbar;
    }

    void createCanvas(int width, int height) {
        fieldCanvas = new Canvas(width, height);
        fieldScroller.setContent(fieldCanvas);
        fieldCanvas.setOnMousePressed(this::handleCanvasMousePressed);
        fieldCanvas.setOnMouseReleased(this::handleCanvasMouseReleased);
        fieldCanvas.setOnMouseDragged(this::handleCanvasMouseDragged);
    }

    void loadFieldMap(Map<String, Object> map) {
        fieldMap = map;
        displayForEditing();

        undoStack.clearStack();
        undoStack.pushSnapshot();
        savedFilePath = null;
    }

    void loadBuiltInLevel(int level) {
        System.out.println("Reading table");
        JarFileFieldReader fieldReader = new JarFileFieldReader();
        loadFieldMap(fieldReader.layoutMapForLevel(level));
        mainStage.setTitle(WINDOW_TITLE_PREFIX + "Table Template " + level);
    }

    void displayForEditing() {
        renderer.setCanvas(fieldCanvas);
        editableField.initFromProperties(fieldMap);
        renderer.doDraw();
        inspector.updateInspectorValues();
        editorState = EditorState.EDITING;
    }

    void launchSingleBall() {
        if (fieldDriver==null) {
            field = new Field();
            field.resetForLevel(editableField.getPropertyMapSnapshot());
            renderer.setField(field);

            fieldDriver = new FieldDriver();
            fieldDriver.setFieldRenderer(renderer);
            fieldDriver.setField(field);
            fieldDriver.start();
        }
        field.getDelegate().gameStarted(field);
        field.removeDeadBalls();
        field.launchBall();
        editorState = EditorState.SAMPLE_BALL;

        // Start polling every second to detect lost ball?
    }

    void stopGame() {
        if (fieldDriver != null) {
            fieldDriver.stop();
        }
        fieldDriver = null;

        renderer.setEditableField(editableField);
        renderer.doDraw();
        editorState = EditorState.EDITING;
    }

    void handleCanvasMousePressed(MouseEvent event) {
        switch (editorState) {
            case SAMPLE_GAME:
            case SAMPLE_BALL:
                field.setAllFlippersEngaged(true);
                break;
            case EDITING:
                renderer.handleEditorMouseDown(event);
                break;
        }
    }

    void handleCanvasMouseReleased(MouseEvent event) {
        switch (editorState) {
            case SAMPLE_GAME:
            case SAMPLE_BALL:
                field.setAllFlippersEngaged(false);
                break;
            case EDITING:
                renderer.handleEditorMouseUp(event);
                break;
        }
    }

    void handleCanvasMouseDragged(MouseEvent event) {
        switch (editorState) {
            case EDITING:
                renderer.handleEditorMouseDrag(event);
                break;
            default:
                break;
        }
    }

    void handleSelectionChange() {
        inspector.update();
    }

    void handleElementChangeFromField() {
        inspector.updateInspectorValues();
    }

    void handleElementChangeFromInspector() {
        renderer.doDraw();
        undoStack.pushSnapshot();
    }

    void createElement(Class<? extends EditableFieldElement> elementClass) {
        if (editableField!=null && fieldDriver==null) {
            EditableFieldElement newElement = editableField.addNewElement(elementClass);
            // Push to undo stack before selecting the new element, so that the previous
            // selection will be restored if we undo.
            undoStack.pushSnapshot();
            editableField.selectElement(newElement);
            renderer.doDraw();
        }
    }

    void undoEdit() {
        if (undoStack.canUndo()) {
            undoStack.undo();
            renderer.doDraw();
            inspector.updateInspectorValues();
        }
    }

    void redoEdit() {
        if (undoStack.canRedo()) {
            undoStack.redo();
            renderer.doDraw();
            inspector.updateInspectorValues();
        }
    }

    void openFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Table");
        File selectedFile = chooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            Path openFilePath = selectedFile.toPath();
            // TODO: Check for implausibly large files.
            byte[] contents = null;
            Map<String, Object> map = null;
            try {
                contents = Files.readAllBytes(openFilePath);
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            try {
                map = JSONUtils.mapFromJSONString(new String(contents, StandardCharsets.UTF_8));
            }
            catch (JSONUtils.ParsingException ex) {
                ex.printStackTrace();
                return;
            }
            loadFieldMap(map);
            savedFilePath = openFilePath;
            mainStage.setTitle(WINDOW_TITLE_PREFIX + savedFilePath.toString());
        }
    }

    void saveFile() {
        if (savedFilePath != null) {
            writeToSavedFilePath();
        }
        else {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Table");
            File selectedFile = chooser.showSaveDialog(mainStage);
            if (selectedFile != null) {
                savedFilePath = selectedFile.toPath();
                writeToSavedFilePath();
                mainStage.setTitle("Vector Pinball: " + savedFilePath.toString());
            }
        }
    }

    void writeToSavedFilePath() {
        String fileText = JSONUtils.jsonStringFromObject(editableField.getPropertyMapSnapshot());
        try {
            Files.write(savedFilePath, fileText.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
