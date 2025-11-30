package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.gamestates.GameState;

public class Connection {
    public int startX, startY, startZ;
    public int endX, endY, endZ;

    public static Camera rawWorldCamera = GameState.IN_GAME.getWorldCamera();
    public static final float inflate = 0.01f;
    //public static final float borderInflate = 0.001f;

    public Connection(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    public static Connection of(int startX, int startY, int startZ, int endX, int endY, int endZ) {
       return new Connection(startX, startY, startZ, endX, endY, endZ);
    }

    public static Connection of(BlockPosition pos1, BlockPosition pos2) {
        return new Connection(pos1.getGlobalX(), pos1.getGlobalY(), pos1.getGlobalZ(), pos2.getGlobalX(), pos2.getGlobalY(), pos2.getGlobalZ());
    }

//    public int lenX() {
//        return maxX - minX + 1;
//    }
//
//    public int lenY() {
//        return maxY - minY + 1;
//    }
//
//    public int lenZ() {
//        return maxZ - minZ + 1;
//    }

//    public RealVolume blankVolume() {
//        return RealVolume.of(sizeX(), sizeY(), sizeZ());
//    }


    public void draw(ShapeRenderer sr, Color colour){
        sr.setProjectionMatrix(rawWorldCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(colour);
        sr.line(startX, startY, startZ, endX, endY, endZ);
        sr.end();
    }
}
