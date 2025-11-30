package com.darkmattrmaestro.cosmic_tools.items;

//import  finalforeach.cosmicreach.BlockSelection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.darkmattrmaestro.cosmic_tools.hallucination.Hallucination;
import com.darkmattrmaestro.cosmic_tools.utils.*;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemSlot;
import com.darkmattrmaestro.cosmic_tools.actions.FillAction;

//import com.github.puzzle.game.util.BlockSelectionUtil;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.networking.packets.blocks.PlaceBlockPacket;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import io.github.puzzle.cosmic.util.APISide;
import com.darkmattrmaestro.cosmic_tools.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;
import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.blockPosToString;
import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;

public class Spatula extends AbstractCosmicItem {
    public static BlockAxis blockAxis = null;
//    public static Selection copySelection = null;
    public static Hallucination copyBlocks = new Hallucination();

    public Spatula(){
        super(Identifier.of(Constants.MOD_ID, "spatula"));
        addTexture(ItemModelType.ITEM_MODEL_3D, Identifier.of(Constants.MOD_ID, "spatula.png"));
    }

//    private static boolean assigningFirstPos = true;
//    private static BlockPosition firstPos = null;
//    private static BlockPosition secondPos = null;

//    private static BlockPosition pasteStartPos = null;

    public static Hallucination getSelection(){
        int maxExpansion = 10;
        blockAxis = BlockSelectionUtil.getBlockSideLookingAtFar(20);
        if(blockAxis == null) return null;

        List<String> primaryBlockIDs = Arrays.asList(
                blockAxis.pos.getBlockState().getBlockId(),
                "base:air",
                null
        );

        copyBlocks = new Hallucination();

        Vector3Int minPos = new Vector3Int(blockAxis.pos.getGlobalX(), blockAxis.pos.getGlobalY(), blockAxis.pos.getGlobalZ());
        Vector3Int maxPos = minPos.cpy();

        //--------------------------------------//

        // Add the initial block if it is not obstructed
        BlockPosition initialFrontBlockPos = BlockPosition.ofGlobal(
                InGame.getLocalPlayer().getZone(),
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

//        Constants.LOGGER.warn("BlockAxis: {}", blockAxis.axis);
        // Iterate expansion amount
        for (int expansion = 0; expansion < maxExpansion; expansion++) {
//            Constants.LOGGER.warn("Expanded: {}", expansion);
            // Iterate x, y, z, except the axis that is flat
            for (int i = 0; i < 3; i++) {
                if (blockAxis.axis.toList()[i] != 0) { continue; }
//                Constants.LOGGER.warn("   - Axis: {}", i);

                // Iterate for min and max
                for (int diff : new int[]{-1, 1}) {
//                    Constants.LOGGER.warn("      - Diff: {}", diff);
                    Vector3Int currentPos = diff < 0 ? minPos : maxPos;
                    boolean match = false;

                    // Iterate for each block in the line that is to be added
                    int lineLength = (i != 0 ? maxPos.x - minPos.x : 0) + (i != 1 ? maxPos.y - minPos.y : 0) + (i != 2 ? maxPos.z - minPos.z : 0);
                    for (int j = -1; j <= lineLength + 1; j++) {
                        BlockPosition blockPos = BlockPosition.ofGlobal(
                                InGame.getLocalPlayer().getZone(),
                                i == 0 ? currentPos.x + diff : (blockAxis.axis.toList()[0] == 0 ? minPos.x + j : currentPos.x),
                                i == 1 ? currentPos.y + diff : (blockAxis.axis.toList()[1] == 0 ? minPos.y + j : currentPos.y),
                                i == 2 ? currentPos.z + diff : (blockAxis.axis.toList()[2] == 0 ? minPos.z + j : currentPos.z)
                        );
                        if (blockPos.chunk() == null) { continue; }

                        BlockPosition frontBlockPos = BlockPosition.ofGlobal(
                                InGame.getLocalPlayer().getZone(),
                                blockPos.getGlobalX() + blockAxis.axis.x,
                                blockPos.getGlobalY() + blockAxis.axis.y,
                                blockPos.getGlobalZ() + blockAxis.axis.z
                        );
                        if (frontBlockPos.chunk() == null) { continue; }

                        if (blockPos.chunk() != null && frontBlockPos.chunk() != null) {
//                            Constants.LOGGER.warn("      - BlockPos {} - {} {}", blockPos, blockAxis.pos.getBlockState().getBlockId().equals(blockPos.getBlockState().getBlockId()), blockPos.getBlockState().getBlockId());
                            // Check that at least one of the same block is in the new row
                            if (
                                    ( // Block is the same
                                            blockAxis.pos.getBlockState().getBlockId().equals(blockPos.getBlockState().getBlockId())
                                    )
                                    && ( // There is space in front to paste
                                            frontBlockPos.getBlockState() == null
                                            || "base:air".equals(frontBlockPos.getBlockState().getBlockId())
                                    )
                            ) {
                                copyBlocks.blocks.add(blockPos);
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

//        copySelection = Selection.of(
//            BlockPosition.ofGlobal(
//                InGame.getLocalPlayer().getZone(),
//                minPos.x,
//                minPos.y,
//                minPos.z
//            ),
//            BlockPosition.ofGlobal(
//                InGame.getLocalPlayer().getZone(),
//                maxPos.x,
//                maxPos.y,
//                maxPos.z
//            )
//        );
        return copyBlocks;
    }

//    public static Selection getPasteSelection(boolean update){
//        if (update) { getSelection(); }
//        if(blockAxis == null || copySelection == null) return null;
//        return Selection.of(
//            copySelection.minX + blockAxis.axis.x,
//            copySelection.minY + blockAxis.axis.y,
//            copySelection.minZ + blockAxis.axis.z,
//            copySelection.maxX + blockAxis.axis.x,
//            copySelection.maxY + blockAxis.axis.y,
//            copySelection.maxZ + blockAxis.axis.z
//        );
//    }

//    public static ArrayList<BlockPosition> getPasteSelection(boolean update){
//        if (update) { getSelection(); }
//
//        if(blockAxis == null || copySelection == null) return null;
//        return Selection.of(
//                copySelection.minX + blockAxis.axis.x,
//                copySelection.minY + blockAxis.axis.y,
//                copySelection.minZ + blockAxis.axis.z,
//                copySelection.maxX + blockAxis.axis.x,
//                copySelection.maxY + blockAxis.axis.y,
//                copySelection.maxZ + blockAxis.axis.z
//        );
//    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean isTool() {
        return true;
    }

    @Override
    public boolean canTargetBlockForBreaking(BlockState blockState) {
        return false;
    }

    @Override
    public boolean canMergeWith(Item item) {
        return false;
    }

    @Override
    public float getEffectiveBreakingSpeed(ItemStack stack) {
        return 0.0f;
    }

    @Override
    public boolean isEffectiveBreaking(ItemStack itemStack, BlockState blockState) {
        return true;
    }

    @Override
    public String getName() {
        return "Spatula";
    }

    @Override
    public boolean use(APISide side, ItemSlot itemSlot, Player player, BlockPosition _targetPlaceBlockPos, BlockPosition targetBreakBlockPos, boolean isLeftClick) {
        if(side == APISide.SERVER) return false;
        BlockPosition pos = BlockSelectionUtil.getBlockPositionLookingAt();
        if(pos == null) {
            return false;
        }

        if(isLeftClick) {
            // Left Click
        } else {
            // Right Click => paste
            List<Chunk> chunksToUpdate = new ArrayList<>();

            Zone zone = InGame.getLocalPlayer().getZone();
            for (BlockPosition blockPos: copyBlocks.blocks) {
                Vector3Int hallucinatedPos = (new Vector3Int(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ())).add(Spatula.blockAxis.axis);
                zone.setBlockState(blockPos.getBlockState(), hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                Chunk c = zone.getChunkAtBlock(hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                if (!chunksToUpdate.contains(c)) chunksToUpdate.add(c);
                if (ClientNetworkManager.isConnected()) {
                    BlockPosition frontBlockPos = BlockPosition.ofGlobal(zone, hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                    ClientNetworkManager.sendAsClient(new PlaceBlockPacket(frontBlockPos, blockPos.getBlockState(), -1));
                }
            }

            ChunkUtils.remesh(chunksToUpdate, zone);
        }
        return true;
    }
}
