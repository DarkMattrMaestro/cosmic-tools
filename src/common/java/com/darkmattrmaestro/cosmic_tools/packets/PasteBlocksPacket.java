package com.darkmattrmaestro.cosmic_tools.packets;

import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.utils.BlockMappings;
import com.darkmattrmaestro.cosmic_tools.utils.PasteUtils;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;

public class PasteBlocksPacket extends GamePacket {
    public BlockMappings blockMappings = new BlockMappings();

    public PasteBlocksPacket() {
    }

    public PasteBlocksPacket(BlockMappings blockMappings) {
        this.blockMappings = blockMappings;
    }

    public void receive(ByteBuf in) {
        int size = this.readByte(in);
        for (int i = 0; i < size; i++) {
            BlockPosition blockPos = this.readBlockPositionZoneless(in);
            BlockState blockState = BlockState.getInstance(this.readString(in), MissingBlockStateResult.EXCEPTION);
            this.blockMappings.put(blockPos, blockState);
        }
    }

    public void write() {
        this.writeByte(blockMappings.size());
        for (BlockPosition blockPos: this.blockMappings.blockPositionSet()) {
            this.writeBlockPosition(blockPos);
            this.writeString(this.blockMappings.getBlockState(blockPos).getSaveKey());
        }
    }

    public void handle(NetworkIdentity identity, ChannelHandlerContext ctx) {
        if (!identity.isClient()) {

            Zone zone = identity.getZone();
            Player player = identity.getPlayer();
            if (player != null && zone != null) {
                if (player.gamemode.hasInfiniteItems() || PasteUtils.playerHasEnoughItems(player, this.blockMappings)) {
                    HashMap<String, Integer> requiredItems = PasteUtils.sumRequiredItems(this.blockMappings);

                    // Remove items from the player
                    if (!player.gamemode.hasInfiniteItems()) {
                        player.inventory.forEachSlot(((ItemSlot itemSlot) -> {
                            if (itemSlot.getItem() == null) {
                                return;
                            }

                            String itemID = itemSlot.getItem().getID();

                            if (requiredItems.containsKey(itemID)) {
                                int numRemove = Math.min(requiredItems.get(itemID), itemSlot.getItemAmount());
                                itemSlot.addAmount(-numRemove);
                                itemSlot.setDirty(true);
                                player.inventory.syncDirtySlots(0, identity);

                                if (requiredItems.get(itemID) - itemSlot.getItemAmount() <= 0) {
                                    requiredItems.remove(itemID);
                                } else {
                                    requiredItems.put(itemID, requiredItems.get(itemID) - itemSlot.getItemAmount());
                                }
                            }
                        }));
                    }

                    // Place blocks
//                    List<Chunk> chunksToUpdate = new ArrayList<>();
                    blockMappings.forEach((BlockPosition blockPos, BlockState blockState) -> {
                        blockPos.convertToLocal(zone);
                        Constants.LOGGER.warn("{} -> {}", blockPos, blockState);
                        BlockEventTrigger[] triggers = blockState.getTrigger("onPlace");

                        zone.setBlockState(blockState, blockPos.localX(), blockPos.localY(), blockPos.localZ());
//                        Chunk c = zone.getChunkAtBlock(blockPos.localX(), blockPos.localY(), blockPos.localZ());
//                        if (!chunksToUpdate.contains(c)) {
//                            chunksToUpdate.add(c);
//                        }

                        if (triggers != null) {
                            BlockEventArgs args = new BlockEventArgs();
                            args.srcBlockState = blockState;
                            args.zone = zone;
                            args.blockPos = blockPos;
                            args.setSrcIdentity(identity);
                            args.run(triggers);
                            args.runScheduledTriggers();
                        }
                    });
//                    ChunkUtils.remesh(chunksToUpdate, zone);
                }
            }
        }
    }
}
