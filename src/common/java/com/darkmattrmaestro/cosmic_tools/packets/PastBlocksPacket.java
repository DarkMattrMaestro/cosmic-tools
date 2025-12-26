package com.darkmattrmaestro.cosmic_tools.packets;

import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemBlock;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PastBlocksPacket extends GamePacket {
    BlockPosition blockPos;
    BlockState targetBlockState;
    byte itemSlotNum;

    public PastBlocksPacket() {
    }

    public PastBlocksPacket(BlockPosition blockPos, BlockState targetBlockState, int hotbarSlot) {
        this.blockPos = blockPos;
        this.targetBlockState = targetBlockState;
        this.itemSlotNum = (byte)hotbarSlot;
    }

    public void receive(ByteBuf in) {
        this.blockPos = this.readBlockPositionZoneless(in);
        this.targetBlockState = BlockState.getInstance(this.readString(in), MissingBlockStateResult.EXCEPTION);
        this.itemSlotNum = this.readByte(in);
    }

    public void write() {
        this.writeBlockPosition(this.blockPos);
        this.writeString(this.targetBlockState.getSaveKey());
        this.writeByte(this.itemSlotNum);
    }

    public void handle(NetworkIdentity identity, ChannelHandlerContext ctx) {
        if (!identity.isClient()) {
            Zone zone = identity.getZone();
            Player player = identity.getPlayer();
            if (player != null && zone != null) {
                this.blockPos.convertToLocal(zone);
                ItemSlot itemSlot = player.inventory.getSlot(this.itemSlotNum);
                if (itemSlot != null) {
                    ItemStack itemStack = itemSlot.getItemStack();
                    if (itemStack != null && itemStack.amount > 0) {
                        Item item = itemStack.getItem();
                        if (!(item instanceof ItemBlock)) {
                            itemSlot.setDirty(true);
                            player.inventory.syncDirtySlots(0, identity);
                        } else {
                            BlockState blockState = ((ItemBlock)item).getBlockState();
                            if (blockState.getBlock() != this.targetBlockState.getBlock()) {
                                itemSlot.setDirty(true);
                                player.inventory.syncDirtySlots(0, identity);
                            } else {
                                BlockEventTrigger[] triggers = this.targetBlockState.getTrigger("onPlace");
                                if (triggers != null) {
                                    BlockEventArgs args = new BlockEventArgs();
                                    args.srcBlockState = this.targetBlockState;
                                    args.zone = zone;
                                    args.blockPos = this.blockPos;
                                    args.setSrcIdentity(identity);
                                    args.run(triggers);
                                    args.runScheduledTriggers();
                                    if (!player.gamemode.hasInfiniteItems()) {
                                        itemSlot.addAmount(-1);
                                        itemSlot.setDirty(true);
                                        player.inventory.syncDirtySlots(0, identity);
                                    }

                                }
                            }
                        }
                    } else {
                        itemSlot.setDirty(true);
                        player.inventory.syncDirtySlots(0, identity);
                    }
                }
            }
        }
    }
}
