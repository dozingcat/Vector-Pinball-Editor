package com.dozingcatsoftware.vectorpinball.model;

public interface IDrawable {
    void draw(IFieldRenderer renderer);
    int getLayer();
}
