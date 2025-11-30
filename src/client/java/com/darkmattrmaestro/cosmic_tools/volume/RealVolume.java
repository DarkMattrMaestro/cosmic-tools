package com.darkmattrmaestro.cosmic_tools.volume;

import finalforeach.cosmicreach.blocks.BlockState;
import com.darkmattrmaestro.cosmic_tools.Constants;

public class RealVolume implements IBlockVolume{
    private final BlockState[][][] blocks;

    public RealVolume(int width, int height, int depth) {
        blocks = new BlockState[width][height][depth];
    }

    public RealVolume(BlockState[][][] blocks) {
        this.blocks = blocks;
    }

    public static RealVolume of(int width, int height, int depth) {
        return new RealVolume(width, height, depth);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        if (x < 0 || x >= blocks.length || y < 0 || y >= blocks[0].length || z < 0 || z >= blocks[0][0].length) {
            Constants.LOGGER.warn("Tried to access invalid RealVolume coordinates ({}, {}, {})", x, y, z);
            return null;
        }
        return blocks[x][y][z];
    }

    public void setBlock(int x, int y, int z, BlockState block) {
        if (x < 0 || x >= blocks.length || y < 0 || y >= blocks[0].length || z < 0 || z >= blocks[0][0].length) {
            Constants.LOGGER.warn("Tried to set invalid RealVolume coordinates ({}, {}, {})", x, y, z);
            return;
        }
        blocks[x][y][z] = block;
    }
}
