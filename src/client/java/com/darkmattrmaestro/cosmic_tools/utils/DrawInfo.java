package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.graphics.Color;

import java.util.function.Supplier;

public class DrawInfo {
    public Supplier<Selection> selectionSupplier;
    public Color fillColor;
    public Color borderColor;
    public float customInflate;

    public DrawInfo(Supplier<Selection> selectionSupplier, Color fillColor, Color borderColor, float customInflate) {
        this.selectionSupplier = selectionSupplier;
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.customInflate = customInflate;
    }
}
