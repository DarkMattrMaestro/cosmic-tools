package com.darkmattrmaestro.cosmic_tools.packets;

import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.packets.blocks.BlockReplacePacket;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ReapPacket extends GamePacket {
    Zone zone;
    BlockPosition blockPos;
    BlockState brokenBlockState;

    public ReapPacket() {
    }

    public ReapPacket(Zone zone, BlockPosition blockPos, BlockState brokenBlockState) {
        this.zone = zone;
        this.blockPos = blockPos;
        this.brokenBlockState = brokenBlockState;
    }

    public void receive(ByteBuf in) {
        this.zone = GameSingletons.world.getZoneCreateIfNull(this.readString(in));
        this.blockPos = this.readBlockPosition(in, this.zone);
        MissingBlockStateResult result = GameSingletons.isClient ? MissingBlockStateResult.EXCEPTION : MissingBlockStateResult.MISSING_OBJECT;
        this.brokenBlockState = BlockState.getInstance(this.readString(in), result);
    }

    public void write() {
        this.writeString(this.zone.zoneId);
        this.writeBlockPosition(this.blockPos);
        this.writeString(this.brokenBlockState.getSaveKey());
    }

    public void handle(NetworkIdentity identity, ChannelHandlerContext ctx) {
        if (!identity.isClient()) {
            BlockState actualCurrentBlockState = this.blockPos.getBlockState();
            if (actualCurrentBlockState != this.brokenBlockState) {
                if (actualCurrentBlockState != null) {
                    identity.send(new BlockReplacePacket(this.zone, actualCurrentBlockState, this.blockPos));
                }

            } else {
                if (this.brokenBlockState.hardness > 0.0F) {
                }

                BlockEventTrigger[] triggers = this.brokenBlockState.getTrigger("onBreak");
                if (triggers != null) {
                    BlockEventArgs args = new BlockEventArgs();
                    args.srcBlockState = this.brokenBlockState;
                    args.zone = this.zone;
                    args.blockPos = this.blockPos;
                    args.setSrcIdentity(identity);
                    args.run(triggers);
                    args.runScheduledTriggers();
                }
            }
        }
    }
}
