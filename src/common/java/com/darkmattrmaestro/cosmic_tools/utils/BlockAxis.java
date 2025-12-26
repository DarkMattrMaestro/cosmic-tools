package com.darkmattrmaestro.cosmic_tools.utils;

import finalforeach.cosmicreach.blocks.BlockPosition;

/**
 * The <code>BlockAxis</code> class represents a block position and the axis of the face that is collided.
 */
public class BlockAxis {
    public BlockPosition pos;
    public Vector3Int axis;

    public BlockAxis() { }

    public BlockAxis(BlockPosition pos, Vector3Int axis) {
        this.pos = pos;
        this.axis = axis;
    }

    @Override
    public String toString() {
        return pos + " axis(" + axis + ")";
    }
}
