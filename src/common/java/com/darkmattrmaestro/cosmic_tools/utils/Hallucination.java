package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.BlockPosition;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Representation of blocks that do not yet exist, that are rendered as ghostly blocks.
 */
public class Hallucination {
    public ArrayList<BlockPosition> blocks = new ArrayList<BlockPosition>();

    public static final Color borderColor = new Color(0.64f, 0.64f, 0.64f, 1);

    public static Camera rawWorldCamera = null;
    public static final float inflate = -0.05f;
    private float customInflate = 0f;

    public Hallucination() {

    }

    public Hallucination(ArrayList<BlockPosition> blocks) {
        this.blocks = blocks;
    }

    public void draw(ShapeRenderer sr, Vector3 offset) {
        this.draw(sr, offset, borderColor);
    }

    public void draw(ShapeRenderer sr, Vector3 offset, Color border) {}

    public Hallucination setCustomInflate(float customInflate) {
        this.customInflate = customInflate;
        return this;
    }

    public float getInflate() {
        return inflate + this.customInflate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Hallucination hallucination = (Hallucination) o;
        return blocks.equals(hallucination.blocks);
    }

    @Override
    public int hashCode() {
            return Objects.hash(blocks);
        }
}
