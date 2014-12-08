package com.dozingcatsoftware.vectorpinball.editor;

import java.util.Map;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.FieldDriver;

// Need to edit project as described in
// http://stackoverflow.com/questions/24467931/using-javafx-jdk-1-8-0-05-in-eclipse-luna-does-not-work

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    Canvas fieldCanvas;

    @Override public void start(Stage primaryStage) {
        /*
         * primaryStage.setTitle("Hello World!"); Button btn = new Button();
         * btn.setText("Say 'Hello World'"); btn.setOnAction((event) ->
         * System.out.println("Hello Lambda!"));
         *
         * int width = 1000; int height = 1000;
         *
         * Canvas canvas = new Canvas(width, height);
         * drawMandelbrot(canvas.getGraphicsContext2D(), width, height);
         *
         *
         * StackPane root = new StackPane(); root.getChildren().add(canvas);
         * //root.getChildren().add(btn);
         */
        int width = 1100;
        int height = 1100;

        GridPane root = new GridPane();

        ColumnConstraints col1 = new ColumnConstraints(300);
        ColumnConstraints col2 = new ColumnConstraints(0, 700, Double.MAX_VALUE);
        col2.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        root.getRowConstraints().addAll(row1, row2);

        VBox palette = new VBox();
        palette.setBackground(new Background(new BackgroundFill(Paint.valueOf("red"), null, null)));
        GridPane.setConstraints(palette, 0, 0);

        VBox inspector = new VBox();
        inspector.setBackground(new Background(new BackgroundFill(Paint.valueOf("blue"), null, null)));
        GridPane.setConstraints(inspector, 0, 1);

        VBox fieldBox = new VBox();
        HBox fieldControls = new HBox(10);
        fieldControls.setPrefHeight(60);
        fieldControls.setBackground(new Background(new BackgroundFill(Paint.valueOf("green"), null, null)));

        ScrollPane fieldScroller = new ScrollPane();
        fieldScroller.setStyle("-fx-background: black;");
        VBox.setVgrow(fieldScroller, Priority.ALWAYS);

        fieldCanvas = new Canvas(700, 1000);
        fieldScroller.setContent(fieldCanvas);

        fieldBox.getChildren().addAll(fieldControls, fieldScroller);

        GridPane.setConstraints(fieldBox, 1, 0, 1, 2);

        root.getChildren().addAll(palette, inspector, fieldBox);

        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.show();

        startGame();
    }

    void startGame() {
        System.out.println("Reading table");
        JarFileFieldReader fieldReader = new JarFileFieldReader();

        Map<String, Object> fieldMap = fieldReader.layoutMapForLevel(2);
        Field field = new Field();
        field.resetForLevel(fieldMap);

        FxCanvasRenderer renderer = new FxCanvasRenderer();
        renderer.setCanvas(fieldCanvas);
        renderer.setField(field);
        renderer.doDraw();

        FieldDriver driver = new FieldDriver();
        driver.setFieldRenderer(renderer);
        driver.setField(field);
        driver.start();

        field.launchBall();
    }

    void drawMandelbrot(GraphicsContext gc, int width, int height) {
        long t1 = System.nanoTime();
        double minx = -2.0;
        double maxx = 0.5;
        double miny = -1.25;
        double maxy = 1.25;
        /*
         * long sum = 0; for (int row=0; row<height; row++) { double y = miny +
         * (maxy-miny)*(1.0*row/(height-1)); for (int col=0; col<width; col++) {
         * double x = minx + (maxx-minx)*(1.0*col/(width-1)); int iters =
         * mandelbrotIters(x, y, 128);
         *
         * if (iters==128) { gc.setFill(Color.BLACK); } else {
         * gc.setFill(Color.rgb(255-iters, 255-iters, 255)); } gc.fillRect(col,
         * row, 1, 1);
         *
         * sum += iters; } }
         */
        int scale = 2;
        Image image = createMandelbrotImage(minx, miny, maxx, maxy, scale * width, scale * height);
        gc.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), 0, 0, width, height);
        double secs = (System.nanoTime() - t1) / 1e9;
        System.out.println("rendered in " + secs);
    }

    int mandelbrotIters(double seedR, double seedI, int maxIters) {
        int iters = 0;
        double re = 0;
        double im = 0;
        while (iters < maxIters && re * re + im * im < 4) {
            double nextRe = re * re - im * im + seedR;
            double nextIm = 2 * re * im + seedI;
            re = nextRe;
            im = nextIm;
            iters++;
        }
        return iters;
    }

    Image createMandelbrotImage(double minx, double miny, double maxx,
            double maxy, int width, int height) {
        int black = 0xff << 24;
        int maxIters = 128;

        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        for (int row = 0; row < height; row++) {
            double y = miny + (maxy - miny) * (1.0 * row / (height - 1));
            for (int col = 0; col < width; col++) {
                double x = minx + (maxx - minx) * (1.0 * col / (width - 1));
                int iters = mandelbrotIters(x, y, maxIters);
                if (iters == maxIters) {
                    writer.setArgb(col, row, black);
                } else {
                    int grey = 255 - iters;
                    writer.setArgb(col, row, (0xff << 24) | (grey << 16) | (grey << 8) | grey);
                }
            }
        }

        return image;
    }
}
