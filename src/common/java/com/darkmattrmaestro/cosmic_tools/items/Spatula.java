package com.darkmattrmaestro.cosmic_tools.items;

import com.darkmattrmaestro.cosmic_tools.utils.*;

import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import com.darkmattrmaestro.cosmic_tools.Constants;

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

//    @Override
//    public boolean use(APISide side, ItemSlot itemSlot, Player player, BlockPosition targetPlaceBlockPos, BlockPosition targetBreakBlockPos, boolean isLeftClick) {
//        super.use(side, itemSlot, player, targetPlaceBlockPos, targetBreakBlockPos, isLeftClick);
//
//        if (!isLeftClick) {
//            // Right Click => paste
//            if (this.playerHasEnoughItems(player)) {
//                if (!player.gamemode.hasInfiniteItems()) {
//                    AtomicInteger consumedItems = new AtomicInteger(copyBlocks.blocks.size());
//                    player.inventory.forEachSlot((itemSlot -> {
//                        if (itemSlot.getItem() == null) {
//                            return;
//                        }
//
//                        if (itemSlot.getItem().getID().equals(initialBlockID)) {
//                            int minConsumed = min(consumedItems.get(), itemSlot.getItemAmount());
//                            itemSlot.addAmount(-minConsumed);
//                            consumedItems.addAndGet(-minConsumed);
//                        }
//                    }));
//                }
//
//                List<Chunk> chunksToUpdate = new ArrayList<>();
//                String triggerName = "onPlace";
//
//                Zone zone = player.getZone();
//                for (BlockPosition blockPos : copyBlocks.blocks) {
//                    BlockEventTrigger[] triggers = blockPos.getBlockState().getTrigger(triggerName);
//
//                    Vector3Int hallucinatedPos = (new Vector3Int(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ())).add(Spatula.blockAxis.axis);
//
//                    zone.setBlockState(blockPos.getBlockState(), hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
//                    Chunk c = zone.getChunkAtBlock(hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
//                    if (!chunksToUpdate.contains(c)) {
//                        chunksToUpdate.add(c);
//                    }
//
//                    if (ClientNetworkManager.isConnected()) {
//                        BlockPosition frontBlockPos = BlockPosition.ofGlobal(zone, hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
//                        ClientNetworkManager.sendAsClient(new PlaceBlockPacket(frontBlockPos, blockPos.getBlockState(), -1));
//                    }
//
////                    if (triggers != null) {
////                        if (GameSingletons.isHost) {
////                            BlockEventArgs args = new BlockEventArgs();
////                            args.srcPlayer = InGame.getLocalPlayer();
////                            args.srcBlockState = blockPos.getBlockState();
////                            args.zone = zone;
////                            args.blockPos = blockPos;
////    //                        for (GameEventTrigger trigger: triggers) {
////    //                            Constants.LOGGER.warn("Triggers: {}", trigger.getAction());
////    //                        }
////                            args.run(triggers);
////                            args.runScheduledTriggers();
////                        }
////                    }
//                }
//
//                SoundManager.INSTANCE.playSound("cosmic_tools:sounds/items/spatula-place.ogg", 1, 1, 0);
//
//
//                ChunkUtils.remesh(chunksToUpdate, zone);
//            }
//        }
//
//        return false;
//    }

//    @Unique
//    public static boolean onMousePressed(int button) {
//        if(!BlockSelectionUtil.doesCollideFar(reachDist)) { return false; }
//
//        if(button == Input.Buttons.RIGHT) {
//            // Right Click => paste
//            if (playerHasEnoughItems()) {
//                if (!InGame.getLocalPlayer().gamemode.hasInfiniteItems()) {
//                    AtomicInteger consumedItems = new AtomicInteger(copyBlocks.blocks.size());
//                    InGame.getLocalPlayer().inventory.forEachSlot((itemSlot -> {
//                        if (itemSlot.getItem() == null) {
//                            return;
//                        }
//
//                        if (itemSlot.getItem().getID().equals(initialBlockID)) {
//                            int minConsumed = min(consumedItems.get(), itemSlot.getItemAmount());
//                            itemSlot.addAmount(-minConsumed);
//                            consumedItems.addAndGet(-minConsumed);
//                        }
//                    }));
//                }
//
//                List<Chunk> chunksToUpdate = new ArrayList<>();
//                String triggerName = "onPlace";
//
//                Zone zone = InGame.getLocalPlayer().getZone();
//                for (BlockPosition blockPos : copyBlocks.blocks) {
//                    BlockEventTrigger[] triggers = blockPos.getBlockState().getTrigger(triggerName);
//
//                    Vector3Int hallucinatedPos = (new Vector3Int(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ())).add(Spatula.blockAxis.axis);
//
//                    zone.setBlockState(blockPos.getBlockState(), hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
//                    Chunk c = zone.getChunkAtBlock(hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
//                    if (!chunksToUpdate.contains(c)) {
//                        chunksToUpdate.add(c);
//                    }
//
//                    if (ClientNetworkManager.isConnected()) {
//                        BlockPosition frontBlockPos = BlockPosition.ofGlobal(zone, hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
//                        ClientNetworkManager.sendAsClient(new PlaceBlockPacket(frontBlockPos, blockPos.getBlockState(), -1));
//                    }
//
////                    if (triggers != null) {
////                        if (GameSingletons.isHost) {
////                            BlockEventArgs args = new BlockEventArgs();
////                            args.srcPlayer = InGame.getLocalPlayer();
////                            args.srcBlockState = blockPos.getBlockState();
////                            args.zone = zone;
////                            args.blockPos = blockPos;
////    //                        for (GameEventTrigger trigger: triggers) {
////    //                            Constants.LOGGER.warn("Triggers: {}", trigger.getAction());
////    //                        }
////                            args.run(triggers);
////                            args.runScheduledTriggers();
////                        }
////                    }
//                }
//
//                SoundManager.INSTANCE.playSound("cosmic_tools:sounds/items/spatula-place.ogg", 1, 1, 0);
//
//
//                ChunkUtils.remesh(chunksToUpdate, zone);
//            }
//        }
//        return true;
//    }
}
