package com.darkmattrmaestro.cosmic_tools.gameevents.actions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.packets.ReapPacket;
import com.darkmattrmaestro.cosmic_tools.utils.ChunkUtils;
import com.darkmattrmaestro.cosmic_tools.utils.Vector3Int;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gameevents.ActionId;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.gameevents.blockevents.actions.BlockActionItemDrop;
import finalforeach.cosmicreach.gameevents.itemevents.ItemEventArgs;
import finalforeach.cosmicreach.gameevents.itemevents.actions.IItemAction;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemBlock;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.items.containers.SlotContainer;
import finalforeach.cosmicreach.items.loot.Loot;
import finalforeach.cosmicreach.networking.packets.blocks.BreakBlockPacket;
import finalforeach.cosmicreach.util.constants.Direction;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import com.darkmattrmaestro.cosmic_tools.utils.Vector3Int;

import java.util.ArrayList;
import java.util.List;

@ActionId(
        id = "cosmic_tools:reap_crops"
)
public class ItemActionReapCrops implements IItemAction {
    int in = 0;
    int out = 5;
    int height = 5;
    float angleLeft = 25.0F;
    float angleRight = -25.0F;

    public void act(ItemEventArgs args) {
        Constants.LOGGER.warn("Ran reap_crops");
        Player player = args.srcPlayer;
        GameEntity playerEntity = player.getEntity();
        Vector3 playerViewDir = playerEntity.viewDirection;
        Vector3 playerPos = player.getPosition();
        Constants.LOGGER.warn("a");
        Constants.LOGGER.warn("reap_crops vars: in={} out={} height={} angleLeft={} angleRight={}", this.in, this.out, this.height, this.angleLeft, this.angleRight);

        for (int x = -this.out; x <= this.out; x++) {
            for (int z = -this.out; z <= this.out; z++) {
                for (int y = -this.height; y <= this.height; y++) {
                    Vector3Int blockPos = new Vector3Int(x, y, z);
                    // Check that the block is in range
                    if (blockPos.x*blockPos.x + blockPos.y*blockPos.y + blockPos.z*blockPos.z > out*out || blockPos.x*blockPos.x + blockPos.y*blockPos.y + blockPos.z*blockPos.z < in*in) {
                        continue;
                    }

                    Vector3Int relPos = blockPos.cpy().add((int) playerPos.x, (int) playerPos.y, (int) playerPos.z);

                    // Check that the block is a cultivable crop
                    BlockState blockState = args.zone.getBlockState(relPos.x, relPos.y, relPos.z);
                    if (!(blockState.canDrop && ("farm".equals(blockState.getParam("type")) || "wild".equals(blockState.getParam("type"))))) {
                        continue;
                    }

                    Constants.LOGGER.warn("Cultivable block at {}", relPos);
                    Constants.LOGGER.warn("relPos {}, this.angleLeft {}, playerViewDir {}, playerPos {}", relPos, this.angleLeft, playerViewDir, playerPos);

                    if (!isWithinAngles(relPos, this.angleLeft, this.angleRight, playerViewDir, playerPos)) {// || isPlaneRight(relPos, this.angleRight, playerViewDir, playerPos)) {
                        continue;
                    }

                    Constants.LOGGER.warn("Block at {}", relPos);
                    this.reapCrop(args, blockState, BlockPosition.ofGlobal(args.zone, relPos.x, relPos.y, relPos.z));
                }
            }
        }

//        projectile.setPosition(player.getPosition());
//        projectile.position.add(playerEntity.viewPositionOffset);
//        projectile.velocity.set(playerEntity.viewDirection).scl((float)this.speed);
//        projectile.velocity.add(this.velocityAdd);
//        Zone zone = player.getZone();
//        if (projectile instanceof EntityProjectileLaser laserProjectile) {
//            float refractiveIndex = 1.0F;
//            BlockState blockState = zone.getBlockState(projectile.getPosition());
//            if (blockState != null) {
//                refractiveIndex = blockState.refractiveIndex;
//            }
//
//            laserProjectile.refractiveIndex = refractiveIndex;
//            ItemStack itemStack = args.getItemStack();
//            if (itemStack != null && itemStack.stackMetadata.has("laserColor")) {
//                int colorMetadata = itemStack.stackMetadata.getInt("laserColor", -1);
//                laserProjectile.setLaserColor(this.tmpColor.set(colorMetadata));
//            }
//        }
//
//        zone.addEntity(projectile);
    }

    public void reapCrop(ItemEventArgs args, BlockState blockState, BlockPosition blockPos) {
//        List<Chunk> chunksToUpdate = new ArrayList<>();
//
////                zone.setBlockState(null, blockAxis.pos.getGlobalX(), blockAxis.pos.getGlobalY(), blockAxis.pos.getGlobalZ());
//        Chunk c = args.zone.getChunkAtBlock(blockAxis.pos.getGlobalX(), blockAxis.pos.getGlobalY(), blockAxis.pos.getGlobalZ());
//        if (!chunksToUpdate.contains(c)) {
//            chunksToUpdate.add(c);
//        }

        BlockEventArgs cropArgs = new BlockEventArgs();
        cropArgs.srcPlayer = args.srcPlayer;
        cropArgs.srcBlockState = blockState;
        cropArgs.zone = args.zone;
        cropArgs.blockPos = blockPos;

        // Break the crop
        BlockEventTrigger[] triggers = blockState.getTrigger("onBreak");
        if (triggers != null) {
            cropArgs.run(triggers);
            cropArgs.runScheduledTriggers();
        }

        // TODO: Set up ReapPacket
//        if (ClientNetworkManager.isConnected()) {
//            ClientNetworkManager.sendAsClient(new ReapPacket(args.zone, blockPos, blockState));
//        }

        BlockState freshBlockState = ((ItemBlock) blockState.getItem()).getBlockState();

//        BlockEventArgs args = new BlockEventArgs();
//        args.srcPlayer = args.srcPlayer;
//        args.srcBlockState = blockState;
//        args.zone = args.zone;
//        args.blockPos = blockAxis.pos;
//        args.run(triggers);
//        args.runScheduledTriggers();

        freshBlockState.getBlockEvents().getTriggers("base:loot_drop");
//                blockState.getItem().useItemOnBlock(new ItemSlot(null, 1), InGame.getLocalPlayer(), blockAxis.pos, Direction.POS_Y, Vector3.Y);
        Constants.LOGGER.warn("{}, {}", (blockState.getItem().getID()), freshBlockState.getBlockEvents().getTriggerMap());
        BlockEventTrigger[] eventTriggers = freshBlockState.getBlockEvents().getTriggerMap().get("onBreak");
        BlockState foundBlockState = null;
        for (BlockEventTrigger eventTrigger: eventTriggers) {
//            if (eventTrigger.getAction() instanceof BlockActionItemDrop actionItemDrop) {
////                        BlockActionLootDrop actionLootDrop = (BlockActionLootDrop) eventTrigger;
//                if (eventTrigger.condition != null && !eventTrigger.condition.test(cropArgs)) { continue; }
//                Constants.LOGGER.warn("+- eventTrigger: {}, {} {}", eventTrigger, eventTrigger.condition != null, !eventTrigger.condition.test(cropArgs));
//                // TODO: Fix likely error
//                Constants.LOGGER.warn("aaaaaaaaaa");
//                Loot loot = Loot.get(actionItemDrop.lootId);
//                Constants.LOGGER.warn("Loot {}", loot);
//                Constants.LOGGER.warn("Lootoptions {}", loot.options);
//                Constants.LOGGER.warn("lootStacks length {}", loot.options.first().lootStacks.length);
//                Constants.LOGGER.warn("---    lootStacks {}", loot.options.first().lootStacks[0].item);
//                Item lootItem = loot.options.first().lootStacks[0].item;
//                // TODO: Guard agains non-instance
//                SlotContainer dummySlotContainer = new SlotContainer(2);
////                dummySlotContainer.addItemStack(new ItemStack(blockState.getItem(), 10000));
//                ItemSlot dummyItemSlot = new ItemSlot(dummySlotContainer, 1);
//                dummyItemSlot.setItemStack(new ItemStack(blockState.getItem(), 10000));
//                lootItem.useItemOnBlock(dummyItemSlot, args.srcPlayer, blockPos.getOffsetBlockPos(args.zone, Direction.NEG_Y), Direction.POS_Y, null);
//
////                        if (ClientNetworkManager.isConnected()) {
////                            ClientNetworkManager.sendAsClient(new SlotInteractPacket(SlotInteractionType.CURSOR_RIGHT, -1, ));
////                        }
//                break;
//            }
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

//        ChunkUtils.remesh(chunksToUpdate, zone);
    }

    public static boolean isWithinAngles(Vector3Int point, float angleLeft, float angleRight, Vector3 viewDir, Vector3 playerPos) {
        Vector3 relPos = (new Vector3(point.x + 0.5f, point.y + 0.5f, point.z + 0.5f)).sub(playerPos);
        float diffAngle = (1080 - angleLeft + (new Vector2(viewDir.x, viewDir.z)).angleDeg() - (new Vector2(relPos.x, relPos.z)).angleDeg()) % 360;

        Constants.LOGGER.warn("     + diffAngle={} check={}", diffAngle, (1080 + angleLeft + angleRight) % 360);
        return diffAngle <= (1080 + angleLeft + angleRight) % 360;
    }
}
