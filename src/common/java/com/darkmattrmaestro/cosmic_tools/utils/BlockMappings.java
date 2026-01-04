package com.darkmattrmaestro.cosmic_tools.utils;

import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class BlockMappings {
    private HashMap<BlockPosition, BlockState> blockMappings;

    public BlockMappings() {
        this.blockMappings = new HashMap<BlockPosition, BlockState>();
    }

    public BlockMappings(HashMap<BlockPosition, BlockState> blockMappings) {
        this.blockMappings = blockMappings;
    }

    public static BlockMappings ofZonePositions(Zone zone, List<BlockPosition> blockPositions) {
        HashMap<BlockPosition, BlockState> blockMappings = new HashMap<BlockPosition, BlockState>();

        for (BlockPosition blockPosition: blockPositions) {
            BlockState blockState = zone.getBlockState(blockPosition.getGlobalX(), blockPosition.getGlobalY(), blockPosition.getGlobalZ());
            blockMappings.put(blockPosition, blockState);
        }

        return new BlockMappings(blockMappings);
    }

    public BlockState put(BlockPosition K, BlockState V) {
        return this.blockMappings.put(K, V);
    }

    public int size() {
        return this.blockMappings.size();
    }

    public Set<BlockPosition> blockPositionSet() {
        return this.blockMappings.keySet();
    }

    public Collection<BlockState> blockStates() {
        return this.blockMappings.values();
    }

    public BlockState getBlockState(BlockPosition blockPosition) {
        return this.blockMappings.get(blockPosition);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        this.blockMappings.forEach((BlockPosition blockPosition, BlockState blockState) -> {
            str.append("(").append(blockPosition).append(",").append(blockState).append(")");
        });

        return str.toString();
    }

    public void forEach(BiConsumer<BlockPosition, BlockState> action) {
        this.blockMappings.forEach(action);
    }
}
