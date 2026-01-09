package com.darkmattrmaestro.cosmic_tools.gameevents.actions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.utils.Vector3Int;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.entities.IProjectileEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.entities.projectiles.EntityProjectileLaser;
import finalforeach.cosmicreach.gameevents.ActionId;
import finalforeach.cosmicreach.gameevents.itemevents.ItemEventArgs;
import finalforeach.cosmicreach.gameevents.itemevents.actions.IItemAction;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.world.Zone;

@ActionId(
        id = "cosmic_tools:reap_crops"
)
public class ItemActionReapCrops implements IItemAction {
    String entityId;
    Vector3 velocityAdd = new Vector3();
    int in = 0;
    int out = 5;
    int height = 5;
    float angleLeft = 25.0F;
    float angleRight = -25.0F;

    public void act(ItemEventArgs args) {
        Player player = args.srcPlayer;
        Entity playerEntity = player.getEntity();
        Vector3 playerViewDir = playerEntity.viewDirection;
        Vector3 playerPos = player.getPosition();

        for (int x = (int) playerPos.x - this.out; x <= (int) playerPos.x + this.out; x++) {
            for (int z = (int) playerPos.z - this.out; z <= (int) playerPos.z + this.out; z++) {
                for (int y = (int) playerPos.y - this.height; z <= (int) playerPos.y + this.height; y++) {
                    Vector3Int blockPos = new Vector3Int(x, y, z);
                    // Check that the block is in range
                    if (blockPos.len2() > out*out || blockPos.len2() < in*in) {
                        continue;
                    }

                    // Check that the block is a cultivable crop
                    BlockState blockState = args.zone.getBlockState(blockPos.x, blockPos.y, blockPos.z);
                    if (!(blockState.canDrop && ("farm".equals(blockState.getParam("type")) || "wild".equals(blockState.getParam("type"))))) {
                        continue;
                    }

                    if (!isPlaneRight(blockPos, this.angleLeft, playerViewDir, playerPos) || isPlaneRight(blockPos, this.angleRight, playerViewDir, playerPos)) {
                        continue;
                    }

                    Constants.LOGGER.warn("Block at {}", blockPos);
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

    public static boolean isPlaneRight(Vector3Int point, float angle, Vector3 viewDir, Vector3 playerPos) {
        // Set the player as the origin
        Vector3 relPos = playerPos.cpy().add(-point.x, -point.y, -point.z);
        relPos.rotate(-(new Vector2(viewDir.x, viewDir.z)).angleDeg(), 0,1,0);

        // Apply the plane angle
        relPos.rotate(-angle, 0,1,0);
        return relPos.x >= 0;
    }
}
