package com.darkmattrmaestro.cosmic_tools.items;

//import  finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemSlot;
import com.darkmattrmaestro.cosmic_tools.actions.FillAction;
import com.darkmattrmaestro.cosmic_tools.utils.BlockAxis;
import com.darkmattrmaestro.cosmic_tools.utils.BlockSelectionUtil;

//import com.github.puzzle.game.util.BlockSelectionUtil;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import io.github.puzzle.cosmic.util.APISide;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.utils.Selection;
import com.darkmattrmaestro.cosmic_tools.utils.Vector3Int;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;
import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.blockPosToString;
import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;

public class Spatula extends AbstractCosmicItem {
    public static BlockAxis blockAxis = null;
    public static Selection copySelection = null;

    public Spatula(){
        super(Identifier.of(Constants.MOD_ID, "spatula"));
        addTexture(ItemModelType.ITEM_MODEL_3D, Identifier.of(Constants.MOD_ID, "spatula.png"));
    }

    private static boolean assigningFirstPos = true;
    private static BlockPosition firstPos = null;
    private static BlockPosition secondPos = null;

    private static BlockPosition pasteStartPos = null;

    public static Selection getSelection(){
        int maxExpansion = 10;
        blockAxis = BlockSelectionUtil.getBlockSideLookingAtFar(20);
        if(blockAxis == null) return null;

        List<String> primaryBlockIDs = Arrays.asList(
                blockAxis.pos.getBlockState().getBlockId(),
                "base:air",
                null
        );

//        int minX = blockAxis.pos.getGlobalX();
//        int minY = blockAxis.pos.getGlobalY();
//        int minZ = blockAxis.pos.getGlobalZ();
//        int maxX = blockAxis.pos.getGlobalX();
//        int maxY = blockAxis.pos.getGlobalY();
//        int maxZ = blockAxis.pos.getGlobalZ();

        Vector3Int minPos = new Vector3Int(blockAxis.pos.getGlobalX(), blockAxis.pos.getGlobalY(), blockAxis.pos.getGlobalZ());
        Vector3Int maxPos = minPos.cpy();

        BlockPosition minBlockPos = blockAxis.pos.copy();
        BlockPosition maxBlockPos = blockAxis.pos.copy();

        //--------------------------------------//



        // Iterate expansion amount
        for (int expansion = 0; expansion < maxExpansion; expansion++) {
            // Iterate x, y, z, except the axis that is flat
            for (int i = 0; i < 3; i++) {
                if (blockAxis.axis.toList()[i] != 0) { continue; }


                // Iterate for min and max
                for (int diff : new int[]{-1, 1}) {
                    Vector3Int currentPos = diff < 0 ? minPos : maxPos;
                    boolean andMatch = true;
                    boolean orMatch = false;

                    // Iterate for each block in the line that is to be added
                    int lineLength = (i != 0 ? maxPos.x - minPos.x : 0) + (i != 1 ? maxPos.y - minPos.y : 0) + (i != 2 ? maxPos.z - minPos.z : 0);
                    for (int j = 0; j <= lineLength; j++) {
                        int[] initialPos = new int[]{}; // TODO: potentially remove
                        BlockPosition blockPos = BlockPosition.ofGlobal(
                                InGame.getLocalPlayer().getZone(),
                                i == 0 ? currentPos.x + diff : minPos.x + j,
                                i == 1 ? currentPos.y + diff : minPos.y + j,
                                i == 2 ? currentPos.z + diff : minPos.z + j
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
                            if (!( // Inverse check that the block is the same or free, and there is space in front to paste it
                                    ( // Block is the same or free
                                            blockPos.getBlockState() == null
                                                    || "base:air".equals(blockPos.getBlockState().getBlockId())
                                                    || primaryBlockIDs.contains(blockPos.getBlockState().getBlockId())
                                    )
                                            && ( // There is space in front to paste
                                            frontBlockPos.getBlockState() == null
                                                    || "base:air".equals(frontBlockPos.getBlockState().getBlockId())
                                    )
                            )) {
                                andMatch = false;
                            }

                            // Check that at least one of the same block is in the new row
                            if (primaryBlockIDs.getFirst().equals(blockPos.getBlockState().getBlockId())) {
                                orMatch = true;
                            }
                        }
                    }

                    if (andMatch && orMatch) {
                        int[] diffPos = new int[]{0, 0, 0};
                        diffPos[i] += diff;
                        currentPos.add(diffPos);
//                        if (diff < 0) {
//                            minPos.add(diffPos);
//                        } else if (diff > 0) {
//                            maxPos.add(diffPos);
//                        }
                    }
                }
            }
        }

        copySelection = Selection.of(
            BlockPosition.ofGlobal(
                InGame.getLocalPlayer().getZone(),
                minPos.x,
                minPos.y,
                minPos.z
            ),
            BlockPosition.ofGlobal(
                InGame.getLocalPlayer().getZone(),
                maxPos.x,
                maxPos.y,
                maxPos.z
            )
        );
        return copySelection;

        //--------------------------------------//

//        for (int i = 0; i < maxExpansion; i++) {
//            if (blockAxis.axis.x == 0) {
//                // Try increasing X axis
//                boolean matchesMin = true;
//                boolean fullMatchesMin = false;
//                boolean matchesMax = true;
//                boolean fullMatchesMax = false;
//                for (int j = 0; j <= maxY - minY + maxZ - minZ; j++) {
//                    int y = minY + j * (maxY - minY);
//                    int z = minZ + j * (maxZ - minZ);
//                    BlockPosition minPos = BlockPosition.ofGlobal(
//                            InGame.getLocalPlayer().getZone(),
//                            minX - 1,
//                            y,
//                            z
//                    );
//                    BlockPosition maxPos = BlockPosition.ofGlobal(
//                            InGame.getLocalPlayer().getZone(),
//                            maxX + 1,
//                            y,
//                            z
//                    );
//
//                    BlockPosition minFrontPos = minPos.getOffsetBlockPos((int)blockAxis.axis.x, (int)blockAxis.axis.y, (int)blockAxis.axis.z);
//                    BlockPosition maxFrontPos = maxPos.getOffsetBlockPos((int)blockAxis.axis.x, (int)blockAxis.axis.y, (int)blockAxis.axis.z);
//
//                    if (minPos.chunk() != null) {
//                        if ( // Check that the block is the same, and there is space to paste it
//                            !primaryBlockIDs.contains(minPos.getBlockState().getBlockId())
//                            || !(
//                                minFrontPos.getBlockState() != null
//                                || "base:air".equals(minFrontPos.getBlockState().getBlockId())
//                            )
//                        ) {
//                            matchesMin = false;
//                        }
//                        if (primaryBlockIDs.getFirst().equals(minPos.getBlockState().getBlockId())) {
//                            fullMatchesMin = true;
//                        }
//                    }
//                    if (maxPos.chunk() != null) {
//                        if (
//                            !primaryBlockIDs.contains(maxPos.getBlockState().getBlockId())
//                            || !(
//                            maxFrontPos.getBlockState() != null
//                                || "base:air".equals(maxFrontPos.getBlockState().getBlockId())
//                            )
//                        ) {
//                            matchesMax = false;
//                        }
//                        if (primaryBlockIDs.getFirst().equals(maxPos.getBlockState().getBlockId())) {
//                            fullMatchesMax = true;
//                        }
//                    }
//                }
//                if (matchesMin && fullMatchesMin) { minX -= 1; }
//                if (matchesMax && fullMatchesMax) { maxX += 1; }
//            }
//
//            if (blockAxis.axis.y == 0) {
//                // Try increasing X axis
//                boolean matchesMin = true;
//                boolean fullMatchesMin = false;
//                boolean matchesMax = true;
//                boolean fullMatchesMax = false;
//                for (int j = 0; j <= maxX - minX + maxZ - minZ; j++) {
//                    int x = minX + j * (maxX - minX);
//                    int z = minZ + j * (maxZ - minZ);
//                    BlockPosition minPos = BlockPosition.ofGlobal(
//                            InGame.getLocalPlayer().getZone(),
//                            x,
//                            minY - 1,
//                            z
//                    );
//                    BlockPosition maxPos = BlockPosition.ofGlobal(
//                            InGame.getLocalPlayer().getZone(),
//                            x,
//                            maxY + 1,
//                            z
//                    );
//
//                    if (minPos.chunk() != null) {
//                        if (!primaryBlockIDs.contains(minPos.getBlockState().getBlockId())) {
//                            matchesMin = false;
//                        }
//                        if (primaryBlockIDs.getFirst().equals(minPos.getBlockState().getBlockId())) {
//                            fullMatchesMin = true;
//                        }
//                    }
//                    if (maxPos.chunk() != null) {
//                        if (!primaryBlockIDs.contains(maxPos.getBlockState().getBlockId())) {
//                            matchesMax = false;
//                        }
//                        if (primaryBlockIDs.getFirst().equals(maxPos.getBlockState().getBlockId())) {
//                            fullMatchesMax = true;
//                        }
//                    }
//                }
//                if (matchesMin && fullMatchesMin) { minY -= 1; }
//                if (matchesMax && fullMatchesMax) { maxY += 1; }
//            }
//
//            if (blockAxis.axis.z == 0) {
//                // Try increasing X axis
//                boolean matchesMin = true;
//                boolean fullMatchesMin = false;
//                boolean matchesMax = true;
//                boolean fullMatchesMax = false;
//                for (int j = 0; j <= maxX - minX + maxY - minY; j++) {
//                    int x = minX + j * (maxX - minX);
//                    int y = minY + j * (maxY - minY);
//                    BlockPosition minPos = BlockPosition.ofGlobal(
//                            InGame.getLocalPlayer().getZone(),
//                            x,
//                            y,
//                            minZ - 1
//                    );
//                    BlockPosition maxPos = BlockPosition.ofGlobal(
//                            InGame.getLocalPlayer().getZone(),
//                            x,
//                            y,
//                            maxZ + 1
//                    );
//
//                    if (minPos.chunk() != null) {
//                        if (!primaryBlockIDs.contains(minPos.getBlockState().getBlockId())) {
//                            matchesMin = false;
//                        }
//                        if (primaryBlockIDs.getFirst().equals(minPos.getBlockState().getBlockId())) {
//                            fullMatchesMin = true;
//                        }
//                    }
//                    if (maxPos.chunk() != null) {
//                        if (!primaryBlockIDs.contains(maxPos.getBlockState().getBlockId())) {
//                            matchesMax = false;
//                        }
//                        if (primaryBlockIDs.getFirst().equals(maxPos.getBlockState().getBlockId())) {
//                            fullMatchesMax = true;
//                        }
//                    }
//                }
//                if (matchesMin && fullMatchesMin) { minZ -= 1; }
//                if (matchesMax && fullMatchesMax) { maxZ += 1; }
//            }
//        }
//
//        copySelection = Selection.of(minX, minY, minZ, maxX, maxY, maxZ);
//        return copySelection;
    }

    public static Selection getPasteSelection(boolean update){
        if (update) { getSelection(); }
        if(blockAxis == null || copySelection == null) return null;
        return Selection.of(
            (int) (copySelection.minX + blockAxis.axis.x),
            (int) (copySelection.minY + blockAxis.axis.y),
            (int) (copySelection.minZ + blockAxis.axis.z),
            (int) (copySelection.maxX + blockAxis.axis.x),
            (int) (copySelection.maxY + blockAxis.axis.y),
            (int) (copySelection.maxZ + blockAxis.axis.z)
        );
    }

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

    private int nBlocks() {
        if (firstPos == null || secondPos == null) { return 0; }
        return abs(firstPos.getGlobalX() - secondPos.getGlobalX() * (firstPos.getGlobalX() - secondPos.getGlobalX()) * (firstPos.getGlobalX() - secondPos.getGlobalX()));
    }

    @Override
    public boolean use(APISide side, ItemSlot itemSlot, Player player, BlockPosition _targetPlaceBlockPos, BlockPosition targetBreakBlockPos, boolean isLeftClick) {
        if(side == APISide.SERVER) return false;
        BlockPosition pos = BlockSelectionUtil.getBlockPositionLookingAt();
        if(pos == null) {
            return false;
        }

        if(isLeftClick) {
            // Left Click => Reassign positions
            if (itemSlot.getSlotId() == 1) {
                if (assigningFirstPos) {
                    sendMsg("First position set to " + blockPosToString(pos));
                    firstPos = pos.copy();
                } else {
                    sendMsg("Second position set to " + blockPosToString(pos));
                    secondPos = pos.copy();
                }
                assigningFirstPos = !assigningFirstPos;
            } else if (itemSlot.getSlotId() == 2) {
                sendMsg("Paste start position set to " + blockPosToString(pos));
                pasteStartPos = pos.copy();
            } else {
                sendMsg("Incorrect slot");
            }
        } else {
            // Right Click =>  Copy (slot 1) or paste (slot 2)
            if (itemSlot.getSlotId() == 2) {
                if (firstPos != null && secondPos != null && pasteStartPos != null) {
                    sendMsg("Pasting " + nBlocks() + " blocks");
                    FillAction.of(Spatula.getSelection(), pasteStartPos, BlockState.getInstance("base:wood_planks", MissingBlockStateResult.MISSING_OBJECT)).paste(InGame.getLocalPlayer().getZone());
                } else {
                    sendMsg("Selection areas not selected");
                }
            } else {
                sendMsg("Incorrect slot");
            }
        }
        return true;
    }
}
