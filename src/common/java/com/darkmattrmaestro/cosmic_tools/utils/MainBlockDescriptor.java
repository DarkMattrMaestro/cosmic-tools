package com.darkmattrmaestro.cosmic_tools.utils;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntity;

public class MainBlockDescriptor {
    public BlockState blockState;
    public BlockEntity blockEntity;

    @Deprecated
    public MainBlockDescriptor() {
        this(null, null);
    }

    public MainBlockDescriptor(BlockState blockState, BlockEntity blockEntity) {
        this.blockState = blockState;
        this.blockEntity = blockEntity;
    }
}
