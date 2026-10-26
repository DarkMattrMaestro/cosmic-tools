package com.darkmattrmaestro.cosmic_tools.utils;

import com.darkmattrmaestro.cosmic_tools.Constants;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntity;

public class Volume {
    int sizeX, sizeY, sizeZ;

    private final MainBlockDescriptor[][][] blocks;

    public Volume(int width, int height, int depth) {
        this.blocks = new MainBlockDescriptor[width][height][depth];
        this.sizeX = width;
        this.sizeY = height;
        this.sizeZ = depth;
    }

    public Volume(MainBlockDescriptor[][][] blocks) {
        this.blocks = blocks;
        this.sizeX = blocks.length;
        this.sizeY = blocks[0].length;
        this.sizeZ = blocks[0][0].length;
    }

    public static Volume of(int width, int height, int depth) {
        return new Volume(width, height, depth);
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public MainBlockDescriptor getBlock(int x, int y, int z) {
        if (x < 0 || x >= blocks.length || y < 0 || y >= blocks[0].length || z < 0 || z >= blocks[0][0].length) {
            Constants.LOGGER.error("Tried to access invalid RealVolume coordinates ({}, {}, {})", x, y, z);
            return null;
        }
        return blocks[x][y][z];
    }

    public void setBlock(int x, int y, int z, BlockState blockState, BlockEntity blockEntity) {
        this.setBlock(x, y, z, new MainBlockDescriptor(blockState, blockEntity));
    }

    public void setBlock(int x, int y, int z, MainBlockDescriptor block) {
        if (x < 0 || x >= blocks.length || y < 0 || y >= blocks[0].length || z < 0 || z >= blocks[0][0].length) {
            Constants.LOGGER.error("Tried to set invalid RealVolume coordinates ({}, {}, {})", x, y, z);
            return;
        }
        blocks[x][y][z] = block;
    }
}