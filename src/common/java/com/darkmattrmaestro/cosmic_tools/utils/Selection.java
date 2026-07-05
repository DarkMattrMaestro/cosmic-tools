package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.darkmattrmaestro.cosmic_tools.Constants;
import finalforeach.cosmicreach.blocks.BlockPosition;

import java.util.Objects;

/**
 * Representation of blocks that do not yet exist, that are rendered as ghostly blocks.
 */
public class Selection {
    public Vector3Int minPos;
    public Vector3Int maxPos;

    public static final Color fillColor = new Color(0.64f, 0.64f, 0.64f, 0.2f);
    public static final Color borderColor = new Color(0.64f, 0.64f, 0.64f, 1);

    public static Camera rawWorldCamera = null;
    public static final float inflate = 0.01f;
    private float customInflate = 0f;

    public Selection(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minPos = new Vector3Int(minX, minY, minZ);
        this.maxPos = new Vector3Int(maxX, maxY, maxZ);
    }

    public Selection(Vector3Int minPos, Vector3Int maxPos) {
        this.minPos = minPos.cpy();
        this.maxPos = maxPos.cpy();
    }

    public static Selection of(int x1, int y1, int z1, int x2, int y2, int z2) {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);
        return new Selection(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static Selection of(BlockPosition pos1, BlockPosition pos2) {
        int minX = Math.min(pos1.getGlobalX(), pos2.getGlobalX());
        int minY = Math.min(pos1.getGlobalY(), pos2.getGlobalY());
        int minZ = Math.min(pos1.getGlobalZ(), pos2.getGlobalZ());
        int maxX = Math.max(pos1.getGlobalX(), pos2.getGlobalX());
        int maxY = Math.max(pos1.getGlobalY(), pos2.getGlobalY());
        int maxZ = Math.max(pos1.getGlobalZ(), pos2.getGlobalZ());
        return new Selection(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static Selection of(Vector3Int firstPos, Vector3Int secondPos) {
        return new Selection(
                firstPos.optimizeWith(secondPos, Math::min),
                firstPos.optimizeWith(secondPos, Math::max)
        );
    }

    public Selection reposition(BlockPosition newOrigin) {
        return this.reposition(newOrigin.getGlobalX(), newOrigin.getGlobalY(), newOrigin.getGlobalZ());
    }

    public Selection reposition(Vector3Int newOrigin) {
        return this.reposition(newOrigin.x, newOrigin.y, newOrigin.z);
    }

    public Selection reposition(int originX, int originY, int originZ) {
        this.maxPos.sub(this.minPos).add(originX, originY, originZ);
        this.minPos.set(originX, originY, originZ);

        return this;
    }

    public void draw(ShapeRenderer sr) {
        this.draw(sr, fillColor, borderColor);
    }

    public int sizeX() {
        return maxPos.x - minPos.x + 1;
    }

    public int sizeY() {
        return maxPos.y - minPos.y + 1;
    }

    public int sizeZ() {
        return maxPos.z - minPos.z + 1;
    }

    public int nBlocks() {
        return sizeX() * sizeY() * sizeZ();
    }

    public void draw(ShapeRenderer sr, Color fill, Color border) {
        sr.setProjectionMatrix(Selection.rawWorldCamera.combined);

        float x = this.maxPos.x + this.getInflate() + 1;
        float y = this.minPos.y - this.getInflate();
        float z = this.minPos.z - this.getInflate();
        float width = -(this.sizeX() + 2*this.getInflate());
        float height = this.sizeY() + 2*this.getInflate();
        float depth = -(this.sizeZ() + 2*this.getInflate());

        // Fill
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(fill);
        sr.box(x, y, z, width, height, depth);
        sr.end();

        // Outline
        Gdx.gl.glLineWidth(2);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(border);
        sr.box(x, y, z, width, height, depth);
        sr.end();
    }

    public Selection setCustomInflate(float customInflate) {
        this.customInflate = customInflate;
        return this;
    }

    public float getInflate() {
        return inflate + this.customInflate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Selection selection = (Selection) o;
        return minPos.equals(selection.minPos) && maxPos.equals(selection.maxPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minPos, maxPos);
    }
}
