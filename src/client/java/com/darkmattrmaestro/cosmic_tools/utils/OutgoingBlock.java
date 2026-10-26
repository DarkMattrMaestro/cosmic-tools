package com.darkmattrmaestro.cosmic_tools.utils;

import finalforeach.cosmicreach.blocks.BlockPosition;

public class OutgoingBlock {
    public BlockPosition blockPos;
    public int[] outgoingDir;
    public int distFromComponent;

    public OutgoingBlock(BlockPosition blockPos, int[] outgoingDir, int distFromComponent) {
        this.blockPos = blockPos;
        this.outgoingDir = outgoingDir;
        this.distFromComponent = distFromComponent;
    }

    @Override
    public String toString() {
        return "{" + blockPos.toString() + " d" + distFromComponent + "}";
    }
}
