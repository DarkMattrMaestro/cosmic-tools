package com.darkmattrmaestro.cosmic_tools.items;

import com.darkmattrmaestro.cosmic_tools.utils.BlockAxis;
import com.darkmattrmaestro.cosmic_tools.utils.BlockMappings;
import com.darkmattrmaestro.cosmic_tools.utils.Hallucination;
import com.darkmattrmaestro.cosmic_tools.utils.Vector3Int;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.abs;

public class FunctionalSpatula {
    float reachDist = 12.0f;
    int maxExpansion = 10;
    public BlockAxis blockAxis = null;
    public Hallucination copyBlocks = new Hallucination();
    public static String initialBlockID = "";

    public FunctionalSpatula(String itemID) {
        super();

        switch (itemID) {
            case "spatula": {
                this.reachDist = 20.0f;
                this.maxExpansion = 5;
            }
        }
    }

    /**
     * Check that the player has enough of the correct item to paste all the hallucinated blocks.
     *
     * @return <code>true</code> if the player has enough blocks, else <code>false</code>
     */
    public boolean playerHasEnoughItems(Player player) {
        AtomicInteger availableItems = new AtomicInteger();
        player.inventory.forEachSlot(((ItemSlot itemSlot) -> {
            if (itemSlot.getItem() == null) { return; }

            if (itemSlot.getItem().getID().equals(initialBlockID)) {
                availableItems.addAndGet(itemSlot.getItemAmount());
            }
        }));

        return this.copyBlocks.blocks.size() <= availableItems.get() || player.gamemode.hasInfiniteItems();
    }

    /**
     * <p>
     * Return the hallucination formed from the largest semi-contiguous set of blocks that are similar to the player's
     * actively selected block.
     * </p>
     * <p>
     * The plane along which to expand is determined according to the block face with which the ray from the player
     * collided. The targeted block is set as the minimum and maximum block, then the minimum and maximum are
     * iteratively decreased and increased, respectively. Each block in the row that might be added (plus one block on
     * either end to allow diagonal expansion) is checked for valid placement conditions and, if at least one block is
     * valid, the respective extremum is expanded.
     * </p>
     *
     * @return the hallucination of the blocks to be copied
     */
    public Hallucination getHallucination(Player player, BlockAxis blockAxis){
        this.blockAxis = blockAxis;

        initialBlockID = blockAxis.pos.getBlockState().getBlockId();

        copyBlocks = new Hallucination();

        Vector3Int minPos = new Vector3Int(blockAxis.pos.getGlobalX(), blockAxis.pos.getGlobalY(), blockAxis.pos.getGlobalZ());
        Vector3Int maxPos = minPos.cpy();

        // Add the initial block if it is not obstructed
        BlockPosition initialFrontBlockPos = BlockPosition.ofGlobal(
                player.getZone(),
                blockAxis.pos.getGlobalX() + blockAxis.axis.x,
                blockAxis.pos.getGlobalY() + blockAxis.axis.y,
                blockAxis.pos.getGlobalZ() + blockAxis.axis.z
        );
        if (blockAxis.pos.chunk() != null && initialFrontBlockPos.chunk() != null) {
            // Check that at least one of the same block is in the new row
            if (initialFrontBlockPos.getBlockState() == null || "base:air".equals(initialFrontBlockPos.getBlockState().getBlockId())) {
                copyBlocks.blocks.add(blockAxis.pos);
            }
        }
        if (copyBlocks.blocks.isEmpty()) {
            return copyBlocks;
        }

//        // Flood Fill
//        Stack<BlockPosition> stack = new Stack<BlockPosition>();
//        stack.push(blockAxis.pos);

        // Iterate expansion amount
        boolean hasExpanded = true;
        while (hasExpanded) {
            hasExpanded = false;
            // Iterate x, y, z, except the axis that is flat
            for (int i = 0; i < 3; i++) {
                if (blockAxis.axis.toList()[i] != 0) { continue; }

                // Iterate for min and max
                for (int diff : new int[]{-1, 1}) {
                    Vector3Int currentPos = diff < 0 ? minPos : maxPos;
                    boolean match = false;

                    // Iterate for each block in the line that is to be added
                    int lineLength = (i != 0 ? maxPos.x - minPos.x : 0) + (i != 1 ? maxPos.y - minPos.y : 0) + (i != 2 ? maxPos.z - minPos.z : 0);
                    for (int j = -1; j <= lineLength + 1; j++) {
                        BlockPosition blockPos = BlockPosition.ofGlobal(
                                player.getZone(),
                                i == 0 ? currentPos.x + diff : (blockAxis.axis.toList()[0] == 0 ? minPos.x + j : currentPos.x),
                                i == 1 ? currentPos.y + diff : (blockAxis.axis.toList()[1] == 0 ? minPos.y + j : currentPos.y),
                                i == 2 ? currentPos.z + diff : (blockAxis.axis.toList()[2] == 0 ? minPos.z + j : currentPos.z)
                        );
                        if (blockPos.chunk() == null) { continue; }

                        BlockPosition frontBlockPos = BlockPosition.ofGlobal(
                                player.getZone(),
                                blockPos.getGlobalX() + blockAxis.axis.x,
                                blockPos.getGlobalY() + blockAxis.axis.y,
                                blockPos.getGlobalZ() + blockAxis.axis.z
                        );
                        if (frontBlockPos.chunk() == null) { continue; }

                        if (blockPos.chunk() != null && frontBlockPos.chunk() != null) {
                            // Check that at least one of the same block is in the new row
                            if (
                                    ( // Block is the same
                                            initialBlockID.equals(blockPos.getBlockState().getBlockId())
                                    )
                                            && ( // There is space in front to paste
                                            frontBlockPos.getBlockState() == null
                                                    || "base:air".equals(frontBlockPos.getBlockState().getBlockId())
                                    )
                                            && ( // The block is within range
                                            abs(blockAxis.pos.getGlobalX() - blockPos.getGlobalX()) <= this.maxExpansion
                                                    && abs(blockAxis.pos.getGlobalY() - blockPos.getGlobalY()) <= this.maxExpansion
                                                    && abs(blockAxis.pos.getGlobalZ() - blockPos.getGlobalZ()) <= this.maxExpansion
                                    )
                            ) {
                                copyBlocks.blocks.add(blockPos);
                                hasExpanded = true;
                                match = true;
                            }
                        }
                    }

                    if (match) {
                        int[] diffPos = new int[]{0, 0, 0};
                        diffPos[i] += diff;
                        currentPos.add(diffPos);
                    }
                }
            }
        }

        return copyBlocks;
    }
}
