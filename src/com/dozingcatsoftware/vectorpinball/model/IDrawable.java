package com.dozingcatsoftware.vectorpinball.model;

public interface IDrawable {
    void draw(Field field, IFieldRenderer renderer);
    int getLayer();
}
