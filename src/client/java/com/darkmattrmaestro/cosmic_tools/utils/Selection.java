package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.gamestates.GameState;
import com.darkmattrmaestro.cosmic_tools.volume.RealVolume;

import java.util.Objects;

public class Selection {
    public int minX, minY, minZ;
    public int maxX, maxY, maxZ;

    public static Camera rawWorldCamera = GameState.IN_GAME.getWorldCamera();
    public static final float inflate = 0.01f;
    //public static final float borderInflate = 0.001f;
    private float customInflate = 0f;

    public Selection(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
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

    public int sizeX() {
        return maxX - minX + 1;
    }

    public int sizeY() {
        return maxY - minY + 1;
    }

    public int sizeZ() {
        return maxZ - minZ + 1;
    }

    public RealVolume blankVolume() {
        return RealVolume.of(sizeX(), sizeY(), sizeZ());
    }


    public void draw(ShapeRenderer sr, Color fill, Color border){
        sr.setProjectionMatrix(rawWorldCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(fill);
        float width = maxX + getInflate() - (minX - getInflate()) + 1;
        float height = maxY + getInflate() - (minY - getInflate()) + 1;
        float depth = maxZ + getInflate() - (minZ - getInflate()) + 1;
        sr.box(minX - getInflate(), minY - getInflate(), maxZ + getInflate() + 1, width, height, depth);
        sr.box(maxX + getInflate() + 1, maxY + getInflate() + 1, minZ - getInflate(), -width, -height, -depth);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(border);
        sr.box(minX - getInflate(), minY - getInflate(), maxZ + getInflate() + 1, width, height, depth);
        //sr.box(bb.min.x + eps * 0.5f, bb.min.y + eps * 0.5f, bb.min.z + bb.getDepth() - eps * 0.5f, bb.getWidth() - eps, bb.getHeight() - eps, bb.getDepth() - eps);
        sr.end();
    }

    public void drawBorder(ShapeRenderer sr, Color border){
        sr.setProjectionMatrix(rawWorldCamera.combined);
        float width = maxX + getInflate() - (minX - getInflate()) + 1;
        float height = maxY + getInflate() - (minY - getInflate()) + 1;
        float depth = maxZ + getInflate() - (minZ - getInflate()) + 1;

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(border);
        sr.box(minX - getInflate(), minY - getInflate(), maxZ + getInflate() + 1, width, height, depth);
        //sr.box(bb.min.x + eps * 0.5f, bb.min.y + eps * 0.5f, bb.min.z + bb.getDepth() - eps * 0.5f, bb.getWidth() - eps, bb.getHeight() - eps, bb.getDepth() - eps);
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
        return minX == selection.minX && minY == selection.minY && minZ == selection.minZ && maxX == selection.maxX && maxY == selection.maxY && maxZ == selection.maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
