package com.darkmattrmaestro.cosmic_tools.items;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.packets.PasteBlocksPacket;
import com.darkmattrmaestro.cosmic_tools.utils.*;
import finalforeach.cosmicreach.audio.SoundManager;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.BlockStateMissing;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.gameevents.blockevents.actions.common.BlockActionLootDrop;
import finalforeach.cosmicreach.gameevents.commonactions.GameActionLootDrop;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.*;
import finalforeach.cosmicreach.items.loot.Loot;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.networking.packets.blocks.BreakBlockPacket;
import finalforeach.cosmicreach.networking.packets.blocks.PlaceBlockPacket;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.util.constants.Direction;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientScythe extends FunctionalSpatula {

    public ClientScythe(String itemID) {
        super(itemID);
    }

//    public Hallucination getHallucination(Player player){
//        this.blockAxis = BlockSelectionUtil.getBlockSideLookingAtFar(this.reachDist);
//        if (this.blockAxis == null) return null;
//
//        return this.getHallucination(player, this.blockAxis);
//    }

    public void harvestBlock(Zone zone, BlockPosition blockPos) {
        BlockState blockState = blockPos.getBlockState();
        if (blockState != null) {
            BlockEventTrigger[] triggers = blockState.getTrigger("onBreak");
            if (triggers != null) {
                ItemRenderer.onBreakBlock();
                ItemSlot selectedSlot = UI.hotbar.getSelectedSlot();
                if (selectedSlot != null) {
                    ItemStack heldItemStack = selectedSlot.getItemStack();
                    if (heldItemStack != null && heldItemStack.hasDurability()) {
                        if (blockState.hardness > 0.0F) {
                            heldItemStack.damage();
                        }

                        if (heldItemStack.isBroken()) {
                            selectedSlot.setItemStack((ItemStack)null);
                        }
                    }
                }

                if (ClientNetworkManager.isConnected()) {
                    ClientNetworkManager.sendAsClient(new BreakBlockPacket(zone, blockPos, blockState));
                }

                if (GameSingletons.isHost) {
                    BlockEventArgs args = new BlockEventArgs();
                    args.srcPlayer = InGame.getLocalPlayer();
                    args.srcBlockState = blockState;
                    args.zone = zone;
                    args.blockPos = blockPos;
                    args.run(triggers);
                    args.runScheduledTriggers();
                }
            }
        }
    }

    public static boolean onMousePressed(int button) {
        if (GameState.currentGameState.getClass() != InGame.class) { return false; }

        ItemStack selected = UI.hotbar.getSelectedItemStack();
        if(selected == null || !Identifier.of(Constants.MOD_ID, "scythe").toString().equals(selected.getItem().getID())) {
            return false;
        }
        ClientScythe clientScythe = new ClientScythe(selected.getItem().getID());

        if(!BlockSelectionUtil.doesCollideFar(clientScythe.reachDist) || !BlockSelectionUtil.isInActiveGame()) { return false; }

        if(button == Input.Buttons.RIGHT) {
            // Right Click => replace crops

            BlockAxis blockAxis = BlockSelectionUtil.getBlockSideLookingAtFar(20);
            if (blockAxis == null) { return false; }
            Zone zone = InGame.getLocalPlayer().getZone();
            if (zone == null) { return false; }
            BlockState blockState = zone.getBlockState(blockAxis.pos.getGlobalX(), blockAxis.pos.getGlobalY(), blockAxis.pos.getGlobalZ());

            // Check that the block is a cultivable crop
            if (blockState.canDrop && ("farm".equals(blockState.getParam("type")) || "wild".equals(blockState.getParam("type")))) {
//                clientScythe.harvestBlock(zone, blockAxis.pos);

                List<Chunk> chunksToUpdate = new ArrayList<>();

//                zone.setBlockState(null, blockAxis.pos.getGlobalX(), blockAxis.pos.getGlobalY(), blockAxis.pos.getGlobalZ());
                Chunk c = zone.getChunkAtBlock(blockAxis.pos.getGlobalX(), blockAxis.pos.getGlobalY(), blockAxis.pos.getGlobalZ());
                if (!chunksToUpdate.contains(c)) {
                    chunksToUpdate.add(c);
                }

                BlockEventTrigger[] triggers = blockState.getTrigger("onBreak");
                if (triggers != null) {
                    BlockEventArgs args = new BlockEventArgs();
                    args.srcPlayer = InGame.getLocalPlayer();
                    args.srcBlockState = blockState;
                    args.zone = zone;
                    args.blockPos = blockAxis.pos;
                    args.run(triggers);
                    args.runScheduledTriggers();
                }

//                if (ClientNetworkManager.isConnected()) {
//                    ClientNetworkManager.sendAsClient(new BreakBlockPacket(zone, blockAxis.pos, blockState));
//                }

                BlockState freshBlockState = ((ItemBlock) blockState.getItem()).getBlockState();

                BlockEventArgs args = new BlockEventArgs();
                args.srcPlayer = InGame.getLocalPlayer();
                args.srcBlockState = blockState;
                args.zone = zone;
                args.blockPos = blockAxis.pos;
                args.run(triggers);
                args.runScheduledTriggers();

                freshBlockState.getBlockEvents().getTriggers("base:loot_drop");
//                blockState.getItem().useItemOnBlock(new ItemSlot(null, 1), InGame.getLocalPlayer(), blockAxis.pos, Direction.POS_Y, Vector3.Y);
                Constants.LOGGER.warn("{}, {}", (blockState.getItem().getID()), freshBlockState.getBlockEvents().getTriggerMap());
                BlockEventTrigger[] eventTriggers = freshBlockState.getBlockEvents().getTriggerMap().get("onBreak");
                BlockState foundBlockState = null;
                for (BlockEventTrigger eventTrigger: eventTriggers) {
                    if (eventTrigger.getAction() instanceof BlockActionLootDrop actionLootDrop) {
//                        BlockActionLootDrop actionLootDrop = (BlockActionLootDrop) eventTrigger;
                        if (eventTrigger.condition != null && !eventTrigger.condition.test(args)) { continue; }
                        Constants.LOGGER.warn("+- eventTrigger: {}, {} {}", eventTrigger, eventTrigger.condition != null, !eventTrigger.condition.test(args));
                        // TODO: Fix likely error
                        Constants.LOGGER.warn("aaaaaaaaaa");
                        Loot loot = Loot.get(actionLootDrop.lootId);
                        Constants.LOGGER.warn("Loot {}", loot);
                        Constants.LOGGER.warn("Lootoptions {}", loot.options);
                        Constants.LOGGER.warn("lootStacks length {}", loot.options.first().lootStacks.length);
                        Constants.LOGGER.warn("---    lootStacks {}", loot.options.first().lootStacks[0].item);
                        Item lootItem = loot.options.first().lootStacks[0].item;
                        // TODO: Guard agains non-instance
                        Constants.LOGGER.warn("foundBlockState {}", lootItem.useItemOnBlock(new ItemSlot(null, 1), InGame.getLocalPlayer(), blockAxis.pos.getOffsetBlockPos(zone, Direction.NEG_Y), Direction.POS_Y, null));
//                        Constants.LOGGER.warn("foundBlockState {}", ((ItemBlock) lootItem).getBlockState());
//                        foundBlockState = ((ItemBlock) lootItem).getBlockState();
                        break;
                    }
                }

//                Constants.LOGGER.warn("Found BlockState: {}", foundBlockState);
//
//                triggers = foundBlockState.getTrigger("onPlace");
//                if (triggers != null) {
//                    BlockEventArgs args1 = new BlockEventArgs();
//                    args1.srcPlayer = InGame.getLocalPlayer();
//                    args1.srcBlockState = foundBlockState;
//                    args1.zone = zone;
//                    args1.blockPos = blockAxis.pos;
//                    args1.run(triggers);
//                    args1.runScheduledTriggers();
//                }
//
//                if (ClientNetworkManager.isConnected()) {
//                    BlockMappings freshBlockMappings = new BlockMappings();
//                    freshBlockMappings.put(blockAxis.pos, freshBlockState);
//                    ClientNetworkManager.sendAsClient(new PasteBlocksPacket(freshBlockMappings));
//                }

                ChunkUtils.remesh(chunksToUpdate, zone);
            }


//            List<Chunk> chunksToUpdate = new ArrayList<>();
//            String triggerName = "onPlace";
//
//            Zone zone = InGame.getLocalPlayer().getZone();
//            for (BlockPosition blockPos : clientScythe.copyBlocks.blocks) {
//                BlockEventTrigger[] triggers = blockPos.getBlockState().getTrigger(triggerName);
//
//                Vector3Int hallucinatedPos = (new Vector3Int(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ())).add(clientScythe.blockAxis.axis);
//
//                zone.setBlockState(blockPos.getBlockState(), hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
//                Chunk c = zone.getChunkAtBlock(hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
//                if (!chunksToUpdate.contains(c)) {
//                    chunksToUpdate.add(c);
//                }
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
//
//            }
//
//            if (ClientNetworkManager.isConnected()) {
//                List<BlockPosition> offsetBlocks = clientScythe.copyBlocks.blocks.stream().map((BlockPosition blockPos) -> {
//                    return blockPos.getOffsetBlockPos(clientScythe.blockAxis.axis.x, clientScythe.blockAxis.axis.y, clientScythe.blockAxis.axis.z);
//                }).toList();
//                ClientNetworkManager.sendAsClient(new PasteBlocksPacket(BlockMappings.ofZonePositions(zone, offsetBlocks)));
//            }
//
//            ChunkUtils.remesh(chunksToUpdate, zone);
//
//            SoundManager.INSTANCE.playSound("cosmic_tools:sounds/items/spatula-place.ogg", 1, 1, 0);
        }
        return true;
    }
}
