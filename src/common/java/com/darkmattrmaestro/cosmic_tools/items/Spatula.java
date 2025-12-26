package com.darkmattrmaestro.cosmic_tools.items;

import com.badlogic.gdx.Input;
import com.darkmattrmaestro.cosmic_tools.utils.Hallucination;
import com.darkmattrmaestro.cosmic_tools.utils.*;
import finalforeach.cosmicreach.audio.SoundManager;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.gamestates.InGame;

import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.networking.packets.blocks.PlaceBlockPacket;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import com.darkmattrmaestro.cosmic_tools.Constants;
import io.github.puzzle.cosmic.util.APISide;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 * <p>
 * A spatula (or a wand) item that copies semi-contiguous patterns of similar blocks to the block in front of the
 * selected face.
 * </p>
 * <p>
 * Spatulas tend to be used for spreading things, and I imagine this tool is spreading blocks on the previous layer of
 * blocks. This is the reason for the name that, in retrospect, is slightly odd.
 * </p>
 */
public class Spatula extends AbstractCosmicItem {
    public static float reachDist = 20.0f;
//    public BlockAxis blockAxis = null;
    public Hallucination copyBlocks = new Hallucination();
    public static String initialBlockID = "";

    public Spatula(){
        super(Identifier.of(Constants.MOD_ID, "spatula"));
        addTexture(ItemModelType.ITEM_MODEL_3D, Identifier.of(Constants.MOD_ID, "spatula.png"));
    }

    @Override
    public boolean isTool() {
        return true;
    }

    @Override
    public boolean canMergeWith(Item item) {
        return false;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public String getName() {
        return "Spatula";
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
    public Hallucination getHallucination(Player player){
        int maxExpansion = 10;
        BlockAxis blockAxis = BlockSelectionUtil.getBlockSideLookingAtFar(Spatula.reachDist);
        if(blockAxis == null) return null;

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
                                            abs(blockAxis.pos.getGlobalX() - blockPos.getGlobalX()) <= maxExpansion
                                                    && abs(blockAxis.pos.getGlobalY() - blockPos.getGlobalY()) <= maxExpansion
                                                    && abs(blockAxis.pos.getGlobalZ() - blockPos.getGlobalZ()) <= maxExpansion
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

    @Override
    public boolean use(APISide side, ItemSlot itemSlot, Player player, BlockPosition targetPlaceBlockPos, BlockPosition targetBreakBlockPos, boolean isLeftClick) {
        super.use(side, itemSlot, player, targetPlaceBlockPos, targetBreakBlockPos, isLeftClick);

        if (!isLeftClick) {
            // Right Click => paste
            if (this.playerHasEnoughItems(player)) {
                if (!InGame.getLocalPlayer().gamemode.hasInfiniteItems()) {
                    AtomicInteger consumedItems = new AtomicInteger(copyBlocks.blocks.size());
                    InGame.getLocalPlayer().inventory.forEachSlot((itemSlot -> {
                        if (itemSlot.getItem() == null) {
                            return;
                        }

                        if (itemSlot.getItem().getID().equals(initialBlockID)) {
                            int minConsumed = min(consumedItems.get(), itemSlot.getItemAmount());
                            itemSlot.addAmount(-minConsumed);
                            consumedItems.addAndGet(-minConsumed);
                        }
                    }));
                }

                List<Chunk> chunksToUpdate = new ArrayList<>();
                String triggerName = "onPlace";

                Zone zone = InGame.getLocalPlayer().getZone();
                for (BlockPosition blockPos : copyBlocks.blocks) {
                    BlockEventTrigger[] triggers = blockPos.getBlockState().getTrigger(triggerName);

                    Vector3Int hallucinatedPos = (new Vector3Int(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ())).add(Spatula.blockAxis.axis);

                    zone.setBlockState(blockPos.getBlockState(), hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                    Chunk c = zone.getChunkAtBlock(hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                    if (!chunksToUpdate.contains(c)) {
                        chunksToUpdate.add(c);
                    }

                    if (ClientNetworkManager.isConnected()) {
                        BlockPosition frontBlockPos = BlockPosition.ofGlobal(zone, hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                        ClientNetworkManager.sendAsClient(new PlaceBlockPacket(frontBlockPos, blockPos.getBlockState(), -1));
                    }

//                    if (triggers != null) {
//                        if (GameSingletons.isHost) {
//                            BlockEventArgs args = new BlockEventArgs();
//                            args.srcPlayer = InGame.getLocalPlayer();
//                            args.srcBlockState = blockPos.getBlockState();
//                            args.zone = zone;
//                            args.blockPos = blockPos;
//    //                        for (GameEventTrigger trigger: triggers) {
//    //                            Constants.LOGGER.warn("Triggers: {}", trigger.getAction());
//    //                        }
//                            args.run(triggers);
//                            args.runScheduledTriggers();
//                        }
//                    }
                }

                SoundManager.INSTANCE.playSound("cosmic_tools:sounds/items/spatula-place.ogg", 1, 1, 0);


                ChunkUtils.remesh(chunksToUpdate, zone);
            }
        }

        return false;
    }

    @Unique
    public static boolean onMousePressed(int button) {
        if(!BlockSelectionUtil.doesCollideFar(reachDist)) { return false; }

        if(button == Input.Buttons.RIGHT) {
            // Right Click => paste
            if (playerHasEnoughItems()) {
                if (!InGame.getLocalPlayer().gamemode.hasInfiniteItems()) {
                    AtomicInteger consumedItems = new AtomicInteger(copyBlocks.blocks.size());
                    InGame.getLocalPlayer().inventory.forEachSlot((itemSlot -> {
                        if (itemSlot.getItem() == null) {
                            return;
                        }

                        if (itemSlot.getItem().getID().equals(initialBlockID)) {
                            int minConsumed = min(consumedItems.get(), itemSlot.getItemAmount());
                            itemSlot.addAmount(-minConsumed);
                            consumedItems.addAndGet(-minConsumed);
                        }
                    }));
                }

                List<Chunk> chunksToUpdate = new ArrayList<>();
                String triggerName = "onPlace";

                Zone zone = InGame.getLocalPlayer().getZone();
                for (BlockPosition blockPos : copyBlocks.blocks) {
                    BlockEventTrigger[] triggers = blockPos.getBlockState().getTrigger(triggerName);

                    Vector3Int hallucinatedPos = (new Vector3Int(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ())).add(Spatula.blockAxis.axis);

                    zone.setBlockState(blockPos.getBlockState(), hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                    Chunk c = zone.getChunkAtBlock(hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                    if (!chunksToUpdate.contains(c)) {
                        chunksToUpdate.add(c);
                    }

                    if (ClientNetworkManager.isConnected()) {
                        BlockPosition frontBlockPos = BlockPosition.ofGlobal(zone, hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                        ClientNetworkManager.sendAsClient(new PlaceBlockPacket(frontBlockPos, blockPos.getBlockState(), -1));
                    }

//                    if (triggers != null) {
//                        if (GameSingletons.isHost) {
//                            BlockEventArgs args = new BlockEventArgs();
//                            args.srcPlayer = InGame.getLocalPlayer();
//                            args.srcBlockState = blockPos.getBlockState();
//                            args.zone = zone;
//                            args.blockPos = blockPos;
//    //                        for (GameEventTrigger trigger: triggers) {
//    //                            Constants.LOGGER.warn("Triggers: {}", trigger.getAction());
//    //                        }
//                            args.run(triggers);
//                            args.runScheduledTriggers();
//                        }
//                    }
                }

                SoundManager.INSTANCE.playSound("cosmic_tools:sounds/items/spatula-place.ogg", 1, 1, 0);


                ChunkUtils.remesh(chunksToUpdate, zone);
            }
        }
        return true;
    }
}
